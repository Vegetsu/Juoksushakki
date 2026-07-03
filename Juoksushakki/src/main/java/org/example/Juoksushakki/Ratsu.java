package org.example.Juoksushakki;

import javafx.scene.image.Image;

/*
Määrittelee ratsulle ominaiset ehdot, käyttäen apuna nappuloille yleistä Piece luokkaa.
 */

public class Ratsu extends Piece {


    /*
    Asettaa ratsuille niille kuuluvat värit.
     */
    public Ratsu(String color) {
        super(color);
    }


    /*
    Hakee ratsuille oikean väriset kuvat käytettäväksi
     */
    @Override
    public Image getImage() {

        String path = color.equals("white")
                ? "/images/w-knight.png"
                : "/images/b-knight.png";

        return new Image(getClass().getResourceAsStream(path));
    }


    /*
   Määrittelee, että mitkä ovat laillisia liikkeitä lähetille.
    */
    @Override
    public boolean isValidMove(int r1, int c1, int r2, int c2, Piece[][] board) {
        int ero = Math.abs(r1 - r2);
        int cero = Math.abs(c1 - c2);
        //System.out.println(r1 + " " + c1 + " " + r2 + " " + c2);
        return  (ero ==2 && cero ==1 || ero == 1 && cero ==2);}
}