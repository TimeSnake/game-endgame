package de.timesnake.game.endgame.player;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.main.GameEndGame;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.basic.util.Tuple;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class LocShowManager {


    private final HashMap<String, ExLocation> locationsByName = new HashMap<>();
    private final HashMap<User, Tuple<ExLocation, BukkitTask>> trackedLocationByUser = new HashMap<>();

    public void addLocation(String name, ExLocation loc) {
        this.locationsByName.put(name, loc);
    }

    public HashMap<String, ExLocation> getLocationsByName() {
        return locationsByName;
    }

    public void setTrackedLocation(User user, String name, ExLocation location) {
        if (this.trackedLocationByUser.containsKey(user)) {
            this.trackedLocationByUser.get(user).getB().cancel();
        }

        Sideboard sideboard = EndGameServer.getScoreboardManager().registerNewSideboard("endgameloc", "§6§lLocation");
        sideboard.setScore(5, "§cName: §f" + name);
        sideboard.setScore(4, "-------------------");
        sideboard.setScore(3, "§9X: §f" + location.getBlockX());
        sideboard.setScore(2, "§9Y: §f" + location.getBlockY());
        sideboard.setScore(1, "§9Z: §f" + location.getBlockZ());
        user.setSideboard(sideboard);

        BukkitTask task = Server.runTaskTimerSynchrony(() -> {
            Location userLoc = user.getLocation();

            Vector locVector = location.toVector().subtract(userLoc.toVector());

            String direction = "#";

            if (locVector.length() > 8) {
                double locAngle = Math.atan2(locVector.getZ(), locVector.getX());
                double userAngle = Math.atan2(userLoc.getDirection().getZ(), userLoc.getDirection().getX());

                double angle = userAngle - locAngle;

                while (angle > Math.PI) {
                    angle = angle - 2 * Math.PI;
                }

                while (angle < -Math.PI) {
                    angle = angle + 2 * Math.PI;
                }

                if (angle < -2.749 || angle >= 2.749) { // -7/8 pi
                    direction = "↓";
                } else if (angle < -1.963) { // -5/8 pi
                    direction = "↘";
                } else if (angle < -1.178) { // -3/8 pi
                    direction = "→";
                } else if (angle < -0.393) { // -1/8 pi
                    direction = "↗";
                } else if (angle < 0.393) { // 1/8 pi
                    direction = "↑";
                } else if (angle < 1.178) { // 3/8 pi
                    direction = "↖";
                } else if (angle < 1.963) { // 5/8 p
                    direction = "←";
                } else if (angle < 2.749) { // 7/8 pi
                    direction = "↙";
                }
            }

            int heightDelta = location.getBlockY() - userLoc.getBlockY();

            sideboard.setScore(0, "§6§l     " + direction + "    ↕ §6" + heightDelta + "m");
        }, 0, 20, GameEndGame.getPlugin());

        this.trackedLocationByUser.put(user, new Tuple<>(location, task));
    }

    public void reset() {
        this.locationsByName.clear();
        for (Tuple<ExLocation, BukkitTask> entry : this.trackedLocationByUser.values()) {
            entry.getB().cancel();
        }

        this.trackedLocationByUser.clear();
    }
}
