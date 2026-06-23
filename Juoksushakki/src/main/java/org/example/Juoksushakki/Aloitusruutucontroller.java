package org.example.Juoksushakki;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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

            Scene scene = new Scene(root);
            stage.setTitle("Juoksushakki");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.show();



            ShakkilautaController controller = fxmlLoader.getController();
            controller.setPlayerNames(white, black);

            /*
            Asetetaan hiirelle toimintoja, eli kun painetaan vasenta hiiren nappia, niin on valkoisen pelaajan vuoro, ja
            kun painetaan hiiren oikeaa nappia, niin on mustan pelaajan vuoro. Kun painaa esc-painiketta, niin
            vuoronvalinta perutaan ja vuoro valitaan uudelleen.
             */
            root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {

                if (controller.getGameState() != ShakkilautaController.GameState.TURN_SELECTION) {
                    return;
                }

                if (event.getButton() == MouseButton.PRIMARY) {
                    controller.setWhiteTurn();
                    controller.startGame();
                    controller.updateTurnLabel();
                }

                if (event.getButton() == MouseButton.SECONDARY) {
                    controller.setBlackTurn();
                    controller.startGame();
                    controller.updateTurnLabel();
                }
            });

            root.setOnKeyPressed(event -> {

                if (event.getCode() == KeyCode.ESCAPE) {
                    controller.cancelTurnSelection();
                    controller.updateTurnLabel();
                    controller.clearHighlights();
                    controller.highlightLastMove();
                }
            });
        }
    }

