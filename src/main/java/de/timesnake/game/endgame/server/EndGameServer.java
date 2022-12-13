/*
 * workspace.game-endgame.main
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

import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.game.endgame.user.LocShowManager;
import de.timesnake.game.endgame.user.TablistManager;

public class EndGameServer extends GameServer {

    public static void resetGame() {
        server.resetGame();
    }

    @Deprecated
    public static void broadcastGameMessage(String message) {
        server.broadcastGameMessage(message);
    }

    public static void pauseGame() {
        server.pauseGame();
    }

    public static void resumeGame() {
        server.resumeGame();
    }

    public static boolean isTimeRunning() {
        return server.isTimeRunning();
    }

    public static void setTimeRunning(boolean run) {
        server.setTimeRunning(run);
    }

    public static boolean isReset() {
        return server.isReset();
    }

    public static void setMode(EndGameMode mode) {
        server.setMode(mode);
    }

    public static LocShowManager getLocShowManager() {
        return server.getLocShowManager();
    }

    public static TablistManager getTablistManager() {
        return server.getTablistManager();
    }

    private static final EndGameServerManager server = EndGameServerManager.getInstance();
}
