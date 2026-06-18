package org.example.Juoksushakki;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

/*
Gameoverpopupcontroller-luokka toimii ohjaajana ikkunalle, joka aukeaa, kun peli loppuu. Se pitää sisällään metodeja, jotka toteuttava tämän ikkunan toiminnallisuuden.
 */
public class Gameoverpopupcontroller {
    @FXML
    private Text gameovertext;

    @FXML
    private Button newgamebutton;

    @FXML
    private BorderPane gameoverborderpane;

    private double xOffset;
    private double yOffset;

    private ShakkilautaModel board;

    private ShakkilautaController mainController;


    /*
    Initialize-metodi asettaa heti pelin päättymisikkunan avauduttua nämä ehdot.
     */
    @FXML
    private void initialize(){

        /*
        Näiden rivien avulla annetaan mahdollisuus liikuttaa pelin päättymisikkunaa.
         */
        gameoverborderpane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        gameoverborderpane.setOnMouseDragged(event -> {

            Stage stage =
                    (Stage) gameoverborderpane.getScene().getWindow();

            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        /*
        Asetetaan "Aloita uusi peli" -nappulalle toiminto, että uusi peli aloitetaan kun sitä painetaan.
         */
        newgamebutton.setOnMouseClicked(e->{
            startnewgame();
        });
    }

    /*
    Metodi, jonka avulla palautetaan shakkilauta alkuperäiseen tilanteeseen ja suljetaan pelin päättymisikkuna.
     */
    private void startnewgame() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Aloitusruutu.fxml"));

            Parent root = loader.load();

            Aloitusruutucontroller controller =
                    loader.getController();

            Stage mainStage =
                    (Stage) mainController.shakkilautalabel
                            .getScene()
                            .getWindow();

            controller.setStage(mainStage);

            mainStage.setScene(new Scene(root));
            mainStage.setTitle("Valitse pelaajien nimet");
            mainStage.centerOnScreen();
            mainStage.setMaximized(false);
            mainStage.setMaximized(true);
            closeWindow();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Metodi pelin päättymisikkunan sulkemiselle
     */
    private void closeWindow() {
        Stage stage = (Stage) gameoverborderpane.getScene().getWindow();
        stage.close();
    }

    /*
    Antaa tälle luokalle viittauksen Shakkilautamodel luokkaan, jotta peli voidaan aloittaa alusta
     */
    public void setModel(ShakkilautaModel board) {
        this.board = board;
    }

    /*
    Antaa viitteen Shakkilautacontroller luokkaan, jotta pelilauta voidaan nollata
     */
    public void setMainController(ShakkilautaController controller) {
        this.mainController = controller;
    }

    /*
    Kun peli loppuu, niin asetetaan pelin loppumisikkunan tekstiksi sen pelaajan väri, kumpi voitti.
     */
    public void gameovertext(String currentTurn) {
        if (currentTurn.equals("white")) {
            currentTurn = "musta";
            gameovertext.setText("Pelaaja "+currentTurn+" voitti!");
        }
        if (currentTurn.equals("black")) {
            currentTurn = "valkoinen";
            gameovertext.setText("Pelaaja "+currentTurn+" voitti!");
        }

    }

}


