package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
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

    private static String[] getApiKeys() {
        try (FileInputStream configFileStream = new FileInputStream("resources/config.properties")) {
            Properties config = new Properties();
            config.load(configFileStream);
            String geocodingKey = config.getProperty("geocoding.apikey");         // get geocoding api key
            String weatherKey = config.getProperty("weather.apikey"); // get weather api key
            return new String[] {geocodingKey,weatherKey};
        } catch (IOException ioe) {
            System.err.println(ioe);
            ioe.printStackTrace();
            return null;
        } // try
    }

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
    Button logo;
    Label directions;
    TextField searchBar;
    Button searchButton;

    // right side of app
    VBox rightResult;
    WebView webView;
    WebEngine webEngine;
    ImageView weatherIcon;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new HBox(8);
        root.setStyle("-fx-background-color: white;");

        // left side of app
        leftMenu = new VBox(5);
        leftMenu.setAlignment(Pos.CENTER);
        ImageView logoImg = new ImageView(new Image("file:resources/logo.png"));
        logoImg.setPreserveRatio(true);
        logoImg.setFitWidth(100);
        logo = new Button("", logoImg);
        directions = new Label("Enter a city to see the current weather");
        searchBar = new TextField("Seoul");
        searchButton = new Button("Search");

        divider = new Separator(Orientation.VERTICAL);

        // right side of app
        rightResult = new VBox(5);
        webView = new WebView();
        webEngine = webView.getEngine();
        rightResult.setAlignment(Pos.TOP_CENTER);
    } // ApiApp

    String svgContent = "";

    /** {@inheritDoc} */
    @Override
    public void init() {
        leftMenu.getChildren()
            .addAll(logo, directions, searchBar, searchButton);
        rightResult.getChildren()
            .addAll(webView);

        // weather icon functionality
        rightResult.setMinWidth(1280 - leftMenu.getWidth() - 200);
        webView.setMaxWidth(210);
        webView.setMaxHeight(220);
        svgContent = readSvgContent("resources/openweathermap/02d.svg");
        Platform.runLater(() -> webEngine.loadContent(svgContent));

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
            System.out.println(jsonString);
            /**
            * TODO: ensure request is ok 
            */
            GeocodingResponse[] geocodingResponse = GSON.fromJson(jsonString, GeocodingResponse[].class);
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
            System.out.println(jsonString);
            /**
            * TODO: ensure request is ok 
            */

            WeatherApiResponse weatherResponse = GSON.fromJson(jsonString, WeatherApiResponse.class);
            String icon = weatherResponse.weather[0].icon + ".svg";
            svgContent = readSvgContent("resources/openweathermap/" + icon);
            Platform.runLater(() -> webEngine.loadContent(svgContent)); 
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
        root.getChildren().addAll(leftMenu, divider, rightResult);
        scene = new Scene(root, 1280, 720);

        // setup stage
        stage.setTitle("ApiApp!");      
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
    } // start

    private String readSvgContent(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
} // ApiApp
