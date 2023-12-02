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


/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {
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
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

} // ApiApp
