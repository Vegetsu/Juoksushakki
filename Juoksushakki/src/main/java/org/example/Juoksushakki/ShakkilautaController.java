package org.example.Juoksushakki;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/*
Luokka, joka pitää sisällään shakkilaudan muuttamiseen tarvittavat metodit, kuten itse laudan rakennuksen, sen
päivittämisen, ruutujen korostamisen eri väreillä ja mitä tapahtuu hiirellä klikkausten aikana.
 */
public class ShakkilautaController {

    @FXML
    private GridPane gridPane;

    @FXML
    public Label shakkilautalabel;

    @FXML
    private ListView<String> Siirtohistorialistview;

    @FXML
    private HBox shakkilautahbox;

    @FXML
    private VBox shakkilautavbox;

    @FXML
    private StackPane Shakkilautabottomstackpane;

    private int lastFromRow = -1;
    private int lastFromCol = -1;
    private int lastToRow = -1;
    private int lastToCol = -1;

    private static final int SIZE = 8;

    private StackPane[][] squares = new StackPane[SIZE][SIZE];

    private int selectedRow = -1;
    private int selectedCol = -1;

    private ShakkilautaModel board; // MODEL
    private List<int[]> highlightedMoves = new ArrayList<>();

    private GameState gameState = GameState.TURN_SELECTION;
    private String whitePlayerName = "Valkoinen";
    private String blackPlayerName = "Musta";
    /*
    Kertoo mitä tapahtuu heti, kun shakkilauta avataan. Tekee uuden shakkilautamallin, luo shakkilaudan ja päivittää sen
    tiedot oikeaksi.
     */
    /*public void initialize() {
        board = new ShakkilautaModel();

        createBoardUI();
        updateUI();
        Platform.runLater(this::setupResponsiveLabels);

    }*/
   public void initialize() {

        board = new ShakkilautaModel();

        createBoardUI();
        updateUI();

        setupResponsiveLabels();

        Platform.runLater(() -> {
            updateUI();
        });

    }

    /*
    Metodi, jolla rakennetaan shakkilauta.
     */
    private void createBoardUI() {

        /*
        Tyhjennetään aluksi kaikki ruudut jotta shakkilauta on varmasti tyhjä.
        */
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();


        /*
        Rakennetaan laudalle pohja niin, että kaikki ruudut ovat samankokoisia ja asetetaan ne valmiiksi tehtyyn 8X8 gridpaneen.
         */
        for (int i = 0; i < 9; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(11.11);
            gridPane.getColumnConstraints().add(cc);

            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(11.11);
            gridPane.getRowConstraints().add(rc);
        }

/*
Rakennetaan shakkilaudan päälle stackpanella ruudukko, koska sitä on helpompi käsitellä eri kerroksilla tietoa kuten
ruudun väri ja shakkinappulan kuva.
 */

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {


                StackPane square = new StackPane();

                square.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                GridPane.setHgrow(square, Priority.ALWAYS);
                GridPane.setVgrow(square, Priority.ALWAYS);

                String color = (row + col) % 2 == 0 ? "#f0d9b5" : "#b58863";
                square.setStyle("-fx-background-color: " + color + ";");

                final int r = row;
                final int c = col;


                /*
                Siirtologiikka, jonka avulla klikatusta ruudusta otetaan ruudun tiedot talteen myöhempää käyttöä varten
                lisäämällä se taulukkoon.
                 */
                square.setOnMouseClicked(e -> {
                    try {
                        handleSquareClick(r, c);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });


                squares[row][col] = square;
                gridPane.add(square, col, row);
            }
        }

    }


