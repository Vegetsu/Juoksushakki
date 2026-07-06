package org.example.Juoksushakki;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/*
Luokka, joka käsittelee eri ääniefekteihin liittyviä toimintoja.
 */
public class SoundManager {

    private final Map<String, MediaPlayer> sounds = new HashMap<>();

    /*
    Kaikki pelissä käytettävät ääniefektit ja yhteys niihin käytettäväksi muille luokille.
     */
    public SoundManager() {
        load("move", "/sounds/freesound_community-ficha-de-ajedrez-34722_shortened.mp3");
        load("start", "/sounds/freesound_community-board-start-38127.mp3");
        load("victory", "/sounds/astralsynthesizer-11l-victory-1749704552668-358772_quiet.mp3");
        load("pickup", "/sounds/freesound_community-pick-92276_short.mp3");
        load("eat", "/sounds/freesound_community-eating-sound-effect-36186.mp3");
        load("promotion", "/sounds/freesound_community-video-game-powerup-38065_quiet.mp3");
        load("linnoitus", "/sounds/freesound_community-tada-fanfare-a-6313_quiet.mp3");
        load("clock", "/sounds/dragon-studio-clock-ticking-down-376897.mp3");
        load("ohestalyonti", "/sounds/dragon-studio-simple-whoosh-382724_quiet.mp3");
        load("error", "/sounds/soundshelfstudio-ui-error-pop-515668_quiet.mp3");




    }

    /*
    Ladataan ääniefektit käytettäväksi ohjelman äänikirjastosta.
     */
    private void load(String name, String path) {
        URL url = getClass().getResource(path);

        if (url == null) {
            System.out.println("Sound not found: " + path);
            return;
        }

        Media media = new Media(url.toExternalForm());
        MediaPlayer player = new MediaPlayer(media);

        sounds.put(name, player);
    }

    /*
    Metodi, jonka avulla valittu ääniefekti soitetaan.
     */
    public void play(String name) {
        MediaPlayer player = sounds.get(name);

        if (player == null) return;

        player.stop();
        player.play();
    }

    /*
    Metodi, jolla ääniefektin soittaminen lopetetaan.
     */
    public void stop(String name) {
        MediaPlayer player = sounds.get(name);

        if (player == null) return;

        player.stop();

    }
}
