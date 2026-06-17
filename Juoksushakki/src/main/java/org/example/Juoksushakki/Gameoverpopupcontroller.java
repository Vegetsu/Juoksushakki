package org.example.Juoksushakki;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/*
Gameoverpopupcontroller-luokka toimii ohjaajana ikkunalle, joka aukeaa, kun peli loppuu shakkimattiin tai
pattitilanteeseen. Se pitää sisällään metodeja, jotka toteuttava tämän ikkunan toiminnallisuuden.
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
    @FXML
    private void startnewgame() {
        board.resetGame();
        mainController.resetUI();
        closeWindow();
    }

    /*
    Metodi pelin päättymisikkunan sulkemiselle
     */
    private void closeWindow() {
        Stage stage = (Stage) gameoverborderpane.getScene().getWindow();
        stage.close();
    }

    /*
    Asettaa pelin päättymisikkunassa olevaksi viestiksi tämän, jos peli päättyy pattitilanteeseen.
     */
    public void stalematetext() {

            gameovertext.setText("Peli päättyi pattitilanteeseen!");


    }

    /*
    Asettaa pelin päättymisikkunassa olevaksi viestiksi jommankumman näistä riippuen, kumpi pelaaja saa shakkimatin.
     */
    public void checkmatetext(String currentTurn) {
        if (currentTurn.equals("white")) {
            currentTurn = "musta";
            gameovertext.setText("Peli päättyi pelaajan "+currentTurn+" shakkimattiin!");
        }
        if (currentTurn.equals("black")) {
            currentTurn = "valkoinen";
            gameovertext.setText("Peli päättyi pelaajan "+currentTurn+" shakkimattiin!");
        }

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

}


