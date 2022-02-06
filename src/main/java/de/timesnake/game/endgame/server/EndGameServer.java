package de.timesnake.game.endgame.server;

import de.timesnake.basic.bukkit.util.Server;
import org.bukkit.Location;

import java.util.HashMap;
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

    public static void addLocation(String name, Location location) {
        server.addLocation(name, location);
    }

    public static HashMap<String, Location> getLocationsByName() {
        return server.getLocationsByName();
    }

    public static boolean isReset() {
        return server.isReset();
    }

    public static void setMode(EndGameMode mode) {
        server.setMode(mode);
    }
}
