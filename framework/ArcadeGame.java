package framework;

import javax.swing.*;
import java.util.ArrayList;

public interface ArcadeGame {
    ArrayList<HighScore> getHighScores();
    JFrame start();
    void stop();
}
