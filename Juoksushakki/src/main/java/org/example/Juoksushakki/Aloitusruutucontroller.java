package org.example.Juoksushakki;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;


/*
Luokka, joka asettaa toiminnallisuudet alkuikkunalle, jossa valitaan pelaajien nimet ja aloitetaan peli.
 */
public class Aloitusruutucontroller {
    @FXML
    private Button aloitusruutubutton;

    @FXML
    private TextField mustapelaajaTextfield;

    @FXML
    private TextField valkoinenpelaajaTextfield;


    private Stage stage;




    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /*
    Kun ohjelma käynnistetään, niin asetetaan pelin aloittamisnappi harmaaksi, kunnes molemmat pelaajat ovat valinneet
    nimensä.
     */
    @FXML
    public void initialize() {

        aloitusruutubutton.disableProperty().bind(
                valkoinenpelaajaTextfield.textProperty().isEmpty()
                        .or(mustapelaajaTextfield.textProperty().isEmpty())
        );
    }

    /*
    Kun painetaan pelin aloittamisnappia, niin haetaan pelaajien nimet shakkilaudan yläosaan käytettäväksi ja avataan
    shakkilautaikkuna.
     */
    @FXML
    private void startGame() throws IOException {

        String white = valkoinenpelaajaTextfield.getText();
        String black = mustapelaajaTextfield.getText();


            System.out.println(getClass().getResource("/org/example/Juoksushakki/Shakkilauta.fxml"));
            FXMLLoader fxmlLoader =
                    new FXMLLoader(getClass().getResource("/org/example/Juoksushakki/Shakkilauta.fxml"));

            Parent root = fxmlLoader.load();
        Platform.runLater(root::requestFocus);
            Scene scene = new Scene(root);
            stage.setTitle("Juoksushakki");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.show();



            ShakkilautaController controller = fxmlLoader.getController();
            controller.setPlayerNames(white, black);
            controller.soundManager.play("start");

            /*
            Metodi, joka tarkistaa, että jos toinen kuin nykyisen vuoron omaava pelaaja painaa omaa nappiaan ja jos näin
            on, käynnistetään ajastin.
             */
            root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                controller.tryStartStealTimer(event.getButton());
                if (controller.getGameState() != ShakkilautaController.GameState.TURN_SELECTION) {
                    return;
                }

                /*
            Asetetaan hiirelle toimintoja, eli kun painetaan vasenta hiiren nappia, niin on valkoisen pelaajan vuoro, ja
            kun painetaan hiiren oikeaa nappia, niin on mustan pelaajan vuoro. Kun painaa esc-painiketta, niin
            vuoronvalinta perutaan ja vuoro valitaan uudelleen.
             */
                if (event.getButton() == MouseButton.PRIMARY) {
                    controller.setWhiteTurn();
                    controller.setActiveButton(MouseButton.PRIMARY);
                    controller.startGame();
                    controller.updateTurnLabel();
                }

                if (event.getButton() == MouseButton.SECONDARY) {
                    controller.setBlackTurn();
                    controller.setActiveButton(MouseButton.SECONDARY);
                    controller.startGame();
                    controller.updateTurnLabel();
                }
            });


        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {

            if (e.getCode() == KeyCode.ESCAPE) {

                controller.disableMouseLock();
                controller.cancelStealTimer();
                controller.cancelTurnSelection();
                controller.updateTurnLabel();
                controller.clearHighlights();
                controller.highlightAllMoves();

                Platform.runLater(() -> {


                    scene.getRoot().requestFocus();


                    controller.enableMouseLock();
                });

                e.consume();
            }
        });
        }
    }

