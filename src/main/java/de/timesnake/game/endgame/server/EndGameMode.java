package de.timesnake.game.endgame.server;

import org.bukkit.Difficulty;

import java.util.ArrayList;
import java.util.List;

public enum EndGameMode {

    EASY("easy", Difficulty.EASY, false),
    NORMAL("normal", Difficulty.NORMAL, false),
    HARD("hard", Difficulty.HARD, false),
    ULTRA_HARD("ultra_hard", Difficulty.HARD, true);

    private final String name;
    private final Difficulty difficulty;
    private final boolean hardcore;

    EndGameMode(String name, Difficulty difficulty, boolean hardcore) {
        this.name = name;
        this.difficulty = difficulty;
        this.hardcore = hardcore;
    }

    public String getName() {
        return name;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public static List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (EndGameMode mode : EndGameMode.values()) {
            names.add(mode.getName());
        }
        return names;
    }

    public static EndGameMode fromName(String name) {
        for (EndGameMode mode : EndGameMode.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
