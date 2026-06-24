package org.example.Juoksushakki;

import javafx.scene.image.Image;

/*
Abstrakti luokka, jota nappulametodit käyttävät ominaisuuksiensa perimiseen. Sisältää nappuloiden  värin,
tiedon siitä onko se liikkunut, kuvan ja tiedon siitä, saako nappula liikkua.
 */

public abstract class Piece {

    protected String color;

    protected boolean hasMoved = false;

    public Piece(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public abstract Image getImage();

    public abstract boolean isValidMove(
            int r1,
            int c1,
            int r2,
            int c2,
            Piece[][] board);

}