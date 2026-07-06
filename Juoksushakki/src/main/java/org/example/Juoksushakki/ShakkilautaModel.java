package org.example.Juoksushakki;


import java.util.ArrayList;
import java.util.List;


/*
Shakkilautamodel-luokka pitää sisällään shakkilaudalla tapahtuvaa toiminnallisuutta, kuten liikkeiden siirtoa ja
erikoistilanteiden tarkistamista.
 */
public class ShakkilautaModel {

    private List<String> siirtohistoria = new ArrayList<>();

    private Piece[][] board = new Piece[8][8];

    public ShakkilautaModel() {
        setupPieces();
    }

    public boolean historyAlreadyAdded;

    private String currentTurn ="";

    private int lastMoveRowFrom;
    private int lastMoveColFrom;
    private int lastMoveRowTo;
    private int lastMoveColTo;
    private Piece lastMovedPiece;

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;

    private boolean whiteRightRookMoved = false;
    private boolean whiteLeftRookMoved = false;

    private boolean blackRightRookMoved = false;
    private boolean blackLeftRookMoved = false;


    /*
    Metodi, jolla tehdään tavallinen siirto.
     */
    private void makeMove(int r1, int c1, int r2, int c2) {
        board[r2][c2] = board[r1][c1];
        board[r1][c1] = null;
    }

    /*
    Metodi, jolla tehdyt liikkeet voidaan kumota.
     */
    private void undoMove(int r1, int c1, int r2, int c2, Piece captured) {
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = captured;
    }

    /*
    Metodi, jolla asetetaan nappulat oikeille paikoilleen shakkilaudalla.
     */
    private void setupPieces() {
        for (int i = 0; i < 8; i++) {
            board[6][i] = new Sotilas("white");
        }

        for (int i = 0; i < 8; i++) {
            board[1][i] = new Sotilas("black");
        }

        board[7][0] = new Torni("white");
        board[7][7] = new Torni("white");

        board[0][0] = new Torni("black");
        board[0][7] = new Torni("black");

        board[7][2] = new Lahetti("white");
        board[7][5] = new Lahetti("white");

        board[0][2] = new Lahetti("black");
        board[0][5] = new Lahetti("black");

        board[7][1] = new Ratsu("white");
        board[7][6] = new Ratsu("white");

        board[0][1] = new Ratsu("black");
        board[0][6] = new Ratsu("black");

        board[7][4] = new Kuningas("white");
        board[0][4] = new Kuningas("black");

        board[7][3] = new Kuningatar("white");
        board[0][3] = new Kuningatar("black");

    }

    /*
    Metodi, jonka avulla saadaan tieto halutusta nappulasta.
     */
    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

/*
Metodi, jolla suoritetaan nappuloiden liikkumiseen vaikuttavia toimintoja.
 */
    public boolean movePiece(int r1, int c1, int r2, int c2) {
        historyAlreadyAdded = false;

        Piece source = board[r1][c1];
        if (source == null) return false;
        if (!source.getColor().equals(currentTurn)) return false;



/*
Tarkistetaan, onko potentiaalinen liike linnoitusliike, ja jos on niin suoritetaan liike ja lisätään se siirtohistoriaan.
Lopuksi vaihdetaan vuoro.
 */
        if (isCastlingMove(r1, c1, r2, c2)) {

            if (!canCastle(r1, c1, c2)) {
                return false;
            }

            performCastle(r1, c2);
            String color = source.getColor().equals("white") ? "Valkoinen" : "Musta";
            int moveNumber = siirtohistoria.size() + 1;
            historyAlreadyAdded = true;
            String move = moveNumber + ". " + color + " Kuningas: "
                    + (c2 > c1 ? "O-O" : "O-O-O");

            siirtohistoria.add(move);
            currentTurn = currentTurn.equals("white")
                    ? "black"
                    : "white";
            soundManager.play("linnoitus");
            return true;
        }

        /*
Tarkistetaan, onko potentiaalinen liike ohestalyöntiliike, ja jos on niin suoritetaan liike ja lisätään se siirtohistoriaan.
Lopuksi vaihdetaan vuoro.
 */
        if (isEnPassantMove(r1, c1, r2, c2)) {

            Piece sotilas = board[r1][c1];

            board[r2][c2] = sotilas;
            board[r1][c1] = null;


            int dir = sotilas.getColor().equals("white") ? 1 : -1;


            if (lastMoveRowTo == r2 + dir && lastMoveColTo == c2) {
                board[lastMoveRowTo][lastMoveColTo] = null;
            }

            sotilas.setHasMoved(true);

            String color = sotilas.getColor().equals("white") ? "Valkoinen" : "Musta";
            int moveNumber = siirtohistoria.size() + 1;
            historyAlreadyAdded = true;
            String move = moveNumber + ". " + color + " Sotilas: "
                    + "Ohestalyönti "
                    + toChessNotation(r1, c1)
                    + " × "
                    + toChessNotation(r2, c2);

            siirtohistoria.add(move);

            currentTurn = currentTurn.equals("white")
                    ? "black"
                    : "white";

            soundManager.play("ohestalyonti");
            return true;
        }

        /*
        Jos liike on taas normaali ja se on sallittu, niin suoritetaan liike.
         */

        if (!isValidMove(r1, c1, r2, c2)) return false;

        Piece target = board[r2][c2];

        makeMove(r1, c1, r2, c2);

        /*
        Asetetaan true ja false arvoja linnoituksessa mukana olevilla nappuloille, jotta niiden korostus toimii oikein
        linnoitustilanteissa.
         */
        if (source instanceof Kuningas) {
            if (source.getColor().equals("white")) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        }

        if (source instanceof Torni) {
            if (r1 == 7 && c1 == 7) whiteRightRookMoved = true;
            if (r1 == 7 && c1 == 0) whiteLeftRookMoved = true;

            if (r1 == 0 && c1 == 7) blackRightRookMoved = true;
            if (r1 == 0 && c1 == 0) blackLeftRookMoved = true;
        }

        source.setHasMoved(true);


/*
Vaihdetaan vuoro ja tallennetaan viimeisimmän siirron tiedot talteen.
 */
        currentTurn = currentTurn.equals("white") ? "black" : "white";


        lastMoveRowFrom = r1;
        lastMoveColFrom = c1;
        lastMoveRowTo = r2;
        lastMoveColTo = c2;
        lastMovedPiece = source;

        return true;
    }