    /*
    Kertoo toiminnallisuuden sille, mitä tapahtuu kun ruutua klikataan.
    */
    private void handleSquareClick(int row, int col) throws IOException {

        /*
        Jos peli ei ole käynnissä, niin ei tapahdu mitään.
         */
        if (gameState != GameState.GAME) {
            return;
        }

/*
Ehtoja klikkaukselle, kuten ei hyväksytä klikkausta jos ruutu on tyhjä tai klikkaa väärän väristä nappulaa.
 */
        if (selectedRow == -1) {

            Piece nappula = board.getPiece(row, col);

            if (nappula == null) return;

            if (!nappula.getColor().equals(board.getCurrentTurn())) {
                return;
            }

            selectedRow = row;
            selectedCol = col;

            /*
            Korostetaan valittu ruutu ja mahdolliset siirrot.
             */
            highlightSquare(row, col);

            highlightedMoves = board.getLegalMoves(row, col);
            highlightSquares(highlightedMoves);

            return;
        }

        /*
        Asetetaan lähtöruutu ja kohderuutu
         */
        int fromRow = selectedRow;
        int fromCol = selectedCol;
        int toRow = row;
        int toCol = col;

        Piece piece = board.getPiece(fromRow, fromCol);
        Piece capturedPiece = board.getPiece(toRow, toCol);

        /*
        Nappulan nimi
         */
        String capturedName = (capturedPiece != null)
                ? " (" + capturedPiece.getClass().getSimpleName() + ")"
                : "";
        if (capturedName.equals(" (Lahetti)")){
            capturedName = " (Lähetti)";
        }

/*
Onko kyseessä syönti vai normaali liike.
 */
        String captureSymbol = (capturedPiece != null) ? " × " : " → ";

        /*
        Testataan siirtoa, ja jos se onnistuu, lähdetään päivittämään siirtohistoriaa.
         */
        boolean success = board.movePiece(fromRow, fromCol, toRow, toCol);

        if (success) {

            lastFromRow = fromRow;
            lastFromCol = fromCol;
            lastToRow = toRow;
            lastToCol = toCol;

            String promotion = "";

            /*
            Shakkilautamodelissa on jo valmiiksi metodit siirtohistorian päivittämiselle, jos ohestalyönti tai linnoitus
            tapahtuu, ja tämä metodi saa sen tiedon eikä lähde päivittämään siirtohistoriaa enää uudelleen.
             */
            if (board.isHistoryAlreadyAdded()) {

                updateMoveHistory();
            }
            /*
            Jos peli päättyy eli toisen joukkueen kuningas syödään, niin päivitetään tämä tieto siirtohistoriaan ja
            avataan pelinloppumisruutu.
             */
            else if (board.isGameOver()) {
                board.addMoveToHistory(
                        fromRow, fromCol,
                        toRow, toCol,
                        piece,
                        captureSymbol,
                        capturedName
                );
                updateMoveHistory();
                endTurn();
                avaagameover();
            }
            /*
            Jos tapahtuu sotilaan korotus, niin tallennetaan siirtohistoriaan, että mihin nappulatyyppiin sotilas
            päivitettiin.
             */
            else if (board.needsPromotion(toRow, toCol)) {

                Piece pawn = board.getPiece(toRow, toCol);

                String promotedTo = avaapawnpromotion(pawn.getColor());

                String promotionName = switch (promotedTo) {
                    case "queen" -> "Kuningatar";
                    case "rook" -> "Torni";
                    case "bishop" -> "Lähetti";
                    case "knight" -> "Ratsu";
                    default -> promotedTo;
                };

                board.promotePawn(toRow, toCol, promotedTo);

                promotion = " = " + promotionName;

                board.addMoveToHistorylong(
                        fromRow, fromCol,
                        toRow, toCol,
                        piece,
                        captureSymbol,
                        capturedName,
                        promotion
                );

                updateMoveHistory();
                 endTurn();

            }

            /*
            Tallennetaan normaalin siirron tiedot siirtohistoriaan.
             */
            else {
                board.addMoveToHistory(
                        fromRow, fromCol,
                        toRow, toCol,
                        piece,
                        captureSymbol,
                        capturedName
                );

            }
            updateMoveHistory();
            endTurn();

        }

/*
Jos toisen pelaajan kuningas syödään, niin peli loppuu.
 */

        clearHighlights();
        selectedRow = -1;
        selectedCol = -1;

        /*
        Shakkilaudan yläosassa oleva teksti päivittyy sen mukaan, kumman pelaajan vuoro on.
         */
        if (board.getCurrentTurn().equals("black")) {
            shakkilautalabel.setText("Pelaajan musta vuoro");
        }

        if (board.getCurrentTurn().equals("white")) {
            shakkilautalabel.setText("Pelaajan valkoinen vuoro");
        }

        /*
        Lopuksi päivitetään kaikki tarpeellinen tieto, kuten yläosan tekstin päivitys, käyttöliittymän
        päivitys sekä viimeisen siirron korotus.
         */
        highlightLastMove();
        updateTurnLabel();
        updateUI();


    }

/*
Käyttöliittymän päivitysmetodi, jolla päivitetään shakkilaudan näkymät, kuten kuvien oikea sijainti sekä ruuduissa
olevien nappuloiden sijainti.
 */
    private void updateUI() {

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {

                squares[r][c].getChildren().clear();


                Piece piece = board.getPiece(r, c);

                if (piece != null) {

                    ImageView img = new ImageView(piece.getImage());

                    img.fitWidthProperty().bind(
                            squares[r][c].widthProperty().multiply(0.8)
                    );

                    img.fitHeightProperty().bind(
                            squares[r][c].heightProperty().multiply(0.8)
                    );

                    img.setPreserveRatio(true);

                    squares[r][c].getChildren().add(img);
                }


            }
        }
    }

