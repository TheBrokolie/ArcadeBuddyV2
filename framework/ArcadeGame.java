package framework;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Die zentrale Schnittstelle für ein Spiel, das im Arcade-Framework ausgeführt werden soll.
 * Eine Implementierung dieser Schnittstelle repräsentiert das eigentliche Spiel.
 */
public interface ArcadeGame {

    /**
     * Gibt die aktuelle Bestenliste (High Scores) des Spiels zurück.
     * Das Framework kann diese Liste zur Anzeige speichern oder persistieren.
     *
     * @return Eine {@code ArrayList} von {@link HighScore}-Objekten.
     */
    ArrayList<HighScore> getHighScores();

    /**
     * Startet das Spiel und gibt den Haupt-{@code JFrame} zurück, in dem das Spiel gezeichnet wird.
     * Diese Methode wird vom Framework einmal beim Start aufgerufen.
     *
     * @return Der Haupt-{@code JFrame} des Spiels.
     */
    JFrame start();

    /**
     * Stoppt die Spiel-Logik (z.B. den Spiel-Thread).
     * Diese Methode wird vom Framework beim Beenden des Spiels aufgerufen,
     * um Ressourcen freizugeben.
     */
    void stop();
}