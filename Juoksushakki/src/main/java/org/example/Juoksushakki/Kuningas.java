package org.example.Juoksushakki;

import javafx.scene.image.Image;

/*
Määrittelee kuninkaalle ominaiset ehdot, käyttäen apuna nappuloille yleistä Piece luokkaa.
 */

public class Kuningas extends Piece {

    /*
    Asettaa kummallekin kuninkaalle niille kuuluvat värit.
     */
    public Kuningas(String color) {
        super(color);
    }

    /*
    Hakee kummallekin kuninkaalle oikean värisen kuvan käytettäväksi
     */
    @Override
    public Image getImage() {

        String path = color.equals("white")
                ? "/images/w-king.png"
                : "/images/b-king.png";

        return new Image(getClass().getResourceAsStream(path));
    }

    /*
    Määrittelee, että mitkä ovat laillisia liikkeitä kuninkaalle.
     */
    @Override
    public boolean isValidMove(int r1, int c1, int r2, int c2, Piece[][] board) {

        return (Math.abs(r1 - r2) <= 1 && Math.abs(c1 - c2) <=1);
    }
}
