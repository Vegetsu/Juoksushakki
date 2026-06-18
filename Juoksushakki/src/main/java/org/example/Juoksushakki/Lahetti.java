package org.example.Juoksushakki;

import javafx.scene.image.Image;

/*
Määrittelee lähetille ominaiset ehdot, käyttäen apuna nappuloille yleistä Piece luokkaa.
 */

public class Lahetti extends Piece {

    /*
    Asettaa läheteille niille kuuluvat värit.
     */
    public Lahetti(String color) {
        super(color);
    }

    /*
    Hakee läheteille oikean väriset kuvat käytettäväksi
     */
    @Override
    public Image getImage() {

        String path = color.equals("white")
                ? "/images/w-bishop.png"
                : "/images/b-bishop.png";

        return new Image(getClass().getResourceAsStream(path));
    }

    /*
    Määrittelee, että mitkä ovat laillisia liikkeitä lähetille.
     */
    @Override
    public boolean isValidMove(int r1, int c1, int r2, int c2, Piece[][] board) {

        if (Math.abs(r1 - r2) != Math.abs(c1 - c2)) {
            return false;
        }

        int rowStep = (r2 > r1) ? 1 : -1;
        int colStep = (c2 > c1) ? 1 : -1;

        int r = r1 + rowStep;
        int c = c1 + colStep;

        while (r != r2) {

            if (board[r][c] != null) {
                return false;
            }

            r += rowStep;
            c += colStep;
        }

        return true;
    }
}