/*
Metodi, jolla korostetaan valittu shakkinappula.
 */
    private void highlightSquare(int row, int col) {
        System.out.println("Moves found: " + highlightedMoves.size());
        squares[row][col].setStyle("-fx-background-color: yellow;");
    }

    /*
    Metodi, jolla tyhjennetään korostukset.
     */
    public void clearHighlights() {

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {

                if ((r + c) % 2 == 0) {
                    squares[r][c].setStyle("-fx-background-color: #f0d9b5;");
                } else {
                    squares[r][c].setStyle("-fx-background-color: #b58863;");
                }
            }
        }
    }

    /*
   Metodi, joka avaa uuden sotilaankorotusikkunan ja asettaa sille halutut ehdot, kuten että sitä ei voi sulkea ruksista
   mutta sitä pystyy kuitenkin liikuttamaan pois shakkilaudan edestä.
     */
    private String avaapawnpromotion(String color) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Pawnpromotion.fxml"));

        Parent root = loader.load();

        Pawnpromotioncontroller controller =
                loader.getController();
        controller.setColor(color);
        Stage stage = new Stage();
        stage.setTitle("Korota sotilas");

        stage.initOwner(
                (Stage) shakkilautalabel.getScene().getWindow()
        );

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(root));
        stage.showAndWait();

        return controller.getSelectedPiece();

    }

    /*
    Metodi, jolla avataan pelin lopettamisikkuna. Muuttaa pelin loppumisikkunan tekstin sen mukaan, millä tilanteella peli
    loppui ja asettaa sille halutut ehdot, kuten että sitä ei voi sulkea ruksista mutta sitä pystyy kuitenkin
    liikuttamaan pois shakkilaudan edestä.
     */
    private void avaagameover() throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Gameoverpopup.fxml"));

        Parent root = loader.load();

        Gameoverpopupcontroller controller =
                loader.getController();

        controller.setModel(board);


        /*
        Jos peli loppuu, niin lähetetään voittajan nimi pelin päättymisikkunaan.
         */
        if (board.isGameOver()) {

            String winner = board.getCurrentTurn().equals("white")
                    ? getBlackPlayerName()+" (Musta)"
                    : getWhitePlayerName()+" (Valkoinen)";

            controller.gameovertext(winner);
        }

        Stage stage = new Stage();
        stage.setTitle("Peli päättyi!");
        stage.initOwner(
                (Stage) shakkilautalabel.getScene().getWindow()
        );

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        controller.setMainController(this);
        stage.setScene(new Scene(root));
        stage.showAndWait();


    }


/*
Korostaa kaikki mahdolliset siirrot, vihreällä mahdolliset siirrot ja punaisella jos siirrolla on mahdollista syödä
vastustajan nappula.
 */
    private void highlightSquares(List<int[]> moves) {
        System.out.println("Moves foundaaaaaaaaaaa: " + highlightedMoves.size());
        for (int[] move : moves) {

            int r = move[0];
            int c = move[1];

            Pane square = squares[r][c];

            if (board.getPiece(r, c) == null) {
                square.setStyle("-fx-background-color: rgba(0,255,0,0.3);");
            }
            else {
                square.setStyle("-fx-background-color: rgba(255,0,0,0.3);");
            }
        }
    }


