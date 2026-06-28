package org.example.Juoksushakki;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
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
    private MouseButton activeButton;

    private Robot robot;

    private boolean lockActive = false;
    private double lockX;
    private double lockY;

    private boolean timerRunning = false;
    private Timeline stealCountdown;
    private int secondsLeft;
    private boolean lockedButton = false;
    private boolean stealCooldown = false;
    private Timeline stealCooldownTimer;
    private String lastMoveColor = null;

    private Rectangle[][] overlays = new Rectangle[8][8];
    private PauseTransition resizePause = new PauseTransition(Duration.millis(80));
    StackPane boardContainer = new StackPane();
    Pane lineLayer = new Pane();

    List<Move> moves = new ArrayList<>();
    /*
    Kertoo mitä tapahtuu heti, kun shakkilauta avataan. Tekee uuden shakkilautamallin, luo shakkilaudan ja päivittää sen
    tiedot oikeaksi. Määrittää myös
     */
   @FXML
   public void initialize() {
       gridPane.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> redrawLines());
       lineLayer.setPickOnBounds(false);
       System.out.println("boardContainer children: " + boardContainer.getChildren());
       System.out.println("gridPane parent: " + gridPane.getParent());
       System.out.println("lineLayer parent: " + (lineLayer != null ? lineLayer.getParent() : "null"));
       gridPane.setMouseTransparent(true);

       try {
           robot = new Robot();
       } catch (AWTException e) {
           e.printStackTrace();
       }

       /*
       Määrittelee, että millä tavalla ajastin toimii. Laskee sekunnin kerrallaan alaspäin, ja jos aika laskee nollaan,
       niin vaihdetaan pelaajan vuoroa.
        */
       stealCountdown = new Timeline(
               new KeyFrame(Duration.seconds(1), e -> {

                   secondsLeft--;

                   shakkilautalabel.setText(
                           "Vuoro vaihtuu automaattisesti: " + secondsLeft + "s"
                   );

                   if (secondsLeft <= 0) {
                        soundManager.stop("clock");
                       stealCountdown.stop();

                       if (board.getCurrentTurn().equals("white")) {
                           System.out.println(board.getCurrentTurn());
                           setBlackTurn();
                           activeButton = MouseButton.SECONDARY;
                       } else if (board.getCurrentTurn().equals("black")) {
                           setWhiteTurn();
                           activeButton = MouseButton.PRIMARY;
                       }

                       lockedButton = false;
                       timerRunning = false;
                       startStealCooldown();

                       selectedRow = -1;
                       selectedCol = -1;

                       clearHighlights();
                       highlightAllMoves();
                       updateTurnLabel();
                       updateUI();
                   }
               })
       );

       stealCountdown.setCycleCount(Timeline.INDEFINITE);


       board = new ShakkilautaModel();

       createBoardUI();

       /*
       Määrittelee viivojen piirtämistä edellisten siirtojen merkkaamiseksi
        */
       ChangeListener<Number> resizeListener = (obs, oldV, newV) -> scheduleRedraw();

       gridPane.widthProperty().addListener(resizeListener);
       gridPane.heightProperty().addListener(resizeListener);
       lineLayer = new Pane();
       lineLayer.setMouseTransparent(true);
       lineLayer.setStyle("-fx-background-color: transparent;");

       gridPane.getChildren().add(lineLayer);
       System.out.println(boardContainer.getChildren());
       updateUI();

       setupResponsiveLabels();
       startMouseLockLoop();

       Platform.runLater(() -> {
           enableMouseLock();
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
ruudun väri ja shakkinappulan kuva. Rakennetaan myös overlay-kerros, jonka avulla korostuksia on helpompi käsitellä.
 */

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                StackPane square = new StackPane();
                Rectangle overlay = new Rectangle();
                overlay.setMouseTransparent(true);
                overlay.setFill(Color.TRANSPARENT);
                overlay.setManaged(false);
                overlay.setMouseTransparent(true);
                square.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                    overlay.setWidth(newVal.getWidth());
                    overlay.setHeight(newVal.getHeight());
                });
                square.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                GridPane.setHgrow(square, Priority.ALWAYS);
                GridPane.setVgrow(square, Priority.ALWAYS);

                String color = (row + col) % 2 == 0 ? "#f0d9b5" : "#b58863";
                square.setStyle("-fx-background-color: " + color + ";");
                square.getChildren().add(overlay);
                overlays[row][col] = overlay;
                final int r = row;
                final int c = col;
                square.setOnMouseClicked(e -> {

                    if (activeButton != null &&
                            e.getButton() != activeButton) {
                        return;
                    }

                    try {
                        handleSquareClick(r, c,e.getButton());
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
    private void handleSquareClick(int row, int col, MouseButton button) throws IOException {

        /*
        Jos peli ei ole käynnissä, niin ei tapahdu mitään.
         */
        if (gameState != GameState.GAME) {
            soundManager.play("error");
            return;
        }
/*
Ehtoja klikkaukselle, kuten ei hyväksytä klikkausta jos ruutu on tyhjä tai klikkaa väärän väristä nappulaa.
 */
        if (selectedRow == -1) {

            Piece nappula = board.getPiece(row, col);

            if (nappula == null) {
                soundManager.play("error");
                return;
            }

            if (!nappula.getColor().equals(board.getCurrentTurn())) {
                soundManager.play("error");
                return;
            }
            soundManager.play("pickup");
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
            if (capturedPiece != null) {
                soundManager.play("eat");
            }

            /*
            Kerätään tehdyt siirrot listaan, jotta niitä voidaan korostaa monta kerrallaan, jos sama pelaaja tekee monta
            siirtoa peräkkäin.
             */
            moves.add(new Move(fromRow, fromCol, toRow, toCol));
            Platform.runLater(() -> {
                drawMoveLine(fromRow, fromCol, toRow, toCol);
            });
            String moverColor = piece.getColor();
            stealCountdown.stop();
            timerRunning = false;
            lastFromRow = fromRow;
            lastFromCol = fromCol;
            lastToRow = toRow;
            lastToCol = toCol;
            lockedButton = false;
            HighlightHistory.add(new Move(fromRow, fromCol, toRow, toCol));
            String promotion = "";
            if (lastMoveColor != null && !lastMoveColor.equals(moverColor)) {
                clearLines();
                moves.add(new Move(fromRow, fromCol, toRow, toCol));
                Platform.runLater(() -> {
                    drawMoveLine(fromRow, fromCol, toRow, toCol);
                });
                HighlightHistory.clear();

                HighlightHistory.add(new Move(fromRow, fromCol, toRow, toCol));
            }
            lastMoveColor = moverColor;
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
                soundManager.play("victory");
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
                soundManager.play("promotion");

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
            enableMouseLock();

        }






                // vastustajan vuoro alkaa → clearataan kaikki vanhat korostukset
                clearHighlights();


            highlightAllMoves();


        selectedRow = -1;
        selectedCol = -1;


        /*
        Lopuksi päivitetään kaikki tarpeellinen tieto, kuten yläosan tekstin päivitys, käyttöliittymän
        päivitys sekä viimeisen siirron korotus.
         */
        highlightAllMoves();
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

            Rectangle overlay = overlays[r][c];

            squares[r][c].getChildren().removeIf(node -> node != overlay);

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

    overlays[row][col].setFill(Color.rgb(255, 255, 0, 0.5));
}

    /*
    Metodi, jolla tyhjennetään korostukset.
     */

    public void clearHighlights() {

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {

                overlays[r][c].setFill(Color.TRANSPARENT);

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

    for (int[] move : moves) {

        int r = move[0];
        int c = move[1];

        if (board.getPiece(r, c) == null) {
            overlays[r][c].setFill(Color.rgb(0, 255, 0, 0.25));
        } else {
            overlays[r][c].setFill(Color.rgb(255, 0, 0, 0.25));
        }
    }
}



    /*
    Apumetodi ruudun värin korostamiseen.
     */
    private void highlightSquare(int row, int col, Color color) {
        overlays[row][col].setFill(color);
    }



    /*
    Muuttaa shakkilautaikkunan yläossassa olevan tekstin sen mukaan, kumman pelaajan vuoro on tai onko neutraali
    vuoronvalintahetki.
     */

    public void updateTurnLabel() {

        if (gameState == GameState.TURN_SELECTION) {
            shakkilautalabel.setText(
                    whitePlayerName+" (Valkoinen): Paina valkoista pelinappia valitaksesi vuoron. "+ blackPlayerName+" (Musta): Paina mustaa pelinappia valitaksesi vuoron. Paina esc palataksesi takaisin vuoron valitsemiseen."
            );
            shakkilautalabel.setStyle(
                    "-fx-text-fill: black;" +
                            "-fx-background-color: #8b6f47;"
            );
            return;
        }
        if (timerRunning) {
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
        gridPane.setMouseTransparent(false);
        disableMouseLock();
    }

    /*
    Asettaa mustan pelaajan vuoron.
     */
    public void setBlackTurn() {
        board.setCurrentTurn("black");
        gridPane.setMouseTransparent(false);
        disableMouseLock();
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
        activeButton = null;
        moveMouseToMoveHistory();
        gridPane.setMouseTransparent(true);
        lockedButton = false;
        soundManager.play("move");
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
        clearHighlights();
        selectedRow = -1;
        selectedCol = -1;
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

    /*
    Asettaa aktiivisen pelaajalle kuuluvan napin olioon, jotta sitä voidaan käyttää muualla vertailussa.
     */
    public void setActiveButton(MouseButton button) {
        this.activeButton = button;
    }

    /*
    Metodi, jolla avataan pelinalkamisikkuna, kun shakkilautanäkymän vasemmassa yläkulmassa olevaa "Aloita uusi peli"
    -nappia painetaan "Uusi peli" -valikosta.
     */
    @FXML
    public void startNewGame() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Aloitusruutu.fxml"));

            Parent root = loader.load();

            Aloitusruutucontroller controller =
                    loader.getController();

            Stage mainStage =
                    (Stage) shakkilautalabel
                            .getScene()
                            .getWindow();

            controller.setStage(mainStage);

            mainStage.setScene(new Scene(root));
            mainStage.setTitle("Valitse pelaajien nimet");
            mainStage.centerOnScreen();
            mainStage.setMaximized(false);
            mainStage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Metodi, jolla hiiri pakotetaan siirtohistorian päälle ruutujen 4 ja 5 väliin, kun vuoro vaihtuu.
     */
    public void moveMouseToMoveHistory() {

        Bounds bounds = Siirtohistorialistview.localToScreen(
                Siirtohistorialistview.getBoundsInLocal()
        );

        double x = bounds.getMinX() + bounds.getWidth() / 2;


        double y = bounds.getMinY() + bounds.getHeight() * 0.443;

        robot.mouseMove((int) x, (int) y);
    }
    /*
    Metodi, jolla tämä hiiren lukitseminen määritellään.
     */
    private void startMouseLockLoop() {

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                if (!lockActive) return;

                PointerInfo info = MouseInfo.getPointerInfo();
                Point p = info.getLocation();


                if (Math.abs(p.x - lockX) > 5 || Math.abs(p.y - lockY) > 5) {
                    robot.mouseMove((int) lockX, (int) lockY);
                }
            }
        };

        timer.start();
    }
    /*
    Metodi, jota käytetään hiiren pakotettavan kohdan kokoaikaiseen päivittämiseen.
     */
    private void updateLockPoint() {

        Bounds bounds = Siirtohistorialistview.localToScreen(
                Siirtohistorialistview.getBoundsInLocal()
        );

        lockX = bounds.getMinX() + bounds.getWidth() / 2;
        lockY = bounds.getMinY() + bounds.getHeight() * 0.443;
    }

    /*
    Metodi, jolla hiiren lukitus laitetaan päälle.
     */
    public void enableMouseLock() {
        updateLockPoint();
        lockActive = true;

    }
    /*
    Metodi, jolla hiiren lukitus laitetaan pois päältä.
     */
    public void disableMouseLock() {
        lockActive = false;


    }

    /*
    Metodi, jolla vuoron ryöstö ajastin laitetaan päälle.
     */
    private void startStealCooldown() {

        stealCooldown = true;

        if (stealCooldownTimer != null) {
            stealCooldownTimer.stop();
        }

        stealCooldownTimer = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    stealCooldown = false;
                })
        );

        stealCooldownTimer.play();
    }

    /*
    Metodi, jolla vuoron ryöstö ajastin laitetaan pois päältä.
     */
    public void cancelStealTimer() {

        if (stealCountdown != null) {
            stealCountdown.stop();
            soundManager.stop("clock");
        }

        if (stealCooldownTimer != null) {
            stealCooldownTimer.stop();
            soundManager.stop("clock");
        }

        timerRunning = false;
        stealCooldown = false;
        secondsLeft = 10;
        lockedButton = false;

        updateTurnLabel();
    }

    /*
    Luokka, jolla siirtoon liittyviä arvoja kerätään talteen viime siirtojen korostukseen käytettävää listaa varten.
     */
    private static class Move {
        int fromRow, fromCol, toRow, toCol;

        Move(int fr, int fc, int tr, int tc) {
            this.fromRow = fr;
            this.fromCol = fc;
            this.toRow = tr;
            this.toCol = tc;
        }
    }

    /*
    Lista, johon kerätään saman pelaajan viimeisimmät siirrot, jotta ne voidaan myöhemmin korostaa.
     */
    private List<Move> HighlightHistory = new ArrayList<>();

    /*
    Metodi, jolla korostetaan viimeiset siirrot, keltaisella lähtöruutu ja päättymisruutu sinisellä.
     */
    public void highlightAllMoves() {

        for (Move m : HighlightHistory) {

            // lähtöruutu = keltainen
            highlightSquare(m.fromRow, m.fromCol,
                    Color.rgb(255, 215, 0, 0.0));

            // kohderuutu = sininen
            highlightSquare(m.toRow, m.toCol,
                    Color.rgb(135, 206, 235, 0.0));
        }
    }
    /*
    Metodi, jolla testataan, että onko olosuhteet oikeat vuoro ryöstö ajastimen käynnistykselle.
     */
    public void tryStartStealTimer(MouseButton button) {

        if (gameState != GameState.GAME) return;
        if (timerRunning || stealCooldown) return;
        if (activeButton == null) return;

        if (button == activeButton) return;

        timerRunning = true;
        secondsLeft = 5;
        soundManager.play("clock");
        shakkilautalabel.setText(
                "Vuoro vaihtuu automaattisesti: " + secondsLeft + "s"
        );
        stealCountdown.stop();
        stealCountdown.playFromStart();

        lockedButton = true;
    }

    /*
    Metodi, jolla poistetaan viimeisimmät korostukset.
     */
    private void resetLastmovehighlight(int r, int c) {

        if ((r + c) % 2 == 0) {
            squares[r][c].setStyle("-fx-background-color: #f0d9b5;");
        } else {
            squares[r][c].setStyle("-fx-background-color: #b58863;");
        }
    }
/*
Metodi, jolla piirretään viiva ja nuoli viivan päähän, jolla merkataan viimeisten siirtojen suuntaa.
 */
    public void drawMoveLine(int startRow, int startCol, int endRow, int endCol) {

        double cellW = squares[0][0].getWidth();
        double cellH = squares[0][0].getHeight();

        double startX = startCol * cellW + cellW / 2;
        double startY = startRow * cellH + cellH / 2;

        double endX = endCol * cellW + cellW / 2;
        double endY = endRow * cellH + cellH / 2;


        Line line = new Line(startX, startY, endX, endY);
        line.setStrokeWidth(4);


        Polygon arrowHead = new Polygon();

        arrowHead.getPoints().addAll(List.of(
                0.0, 0.0,
                -10.0, -6.0,
                -10.0, 6.0
        ));
        arrowHead.setFill(Color.RED);
        line.setStroke(Color.RED);

        double angle = Math.atan2(endY - startY, endX - startX);


        double offset = -7;

        arrowHead.setLayoutX(endX - Math.cos(angle) * offset);
        arrowHead.setLayoutY(endY - Math.sin(angle) * offset);
        Rotate rotate = new Rotate(
                Math.toDegrees(angle),
                0, 0
        );

        arrowHead.getTransforms().add(rotate);

        lineLayer.getChildren().addAll(line, arrowHead);
    }

/*
Metodi, jolla piirretään viimeisten siirtojen viivat uudelleen, jos ruudun kokoa muutetaan.
 */
    private void redrawLines() {

        Platform.runLater(() -> {

            double cellW = squares[0][0].getWidth();
            double cellH = squares[0][0].getHeight();

            if (cellW <= 1 || cellH <= 1) {
                return;
            }

            lineLayer.getChildren().clear();

            for (Move m : moves) {

                double startX = m.fromCol * cellW + cellW / 2;
                double startY = m.fromRow * cellH + cellH / 2;

                double endX = m.toCol * cellW + cellW / 2;
                double endY = m.toRow * cellH + cellH / 2;


                Line line = new Line(startX, startY, endX, endY);
                line.setStroke(Color.RED);
                line.setStrokeWidth(4);


                Polygon arrowHead = new Polygon();
                arrowHead.getPoints().addAll(
                        0.0, 0.0,
                        -10.0, -6.0,
                        -10.0, 6.0
                );

                double angle = Math.atan2(endY - startY, endX - startX);

                double offset = -7;

                arrowHead.setLayoutX(endX - Math.cos(angle) * offset);
                arrowHead.setLayoutY(endY - Math.sin(angle) * offset);

                arrowHead.getTransforms().add(
                        new Rotate(Math.toDegrees(angle), 0, 0)
                );

                arrowHead.setFill(Color.RED);


                lineLayer.getChildren().addAll(line, arrowHead);
            }
        });
    }

    /*
    Metodi, jolla viivojeen uudelleenpiirtämistä viivytetään hieman, jotta uudelleenpiirtämistä ei suoriteta kesken
    ikkunan koon muuttamisen.
     */
    private void scheduleRedraw() {

        resizePause.setOnFinished(e -> {
            if (isLayoutStable()) {
                redrawLines();
            }
        });

        resizePause.playFromStart();
    }

    /*
    Metodi, jolla katsotaan, että onko ikkuna paikallaan viivojen piirtämiseksi.
     */
    private boolean isLayoutStable() {

        double w = gridPane.getWidth();
        double h = gridPane.getHeight();

        return w > 10 && h > 10;
    }

    /*
    Metodi, jolla viivat poistetaan ruudulta, kun vuoro vaihtuu.
     */
    private void clearLines() {
        lineLayer.getChildren().clear();
        moves.clear();
    }

    /*
    Metodi, jolla annetaan yhteys käyttää ääniefektejä Soundmanager-luokasta.
     */
    public SoundManager soundManager = new SoundManager();
}