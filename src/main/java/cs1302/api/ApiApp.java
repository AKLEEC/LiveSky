package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
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

/**
 * Weather app that shows the current weather based on user input.
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
    Label directions;
    TextField citySearchBar;
    TextField stateSearchBar;
    TextField countrySearchBar;
    Button searchButton;

    // right side of app
    VBox rightResult;
    HBox cityBox;
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
        logoImg = new ImageView(new Image("file:resources/logo.png"));
        directions = new Label("  Enter a city to see the current weather");
        citySearchBar = new TextField();
        stateSearchBar = new TextField();
        countrySearchBar = new TextField();
        searchButton = new Button("Search");
        leftMenu = new VBox(5,
            logoImg, directions, citySearchBar, countrySearchBar, stateSearchBar, searchButton);
        divider = new Separator(Orientation.VERTICAL);

        // right side of app
        cityLabel = new Label();
        cityBox = new HBox(cityLabel);
        webViewIcon = new WebView();
        webViewIconBox = new HBox(webViewIcon);
        webViewIconBox.setAlignment(Pos.TOP_CENTER);
        temp = new Label();
        feelsLikeTemp = new Label();
        temperatureBox = new HBox(8, temp, feelsLikeTemp);
        tempDescription = new Label();
        informationBox = new VBox(tempDescription, temperatureBox);
        rightResult = new VBox(cityBox, webViewIconBox, informationBox);
    } // ApiApp

    String svgContent = "";

    /** {@inheritDoc} */
    @Override
    public void init() {
        directions.setWrapText(true);
        cityLabel.setWrapText(true);
        citySearchBar.setPromptText("City (required)");
        stateSearchBar.setPromptText("State (available if country is \"US\")");
        countrySearchBar.setPromptText("Country (optional - US)");
        webEngineIcon = webViewIcon.getEngine();
        logoImg.setPreserveRatio(true);
        logoImg.setFitWidth(100);
        searchButton.setDisable(true);

        stateSearchBar.setDisable(true);

        // build hierarchy
        root.getChildren().addAll(leftMenu, divider, rightResult);
        HBox.setHgrow(rightResult, Priority.ALWAYS);

        leftMenu.setAlignment(Pos.CENTER);
        rightResult.setAlignment(Pos.CENTER);
        cityBox.setAlignment(Pos.CENTER);
        informationBox.setAlignment(Pos.TOP_CENTER);
        temperatureBox.setAlignment(Pos.BOTTOM_CENTER);
        webViewIconBox.setMaxSize(190, 190);
        VBox.setVgrow(webViewIconBox, Priority.ALWAYS);
        temperatureBox.setMaxHeight(temp.getHeight());
        HBox.setMargin(feelsLikeTemp, new Insets(0, 0, 10,0));
        // weather icon functionality
        Platform.runLater(() -> {
            webEngineIcon.setUserStyleSheetLocation("file:resources/style.css");
        });
        this.getWeather(this.getCoordinates("Athens", "GA", "US"));

        // search button functionality
        EventHandler<ActionEvent> searchClicked = (ActionEvent e) -> {
            directions.setText("  Getting Results...");
            String[] location = {
                citySearchBar.getText(), stateSearchBar.getText(), countrySearchBar.getText()
            };
            runNow(() -> this.getWeather(
                this.getCoordinates(location[0], location[1], location[2])));
        };
        searchButton.setOnAction(searchClicked);

        // set listeners
        citySearchBar.textProperty().addListener((observable, oldText, newText) -> {
            if (oldText.equals("") && !newText.equals("")) {
                searchButton.setDisable(false);
            } else if (!oldText.equals("") && newText.equals("")) {
                searchButton.setDisable(true);
            }
        });
        countrySearchBar.textProperty().addListener((observable, oldText, newText) -> {
            if (newText.equalsIgnoreCase("US")) {
                stateSearchBar.setDisable(false);
            } else {
                stateSearchBar.setDisable(true);
                stateSearchBar.setText("");
            }
        });
    }

    /**
     * Gets the coordinates of a city, state, and country from API-Ninjas Geocoding API.
     *
     * @param city user inputted
     * @param state user inputted
     * @param country user inputted
     * @return an array of doubles containing the latitude and longitude of the city
     */
    private double[] getCoordinates(String city, String state, String country) {
        city = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String coordQuery = String.format("city=%s", city);
        if (!state.trim().equals("")) {
            state = URLEncoder.encode(state, StandardCharsets.UTF_8);
            coordQuery += String.format("&state=%s", state);
        }
        if (!country.trim().equals("")) {
            country = URLEncoder.encode(country, StandardCharsets.UTF_8);
            coordQuery += String.format("&country=%s", country);
        }
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

            GeocodingResponse[] geocodingResponse = GSON.fromJson(
                jsonString, GeocodingResponse[].class);
            if (geocodingResponse.length == 0) {
                throw new IllegalArgumentException("No results found");
            }

            Platform.runLater(() -> {
                cityLabel.setText(geocodingResponse[0].name + ", " + geocodingResponse[0].country);
            });
            if (geocodingResponse[0].name.length() < 15) {
                cityLabel.setFont(new Font(50));
            } else {
                cityLabel.setFont(new Font(25));
            }
            double[] coordinates = {geocodingResponse[0].latitude, geocodingResponse[0].longitude};
            return coordinates;
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            return null;
        }
    } // getCoordinates

    /**
     * Gets the current weather of a city from OpenWeatherMap API.
     *
     * @param coordinates an array of doubles containing the latitude and longitude of the city
     */
    private void getWeather(double[] coordinates) {
        try {
            if (coordinates == null) {
                throw new IllegalArgumentException("No results found");
            }
            String latitude = URLEncoder.encode("" + coordinates[0], StandardCharsets.UTF_8);
            String longitude = URLEncoder.encode("" + coordinates[1], StandardCharsets.UTF_8);
            String units = URLEncoder.encode("imperial", StandardCharsets.UTF_8);
            String apiKey = getApiKeys()[1];
            String coordQuery = String.format(
                "lat=%s&lon=%s&appid=%s&units=%s", latitude, longitude, apiKey, units);
            String coordUri = "https://api.openweathermap.org/data/2.5/weather?" + coordQuery;
            // build request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(coordUri))
                .build();

            // send request & recieve response
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            String jsonString = response.body();

            WeatherApiResponse weatherResponse = GSON.fromJson(
                jsonString, WeatherApiResponse.class);
            String icon = weatherResponse.weather[0].icon + ".svg";
            svgContent = readSvgContent("resources/openweathermap/" + icon);
            tempDescription.setFont(new Font("Comic Sans MS", 35));
            tempDescription.setOpacity(0.8);
            temp.setFont(new Font(50));
            Platform.runLater(() -> {
                feelsLikeTemp.setText(
                    "feels like: " + (int)(Math.round(weatherResponse.main.feelsLike)) + "°F");
                directions.setText("  Enter a city to see the current weather");
                temp.setText((int)(Math.round(weatherResponse.main.temp)) + "°F");
                webEngineIcon.loadContent(svgContent);
                tempDescription.setText(weatherResponse.weather[0].description);
            });
            feelsLikeTemp.setFont(new Font(20));
            feelsLikeTemp.setOpacity(0.7);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            Platform.runLater(() -> {
                directions.setText("Last attempt to get weather failed...");
                alertError(e);
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setMaxWidth(1280);
        stage.setMaxHeight(720);
        // setup scene
        scene = new Scene(root, 1080, 600);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    /**
     * Read the svg files as bytes to show animations in the app.
     * @param filePath the file path to the svg file
     * @return string of bytes
     */
    private String readSvgContent(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Show a modal error alert based on {@code cause}.
     * @param cause a {@link java.lang.Throwable Throwable} that caused the alert
     */
    public void alertError(Throwable cause) {
        TextArea text = new TextArea();
        text.appendText("\n\nException: " + cause.toString());
        text.setEditable(false);
        Alert alert = new Alert(AlertType.ERROR);
        alert.getDialogPane().setContent(text);
        alert.setResizable(true);
        alert.showAndWait();
    } // alertError

    /**
     * Returns an array of API keys from the config file.
     * @return an array of API keys
     */
    private static String[] getApiKeys() {
        try (FileInputStream configFileStrea = new FileInputStream("resources/config.properties")) {
            Properties config = new Properties();
            config.load(configFileStrea);
            String geocodingKey = config.getProperty("geocoding.apikey"); // get geocoding api key
            String weatherKey = config.getProperty("weather.apikey"); // get weather api key
            return new String[] {geocodingKey,weatherKey};
        } catch (IOException ioe) {
            return null;
        } // try
    }

    /**
     * Creates and immediately starts a new daemon thread that executes
     * {@code target.run()}. This method, which may be called from any thread,
     * will return immediately its the caller.
     * @param target the object whose {@code run} method is invoked when this
     *               thread is started
     */
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();
    } // runNow
} // ApiApp