/*
Korostaa viimeisen siirron, lähtöruutu vaaleansinisellä ja saapumisruutu vaaleankeltaisella.
 */
    public void highlightLastMove() {


        updateUI();
        highlightSquare(lastFromRow, lastFromCol, "rgba(135, 206, 235, 0.45)");
        highlightSquare(lastToRow, lastToCol, "rgba(255, 215, 0, 0.4)");
    }

    /*
    Apumetodi ruudun värin korostamiseen.
     */
    private void highlightSquare(int r, int c, String color) {

        if (r < 0 || c < 0) return;

        StackPane pane = squares[r][c];
        if (pane == null) return;

        pane.setStyle("-fx-background-color: " + color + ";");
    }



    /*
    Muuttaa shakkilautaikkunan yläossassa olevan tekstin sen mukaan, kumman pelaajan vuoro on tai onko neutraali
    vuoronvalintahetki.
     */

    public void updateTurnLabel() {
        if (gameState == GameState.TURN_SELECTION) {
            shakkilautalabel.setText(
                    whitePlayerName+" (Valkoinen): Paina hiiren vasenta nappia valitaksesi vuoron. "+ blackPlayerName+" (Musta): Paina hiiren oikeaa nappia valitaksesi vuoron. Paina esc palataksesi takaisin vuoron valitsemiseen."
            );
            shakkilautalabel.setStyle(
                    "-fx-text-fill: black;" +
                            "-fx-background-color: #8b6f47;"
            );
            return;
        }

        if (board.getCurrentTurn().equals("black")) {

            shakkilautalabel.setText("Pelaajan "+blackPlayerName+" (Musta) vuoro!");
            shakkilautalabel.setStyle(
                    "-fx-background-color: black; " +
                            "-fx-text-fill: white;"
            );

        } else {

            shakkilautalabel.setText("Pelaajan "+whitePlayerName+" (Valkoinen) vuoro!");
            shakkilautalabel.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-text-fill: black;"
            );
       }
    }

    /*
    Päivittää siirtohistorian.
     */
    private void updateMoveHistory() {

        Siirtohistorialistview.getItems().setAll(
                board.getSiirtohistoria()
        );
    }

    /*
    Asettaa valkoisen pelaajan vuoron.
     */
    public void setWhiteTurn() {
        board.setCurrentTurn("white");
    }

    /*
    Asettaa mustan pelaajan vuoron.
     */
    public void setBlackTurn() {
        board.setCurrentTurn("black");
    }

    /*
    Luo mahdolliset pelitilat jota käytetään siihen, että onko vuoronvalintahetki vai onko peli käynnissä.
     */
    public enum GameState {
        TURN_SELECTION,
        GAME
    }

/*
Vaihtaa pelitilan niin, että peli on käynnissä.
 */
    public void startGame() {
        gameState = GameState.GAME;
    }

    /*
    Vaihtaa pelitilan niin, että on vuoro päättyy ja tilanne muuttuu vuoronvalintatilanteeksi.
     */
    public void endTurn() {
        gameState = GameState.TURN_SELECTION;
    }

    /*
    Palauttaa nykyisen pelitilan.
     */
    public GameState getGameState() {
        return gameState;
    }

    /*
    Asettaa samanlaiset skaalaukset yhdeksännelle riville ja sarakkeelle
     */
    private void setupResponsiveLabels() {

        for (Node node : gridPane.getChildren()) {

            if (node instanceof Label label &&
                    label.getStyleClass().contains("coordinate-label")) {

                label.fontProperty().bind(
                        Bindings.createObjectBinding(
                                () -> Font.font(
                                        Math.min(gridPane.getWidth(), gridPane.getHeight()) / 20
                                ),
                                gridPane.widthProperty(),
                                gridPane.heightProperty()
                        )
                );
            }
        }
    }

    /*
    Metodi, jonka avulla tehdään pelaajan valinnan peruutus
     */
    public void cancelTurnSelection() {
        gameState = GameState.TURN_SELECTION;
        updateTurnLabel();
    }
    /*
    Asettaa pelaajien nimet shakkilaudan yläosaan, jotka haetaan pelaajien nimeämisikkunasta.
     */
    public void setPlayerNames(String white, String black) {
        this.whitePlayerName = white;
        this.blackPlayerName = black;
        updateTurnLabel();
    }

    public String getWhitePlayerName() {
        return whitePlayerName;
    }

    public String getBlackPlayerName() {
        return blackPlayerName;
    }
}