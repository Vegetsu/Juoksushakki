package org.example.Juoksushakki;

import javafx.scene.image.Image;

/*
Määrittelee kuningattarelle ominaiset ehdot, käyttäen apuna nappuloille yleistä Piece luokkaa.
 */

public class Kuningatar extends Piece {

    /*
    Asettaa kummallekin kuningattarelle niille kuuluvat värit.
     */
    public Kuningatar(String color) {
        super(color);
    }

    /*
    Hakee kummallekin kuningattarelle oikean värisen kuvan käytettäväksi
     */
    @Override
    public Image getImage() {

        String path = color.equals("white")
                ? "/images/w-queen.png"
                : "/images/b-queen.png";

        return new Image(getClass().getResourceAsStream(path));
    }


    /*
    Määrittelee, että mitkä ovat laillisia liikkeitä kuningattarelle.
     */
    @Override
    public boolean isValidMove(int r1, int c1, int r2, int c2, Piece[][] board) {

        if(!(r1 == r2 || c1 == c2 || Math.abs(r1 - r2) == Math.abs(c1 - c2))){
            return false;
        }

        if(r1 == r2){
            int step = (c2 > c1) ? 1 : -1;
            for(int i = c1+step; i != c2; i +=step){
                if (board[r1][i] != null) {
                    return false;
                }
            }

        }

        else if(c1 == c2){
            int step = (r2 > r1) ? 1 : -1;
            for(int i = r1+step; i != r2; i +=step){
                if (board[i][c1] != null) {
                    return false;
                }
            }

        }

        else{
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
}
        return true;
    }
}
