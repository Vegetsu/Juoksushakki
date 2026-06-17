package org.example.Juoksushakki;



import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
Luokka, jonka tehtävänä on avata shakkilaudan päänäkymä. Se hakee shakkilaudan fxml-tiedoston ja
avaa tämän sitten ruudulle.
 */


public class Shakkilauta extends Application {
    private boolean turnLocked = false;
    @Override
    public void start(Stage stage) throws Exception {
        System.out.println(getClass().getResource("/org/example/Juoksushakki/Shakkilauta.fxml"));
        FXMLLoader fxmlLoader =
                new FXMLLoader(getClass().getResource("/org/example/Juoksushakki/Shakkilauta.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.setTitle("Juoksushakki");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        ShakkilautaController controller = fxmlLoader.getController();

        /*
        Kun painetaan valittuja nappuloita, niin niitä nappuloita vastaava pelaaja saa vuoron.
         */
        scene.setOnKeyPressed(event -> {

            if (controller.getGameState() != ShakkilautaController.GameState.TURN_SELECTION) {
                return;
            }

            switch (event.getCode()) {
                case Q -> {
                    controller.setWhiteTurn();
                    controller.startGame();
                    controller.updateTurnLabel();
                }

                case P -> {
                    controller.setBlackTurn();
                    controller.startGame();
                    controller.updateTurnLabel();
                }
            }
        });

    }

    public static void main(String[] args) {
        launch();
    }
}