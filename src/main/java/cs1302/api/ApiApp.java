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
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;

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
    Stage stage;
    Scene scene;
    HBox root;
    Separator divider;

    VBox leftMenu;
    Button logo;
    Label directions;
    TextField searchBar;
    Button searchButton;

    VBox rightResult;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new HBox(8);

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

        rightResult = new VBox(5);
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        leftMenu.getChildren()
        .addAll(logo, directions, searchBar, searchButton);

        EventHandler<ActionEvent> searchClicked = (ActionEvent e) -> {
            this.getCoordinates("Athens");
        };
        searchButton.setOnAction(searchClicked);
    }
    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        
        /* demonstrate how to load local asset using "file:resources/"
        Image bannerImage = new Image("file:resources/readme-banner.png");
        ImageView banner = new ImageView(bannerImage);
        banner.setPreserveRatio(true);
        banner.setFitWidth(640);
        */

        // setup scene

        root.getChildren().addAll(leftMenu, divider, rightResult);
        scene = new Scene(root);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setMaxWidth(1280);
        stage.setMaxHeight(720);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    private void getCoordinates(String city) {
        city = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String coordQuery = String.format("city=%s", city);
        String coordUri = "https://api.api-ninjas.com/v1/geocoding?" + coordQuery;

        try {
            // build request
            HttpRequest request = HttpRequest.newBuilder()
                .header("X-Api-Key", "w4JHOP4Fc54IWr0it+xLzQ==UWRhEGsLsCuvrosC")
                .uri(URI.create(coordUri))
                .build();    
            
            // send request & recieve response
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            System.out.println(response.body());
            //ensure request is ok
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            directions.setText("Last attempt to get weather failed...");
        }

    }
} // ApiApp
