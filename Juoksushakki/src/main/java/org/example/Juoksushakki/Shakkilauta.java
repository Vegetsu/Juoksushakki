package org.example.Juoksushakki;



import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
Luokka, jonka tehtävänä on avata aloitusikkuna kun peli käynnistetään. Se hakee aloitusruudun fxml-tiedoston ja
avaa tämän sitten ruudulle.
 */

public class Shakkilauta extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/Juoksushakki/Aloitusruutu.fxml")
        );

        Scene scene = new Scene(loader.load());

        Aloitusruutucontroller controller = loader.getController();
        controller.setStage(stage);



        stage.setMinWidth(600);
        stage.setMinHeight(400);


        stage.setMaximized(true);

        stage.setScene(scene);
        stage.setTitle("Valitse pelaajien nimet:");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}