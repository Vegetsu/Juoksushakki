package org.example.Juoksushakki;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
Luokka, jonka avulla pelin siirtohistoria ja muut olennaiset tiedot tallennetaan erilliselle tekstitiedostolle. Tiedoston
löytää tämän ohjelman sisällä olevasta Pelit-kansiosta. Tiedoston nimi on muotoa Peli_2026-07-03_17-54-55.
 */
public class Pelintallennus {

    private BufferedWriter writer;

    /*
    Metodi, jolla määritellään tekstitiedoston tiedot, ja tehdään itse tiedoston luonti.
     */
    public void peliMuistiin(String whitePlayer, String blackPlayer) {
        System.out.println("startNewGame kutsuttu");
        try {

            File folder = new File("Pelit");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            LocalDateTime now = LocalDateTime.now();
            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            );
            String naytettavaAika = now.format(
                    DateTimeFormatter.ofPattern("d.M.yyyy HH:mm")
            );

            File file = new File(folder,
                    timestamp + "."+whitePlayer+" vs "+blackPlayer+".txt");

            writer = new BufferedWriter(new FileWriter(file));

            writer.write("Juoksushakki");
            writer.newLine();
            writer.write("Päivämäärä ja aika: " + naytettavaAika);
            writer.newLine();
            writer.write("Valkoinen: " + whitePlayer);
            writer.newLine();
            writer.write("Musta: " + blackPlayer);
            writer.newLine();
            writer.write("----------------------------------------");
            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Metodi, jolla shakkipelin liikkeet tallennetaan tekstitiedostoon.
     */
    public void muistiinLiike(String move) {

        if (writer == null) return;

        try {
            writer.write(move);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Metodi, jonka avulla tekstitiedostoon kirjoittaminen lopetetaan.
     */
    public void close() {

        if (writer == null) return;

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Metodi, jonka avulla kirjoitetaan tekstitiedoston loppuun se, miten peli päättyi.
     */
    public void kirjoitaLopputulos(String teksti) {

        if (writer == null) return;

        try {
            writer.write("----------------------------------------");
            writer.newLine();
            writer.write(teksti);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}