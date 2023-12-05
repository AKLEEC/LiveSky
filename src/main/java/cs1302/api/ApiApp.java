package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Separator;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.net.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {

    /* HTTP Client */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    /* Google Gson object for parsing JSON-formatted strings */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    
    // top of heiarchy
    Stage stage;
    Scene scene;
    HBox root;
    Separator divider;

    // left side of app
    VBox leftMenu;
    ImageView logoImg;
    Button logo;
    Label directions;
    TextField searchBar;
    Button searchButton;

    // right side of app
    VBox rightResult;
   // HBox cityBox;
    Label cityLabel;
    HBox webViewIconBox;
    WebView webViewIcon;
    WebEngine webEngineIcon;
    VBox informationBox;
    Label tempDescription;
    HBox temperatureBox;
    Label temp;
    Label feelsLikeTemp;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        this.stage = null;
        this.scene = null;
        this.root = new HBox(8);
        root.setStyle("-fx-background-color: white;");

        // left side of app
        leftMenu = new VBox(5);
        logoImg = new ImageView(new Image("file:resources/logo.png"));
        logo = new Button("", logoImg);
        directions = new Label("Enter a city to see the current weather");
        searchBar = new TextField("Seoul");
        searchButton = new Button("Search");

        divider = new Separator(Orientation.VERTICAL);

        // right side of app
        rightResult = new VBox();
        cityLabel = new Label("CITY");
        webViewIcon = new WebView();
        webViewIconBox = new HBox(webViewIcon);
        webViewIconBox.setAlignment(Pos.TOP_CENTER);        
        temp = new Label();
        feelsLikeTemp = new Label();
        temperatureBox = new HBox(8, temp, feelsLikeTemp);
        tempDescription = new Label();
        informationBox = new VBox(tempDescription, temperatureBox);
    } // ApiApp

    String svgContent = "";

    /** {@inheritDoc} */
    @Override
    public void init() {
        webEngineIcon = webViewIcon.getEngine();
        logoImg.setPreserveRatio(true);
        logoImg.setFitWidth(100);

        // build hierarchy
        leftMenu.getChildren()
            .addAll(logo, directions, searchBar, searchButton);       
        rightResult.getChildren().addAll(cityLabel, webViewIconBox, informationBox);
        root.getChildren().addAll(leftMenu, divider, rightResult);
        
        leftMenu.setAlignment(Pos.CENTER);
        rightResult.setAlignment(Pos.TOP_CENTER); 
        informationBox.setAlignment(Pos.TOP_CENTER);
        temperatureBox.setAlignment(Pos.BOTTOM_CENTER);

        webViewIconBox.setMaxSize(190, 190);
        VBox.setVgrow(webViewIconBox, Priority.ALWAYS);
        temperatureBox.setMaxHeight(temp.getHeight());
        cityLabel.setPadding(new Insets(-10, 0, -20, 0));

        /* 
        rightResult.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        webViewIconBox.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        svgContent = readSvgContent("resources/openweathermap/01d.svg");
        Platform.runLater(() -> webEngineIcon.loadContent(svgContent));    
        temperatureBox.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        */

        // weather icon functionality
        rightResult.setMinWidth(1280 - leftMenu.getWidth() - 200);        
        Platform.runLater(() -> webEngineIcon.setUserStyleSheetLocation("file:resources/style.css"));

        // search button functionality
        EventHandler<ActionEvent> searchClicked = (ActionEvent e) -> {
            this.getWeather(this.getCoordinates(searchBar.getText()));
        };
        searchButton.setOnAction(searchClicked);

    }

    private double[] getCoordinates(String city) {
        city = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String coordQuery = String.format("city=%s", city);
        String coordUri = "https://api.api-ninjas.com/v1/geocoding?" + coordQuery;

        try {
            // build request
            HttpRequest request = HttpRequest.newBuilder()
                .header("X-Api-Key", getApiKeys()[0])
                .uri(URI.create(coordUri))
                .build();    
            
            // send request & recieve response
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            String jsonString = response.body();
            /**
            * TODO: ensure request is ok 
            */
            GeocodingResponse[] geocodingResponse = GSON.fromJson(jsonString, GeocodingResponse[].class);
            System.out.println(GSON.toJson(geocodingResponse[0]));
            cityLabel.setText(geocodingResponse[0].name);
            cityLabel.setFont(new Font(75));
            double[] coordinates = {geocodingResponse[0].latitude, geocodingResponse[0].longitude};
            return coordinates;
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            directions.setText("Last attempt to get weather failed...");
            return null;
        }
    } // getCoordinates
    
    private void getWeather(double[] coordinates) {
        String latitude = URLEncoder.encode("" + coordinates[0], StandardCharsets.UTF_8);
        String longitude = URLEncoder.encode("" + coordinates[1], StandardCharsets.UTF_8);
        String units = URLEncoder.encode("imperial", StandardCharsets.UTF_8);
        String apiKey = getApiKeys()[1];
        String coordQuery = String.format("lat=%s&lon=%s&appid=%s&units=%s", latitude, longitude, apiKey, units);
        String coordUri = "https://api.openweathermap.org/data/2.5/weather?" + coordQuery;

        try {
            // build request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(coordUri))
                .build();    
            
            // send request & recieve response
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            String jsonString = response.body();
            /**
             * TODO: ensure request is ok 
            */

            WeatherApiResponse weatherResponse = GSON.fromJson(jsonString, WeatherApiResponse.class);
            System.out.println(GSON.toJson(weatherResponse));
            String icon = weatherResponse.weather[0].icon + ".svg";
            svgContent = readSvgContent("resources/openweathermap/" + icon);
            Platform.runLater(() -> webEngineIcon.loadContent(svgContent));
            tempDescription.setText(weatherResponse.weather[0].description);
            tempDescription.setFont(new Font("Comic Sans MS", 35));
            tempDescription.setOpacity(0.8);
            temp.setText((int)(Math.round(weatherResponse.main.temp)) + "°F");
            temp.setFont(new Font(50));
            feelsLikeTemp.setText("feel like: " + (int)(Math.round(weatherResponse.main.feels_like)) + "°F");
            feelsLikeTemp.setFont(new Font(20));
            feelsLikeTemp.setOpacity(0.7);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            directions.setText("Last attempt to get weather failed...");
        }
    }
    
    /**
     * * String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(epoch * 1000))
     */
    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
    
        // setup scene
        scene = new Scene(root, 1280, 720);

        // setup stage
        stage.setTitle("ApiApp!");      
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    private String readSvgContent(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String[] getApiKeys() {
        try (FileInputStream configFileStream = new FileInputStream("resources/config.properties")) {
            Properties config = new Properties();
            config.load(configFileStream);
            String geocodingKey = config.getProperty("geocoding.apikey"); // get geocoding api key
            String weatherKey = config.getProperty("weather.apikey"); // get weather api key
            return new String[] {geocodingKey,weatherKey};
        } catch (IOException ioe) {
            System.err.println(ioe);
            ioe.printStackTrace();
            return null;
        } // try
    }
} // ApiApp