    /*
    Tarkistaa, että onko siirto sallittu. Valittu ruutu ei saa olla tyhjä, kohdenappula ei saa olla oman värinen eikä
    lähtöruutu ja kohderuutu voi olla sama ruutu.
     */
    public boolean isValidMove(int r1, int c1, int r2, int c2) {


        Piece source = board[r1][c1];
        Piece target = board[r2][c2];

        if (source == null)
            return false;

        if (target != null &&
                source.getColor().equals(target.getColor()))
            return false;


        if (r1 == r2 && c1 == c2)
            return false;

        return source.isValidMove(
                r1,
                c1,
                r2,
                c2,
                board);

    }


/*
Kertoo, jos siirretty sotilas on sellaisessa tilanteessa, jossa sille on aihetta tarjota korotusta.
 */
    public boolean needsPromotion(int row, int col) {

        Piece piece = board[row][col];

        if (!(piece instanceof Sotilas)) {
            return false;
        }

        return (piece.getColor().equals("white") && row == 0)
                || (piece.getColor().equals("black") && row == 7);
    }

    /*
    Metodi, jonka avulla sotilas korotetaan. Pelaaja saa valinnan neljästä vaihtoehdosta, jonka jälkeen sotilas korotetaan
    valituksi nappulaksi.
     */
    public void promotePawn(
            int row,
            int col,
            String pieceType) {

        String color = board[row][col].getColor();

        switch (pieceType) {

            case "queen":
                board[row][col] =
                        new Kuningatar(color);
                break;

            case "rook":
                board[row][col] =
                        new Torni(color);
                break;

            case "bishop":
                board[row][col] =
                        new Lahetti(color);
                break;

            case "knight":
                board[row][col] =
                        new Ratsu(color);
                break;
        }
    }

    /*
    Metodi, jolla tarkastetaan, että onko jokin ruutu hyökkäysuhan alla. Käytetään linnoituksen tarkastuksessa.
     */
    private boolean squareUnderAttack(
            int row,
            int col,
            String color) {

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {

                Piece piece = board[r][c];

                if (piece != null &&
                        !piece.getColor().equals(color)) {

                    if (piece.isValidMove(
                            r,
                            c,
                            row,
                            col,
                            board)) {

                        return true;
                    }
                }
            }
        }

