package de.timesnake.game.endgame.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.endgame.player.LocShowManager;

import java.util.UUID;

public class EndGameServer extends Server {

    private static final EndGameServerManager server = EndGameServerManager.getInstance();

    public static void resetGame() {
        server.resetGame();
    }

    public static void broadcastGameMessage(String message) {
        server.broadcastGameMessage(message);
    }

    public static UUID getOwner() {
        return server.getOwner();
    }

    public static String getOwnerName() {
        return server.getOwnerName();
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
}
