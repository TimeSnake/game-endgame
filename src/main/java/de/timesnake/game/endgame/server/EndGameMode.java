/*
 * game-endgame.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

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
