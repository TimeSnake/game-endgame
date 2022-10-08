package de.timesnake.game.endgame.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.endgame.player.LocShowManager;

public class EndGameServer extends Server {

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

    private static final EndGameServerManager server = EndGameServerManager.getInstance();
}