        return false;
    }

    /*
    Tarkastetaan, että onko yritettävä siirto linnoitussiirto. Siirrettävä nappula pitää olla kuningas ja sen pitää
    liikkua kaksi saraketta samalla rivillä.
     */
    public boolean isCastlingMove(
            int r1,
            int c1,
            int r2,
            int c2) {

        Piece piece = board[r1][c1];

        return piece instanceof Kuningas
                && r1 == r2
                && Math.abs(c2 - c1) == 2;
    }

    /*
    Tarkastetaan, että onko linnoitus sallittu liike.
     */
    public boolean canCastle(
            int r1,
            int c1,
            int c2) {

        Piece king = board[r1][c1];

        if (king.hasMoved())
            return false;


        /*
        Lyhyt linnoitus, torni ei saa olla liikkunut  ja kuninkaan ja tprnin välissä olevien ruutujen täytyy olla tyhjiä
        eivätkä ne saa olla hyökkäyksen uhan alla.
         */
        if (c2 == 6) {

            Piece rook = board[r1][7];

            if (!(rook instanceof Torni))
                return false;

            if (rook.hasMoved())
                return false;

            if (board[r1][5] != null)
                return false;

            if (board[r1][6] != null)
                return false;

            if (squareUnderAttack(
                    r1, 5,
                    king.getColor()))
                return false;

            if (squareUnderAttack(
                    r1, 6,
                    king.getColor()))
                return false;

            return true;
        }

        /*
        Pitkä linnoitus, torni ei saa olla liikkunut  ja kuninkaan ja tprnin välissä olevien ruutujen täytyy olla tyhjiä
        eivätkä ne saa olla hyökkäyksen uhan alla.
         */
        if (c2 == 2) {

            Piece rook = board[r1][0];

            if (!(rook instanceof Torni))
                return false;

            if (rook.hasMoved())
                return false;

            if (board[r1][1] != null)
                return false;

            if (board[r1][2] != null)
                return false;

            if (board[r1][3] != null)
                return false;

            if (squareUnderAttack(
                    r1, 3,
                    king.getColor()))
                return false;

            if (squareUnderAttack(
                    r1, 2,
                    king.getColor()))
                return false;

            return true;
        }

        return false;
    }

    /*
    Linnoitusliike, jos lyhyt siirto niin siirretään torni kuninkaan vasemmalle puolelle samalla kun kuningasta liikutetaan
    kaksi ruutua oikealle
     */
    private void performCastle(
            int row,
            int destinationCol) {

        if (destinationCol == 6) {

            Piece king = board[row][4];
            Piece rook = board[row][7];

            board[row][6] = king;
            board[row][5] = rook;

            board[row][4] = null;
            board[row][7] = null;

            king.setHasMoved(true);
            rook.setHasMoved(true);
        }

        /*
        Jos pitkä siirto niin siirretään torni kuninkaan oikealle puolelle samall kun kuningasta
        liikutetaan kolme ruutua vasemmalle.
        */
        else {

            Piece king = board[row][4];
            Piece rook = board[row][0];

            board[row][2] = king;
            board[row][3] = rook;

            board[row][4] = null;
            board[row][0] = null;

            king.setHasMoved(true);
            rook.setHasMoved(true);
        }

    }

    /*
Tarkistaa, että onko liike ohestalyöntiliike. Oman sotilaan täytyy olla liikkunut kolme ruutua lähtöruudustaan ja vastustajan
sotilaan täytyy hypätä kaksi ruutua eteenpäin oman sotilaan viereen. Sitten tarkastetaan, että vastustajan sotilaan takana
on tyhjä liike, ja liike suoritetaan.
     */
    public boolean isEnPassantMove(int r1, int c1, int r2, int c2) {

        Piece piece = board[r1][c1];

        if (!(piece instanceof Sotilas)) {
            return false;
        }

        Piece target = board[r2][c2];


        if (target != null) {
            return false;
        }


        Piece last = lastMovedPiece;

        if (!(last instanceof Sotilas)) {
            return false;
        }


        if (Math.abs(lastMoveRowFrom - lastMoveRowTo) != 2) {
            return false;
        }
        if (r1 == lastMoveRowTo &&
                Math.abs(c1 - lastMoveColTo) == 1 &&
                c2 == lastMoveColTo &&
                r2 == lastMoveRowTo +
                        (piece.getColor().equals("white") ? -1 : 1)) {

            return true;
        }

        return false;
    }

    /*
    Metodi, jonka avulla peli voidaan resetoida. Ruutu palautetaan alkuasetelmaan, tallennetut tiedot nollataan ja
    siirtohistoria tyhjennetään.
     */
    public void resetGame() {

        board = new Piece[8][8];
        currentTurn = "white";


        lastMoveRowFrom = -1;
        lastMoveColFrom = -1;
        lastMoveRowTo = -1;
        lastMoveColTo = -1;
        lastMovedPiece = null;
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRightRookMoved = false;
        whiteLeftRookMoved = false;
        blackRightRookMoved = false;
        blackLeftRookMoved = false;
        siirtohistoria.clear();
        setupPieces();
    }

    /*
    Metodi, jolla saadaan sen pelaajan väri, jonka vuoro parhaillaan on menossa.
     */
    public String getCurrentTurn() {
        return currentTurn;
    }


