package org.example.Juoksushakki;

import javafx.scene.image.Image;


/*
Määrittelee soturille ominaiset ehdot, käyttäen apuna nappuloille yleistä Piece luokkaa.
 */
public class Soturi extends Piece {


    /*
    Asettaa sotureille niille kuuluvat värit.
     */
    public Soturi(String color) {
        super(color);
    }


    /*
    Hakee sotureille oikean väriset kuvat käytettäväksi
     */
    @Override
    public Image getImage() {

        String path = color.equals("white")
                ? "/images/w-pawn.png"
                : "/images/b-pawn.png";

        return new Image(getClass().getResourceAsStream(path));
    }


    /*
    Määrittelee, että mitkä ovat laillisia liikkeitä soturille.
     */
    @Override
    public boolean isValidMove(int r1, int c1, int r2, int c2, Piece[][] board) {


        Piece target = board[r2][c2];
        int direction = color.equals("white") ? -1 : +1;

        if (r2 == r1 + direction && Math.abs(c2 - c1) == 1) {
            return target != null && !target.getColor().equals(color);
        }

        if (c1 == c2 && r2 == r1 + direction) {
            return target == null;
        }

        if (c1 == c2 && ((color.equals("white") && r1 == 6) || (color.equals("black") && r1 == 1))) {
            int middleRow = r1 + direction;
            return (r2 == r1 + 2 * direction && board[middleRow][c1] == null && board[r2][c2] == null);
        }


        return false;


    }
}