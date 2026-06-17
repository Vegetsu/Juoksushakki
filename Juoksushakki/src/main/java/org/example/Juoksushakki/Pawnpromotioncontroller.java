package org.example.Juoksushakki;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Objects;

/*
Pawnpromotioncontrollerilla määritellään toiminnallisuus soturinkorotusikkunaan. Sen avulla soturinkorotusikkunassa eri
nappuloita painamalla soturi korotetaan valituksi nappulaksi.
 */
public class Pawnpromotioncontroller {

    private String selectedPiece;

    @FXML
    private ImageView lahettipanel;

    @FXML
    private ImageView queenpanel;

    @FXML
    private ImageView ratsupanel;

    @FXML
    private ImageView tornipanel;

    @FXML
    private Button Lahettibutton;

    @FXML
    private Button Ratsubutton;

    @FXML
    private Button Tornibutton;

    @FXML
    private Pane pawncontrollerpane;

    @FXML
    private Button queenbutton;



    /*
    Initialize-metodi asettaa heti soturinkorotusikkunan avauduttua eri nappuloille toiminnallisuuden, jonka avulla
    ohjelma tunnistaa, että minkä pelinappulan vastaavaa nappia ollaan painettu.
     */

    @FXML
    private void initialize(){
        queenbutton.setOnMouseClicked(e->{
            queenClicked();
        });
        Lahettibutton.setOnMouseClicked(e->{
            bishopClicked();
        });
        Ratsubutton.setOnMouseClicked(e->{
           knightClicked();
        });
        Tornibutton.setOnMouseClicked(e->{
            rookClicked();
        });

}


/*
Kun klikataan kuningatar-nappia, niin valitaan että soturi halutaan korottaa kuningattareksi ja soturinkorotusikkuna suljetaan.
 */
    @FXML
    private void queenClicked() {
        selectedPiece = "queen";
        closeWindow();
    }

/*
Metodi, jolla ikkuna suljetaan.
 */
    private void closeWindow() {
        Stage stage = (Stage) pawncontrollerpane.getScene().getWindow();
        stage.close();
    }


    /*
    Kun klikataan torni-nappia, niin valitaan että soturi halutaan korottaa torniksi ja soturinkorotusikkuna suljetaan.
     */
    @FXML
    private void rookClicked() {
        selectedPiece = "rook";
        closeWindow();
    }


    /*
Kun klikataan ratsu-nappia, niin valitaan että soturi halutaan korottaa ratsuksi ja soturinkorotusikkuna suljetaan.
 */
    @FXML
    private void knightClicked() {
        selectedPiece = "knight";
        closeWindow();
    }


    /*
Kun klikataan lähetti-nappia, niin valitaan että soturi halutaan korottaa lähetiksi ja soturinkorotusikkuna suljetaan.
 */
    @FXML
    private void bishopClicked() {
        selectedPiece = "bishop";
        closeWindow();
    }

    /*
    Lähettää tiedon Shakkilautacontrolleriin, että miksi nappulaksi soturi vaihdetaan.
     */
    public String getSelectedPiece() {
        return selectedPiece;
    }

    /*
    Asettaa oikean värisen kuvan soturinkorotusruutuun sen soturin värin mukaan, joka on korottumassa.
     */
    public void setColor(String color) {

        String prefix = color.equals("white") ? "w" : "b";

        queenpanel.setImage(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                        "/images/" + prefix + "-queen.png"))));

        tornipanel.setImage(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                        "/images/" + prefix + "-rook.png"))));

        lahettipanel.setImage(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                        "/images/" + prefix + "-bishop.png"))));

        ratsupanel.setImage(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                        "/images/" + prefix + "-knight.png"))));
    }

}