/*
Palauttaa lailliset siirrot katselun alla olevalle nappulalle. Liikkeet simuloidaan ja mahdolliset liikkeet laitetaan
muistiin listaan.
 */
    public List<int[]> getLegalMoves(int row, int col) {

        List<int[]> moves = new ArrayList<>();

        Piece piece = board[row][col];
        if (piece == null) return moves;

        String color = piece.getColor();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {


                Piece from = board[row][col];
                Piece to = board[r][c];

                board[r][c] = from;
                board[row][col] = null;


                board[row][col] = from;
                board[r][c] = to;


                if (isValidMove(row, col, r, c)) {
                    moves.add(new int[]{r, c});
                }


            }
        }

        /*
        Jos kyseinen nappula on sotilas, niin tarkastetaan lisäksi, että onko ohestalyöntiliike sille mahdollinen.
         */
        if (piece instanceof Sotilas) {

            int direction = color.equals("white") ? -1 : 1;

            if (canEnPassant(row, col,
                    row + direction,
                    col - 1)) {

                moves.add(
                        new int[]{row + direction, col - 1});
            }

            if (canEnPassant(row, col,
                    row + direction,
                    col + 1)) {

                moves.add(
                        new int[]{row + direction, col + 1});
            }
        }

        /*
        Jos kyseinen nappula on kuningas, niin tarkastetaan lisäksi, että onko linnoitus sille mahdollinen.
         */


        if (piece instanceof Kuningas) {

            if (color.equals("white") && row == 7 && col == 4) {

                if (canCastleKingside(color)) {
                    moves.add(new int[]{7, 6});
                }

                if (canCastleQueenside(color)) {
                    moves.add(new int[]{7, 2});
                }
            }

            if (color.equals("black") && row == 0 && col == 4) {

                if (canCastleKingside(color)) {
                    moves.add(new int[]{0, 6});
                }

                if (canCastleQueenside(color)) {
                    moves.add(new int[]{0, 2});
                }
            }
        }
        return moves;
    }

    /*
    Tarkistaa, että onko ohestalyöntiliike mahdollista suorittaa. Vastustajan viimeksi siirretty nappula täytyy olla sotilas
    ja sen on täytynyt juuri olla liikkunut 2 ruutua ja sen täytyy sijaita viereisessä sarakkeessa.
     */
    public boolean canEnPassant(int r1, int c1, int r2, int c2) {

        Piece piece = board[r1][c1];

        if (!(piece instanceof Sotilas))
            return false;

        if (!(lastMovedPiece instanceof Sotilas))
            return false;


        if (Math.abs(lastMoveRowFrom - lastMoveRowTo) != 2)
            return false;


        if (Math.abs(lastMoveColTo - c1) != 1)
            return false;

        if (piece.getColor().equals("white")) {

            return r1 == 3 &&
                    r2 == 2 &&
                    c2 == lastMoveColTo;
        }

        return r1 == 4 &&
                r2 == 5 &&
                c2 == lastMoveColTo;

    }

    /*
Tarkistaa, että voiko kuninkaan puolelle suorittaa lyhyen linnoituksen. Samat tarkastukset kuin mitä aiemmin on jo kerrottu
linnoitukseen liittyen.
     */


    public boolean canCastleKingside(String color) {

        if (color.equals("white")) {

            if (whiteKingMoved || whiteRightRookMoved)
                return false;


            if (board[7][4] == null || !(board[7][4] instanceof Kuningas))
                return false;

            if (board[7][5] != null || board[7][6] != null)
                return false;

            if (isSquareAttacked(7,4,"black")) return false;
            if (isSquareAttacked(7,5,"black")) return false;
            if (isSquareAttacked(7,6,"black")) return false;

            return true;
        }


        if (blackKingMoved || blackRightRookMoved)
            return false;

        if (board[0][4] == null || !(board[0][4] instanceof Kuningas))
            return false;

        if (board[0][5] != null || board[0][6] != null)
            return false;

        if (isSquareAttacked(0,4,"white")) return false;
        if (isSquareAttacked(0,5,"white")) return false;
        if (isSquareAttacked(0,6,"white")) return false;

        return true;
    }

    /*
Tarkistaa, että voiko kuningattaren puolelle suorittaa pitkän linnoituksen. Samat tarkastukset kuin mitä aiemmin on jo kerrottu
linnoitukseen liittyen.
     */
    public boolean canCastleQueenside(String color) {

        if (color.equals("white")) {

            if (whiteKingMoved || whiteLeftRookMoved)
                return false;

            if (board[7][1] != null ||
                    board[7][2] != null ||
                    board[7][3] != null)
                return false;

            if (isSquareAttacked(7,4,"black"))
                return false;

            if (isSquareAttacked(7,3,"black"))
                return false;

            if (isSquareAttacked(7,2,"black"))
                return false;

            return true;
        }

        if (blackKingMoved || blackLeftRookMoved)
            return false;

        if (board[0][1] != null ||
                board[0][2] != null ||
                board[0][3] != null)
            return false;

        if (isSquareAttacked(0,4,"white"))
            return false;

        if (isSquareAttacked(0,3,"white"))
            return false;

        if (isSquareAttacked(0,2,"white"))
            return false;

        return true;
    }

    /*
    Tarkistaa, että onko jokin ruutu hyökkäyksen uhan alla.
     */
    public boolean isSquareAttacked(int row,
                                    int col,
                                    String attackerColor) {

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {

                Piece piece = board[r][c];

                if (piece != null &&
                        piece.getColor().equals(attackerColor)) {

                    if (piece.isValidMove(
                            r,
                            c,
                            row,
                            col,
                            board)) {

                        return true;
                    }
                }
            }
        }

        return false;
    }



    /*
    Metodi, jonka avulla palautetaan siirtohistoria.
     */
    public List<String> getSiirtohistoria() {
        return siirtohistoria;
    }

    /*
    Muuntaa taulukkoindeksin shakkinotaatioksi niin, että esim (0,0) ruutu muuttuu muotoon a8.
     */
    private String toChessNotation(int row, int col) {

        char file = (char) ('a' + col);
        int rank = 8 - row;

        return file + String.valueOf(rank);
    }

    /*
    Metodi, jonka avulla tehty liike lisätään siirtohistoriaan. Normaali liikkeen merkkaustapa.
     */
    public void addMoveToHistory(
            int r1,
            int c1,
            int r2,
            int c2,
            Piece piece,
            String captureSymbol,
            String capturedName
    ) {
        String color = piece.getColor().equals("white") ? "Valkoinen" : "Musta";
        String pieceName = piece.getClass().getSimpleName();

        String from = toChessNotation(r1, c1);
        String to = toChessNotation(r2, c2);

        int moveNumber = siirtohistoria.size()+ 1;

        String move = moveNumber + ". "
                + color + " "
                + pieceName + ": "
                + from + captureSymbol + to + capturedName;

        siirtohistoria.add(move);


/*
 Metodi, jonka avulla tehty liike lisätään siirtohistoriaan. Käytetään erikoisliikkeiden merkkaamiseen.
 */
    }
    public void addMoveToHistorylong(
            int r1,
            int c1,
            int r2,
            int c2,
            Piece piece,
            String captureSymbol,
            String capturedName,
            String promotion
    ) {

        String color = piece.getColor().equals("white") ? "Valkoinen" : "Musta";
        String pieceName = piece.getClass().getSimpleName();

        String from = toChessNotation(r1, c1);
        String to = toChessNotation(r2, c2);

        int moveNumber = siirtohistoria.size()+ 1;

        String move = moveNumber + ". "
                + color + " "
                + pieceName + ": "
                + from + captureSymbol + to + capturedName
                + promotion;

        siirtohistoria.add(move);
    }

    /*
    Tarkastaa, että onko siirto jo lisätty historiaan.
     */
    public boolean isHistoryAlreadyAdded() {
        return historyAlreadyAdded;
    }

    /*
    Kertoo, kumman pelaajan vuoro on valittu.
     */
    public void setCurrentTurn(String turn) {
        this.currentTurn = turn;
    }
    public boolean isGameOver() {

        boolean whiteKingFound = false;
        boolean blackKingFound = false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {

                Piece piece = board[r][c];

                if (piece instanceof Kuningas) {

                    if (piece.getColor().equals("white")) {
                        whiteKingFound = true;
                    } else {
                        blackKingFound = true;
                    }
                }
            }
        }

        return !whiteKingFound || !blackKingFound;
    }

    /*
    Annetaan Shakkilautamodelille yhteys Soundmanageriin, jonka avulla soitetaan ääniefektejä.
     */
    public SoundManager soundManager = new SoundManager();
    }







