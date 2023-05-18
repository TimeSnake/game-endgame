/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.SideboardBuilder;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.main.GameEndGame;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.basic.util.Tuple;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class LocShowManager {


  private final HashMap<UUID, Tuple<String, ExLocation>> locationsById = new HashMap<>();
  private final HashMap<User, Tuple<ExLocation, BukkitTask>> trackedLocationByUser = new HashMap<>();

  public void addLocation(UUID uuid, String name, ExLocation loc) {
    this.locationsById.put(uuid, new Tuple<>(name, loc));
  }

  public HashMap<UUID, Tuple<String, ExLocation>> getLocationsById() {
    return locationsById;
  }

  public void setTrackedLocation(User user, String name, ExLocation location) {
    if (this.trackedLocationByUser.containsKey(user)) {
      this.trackedLocationByUser.get(user).getB().cancel();
    }

    Sideboard sideboard = EndGameServer.getScoreboardManager()
        .registerSideboard(new SideboardBuilder()
            .name("endgameloc")
            .title("§6§lLocation")
            .setScore(6, "§cName: §f" + name)
            .setScore(5, "-------------------")
            .setScore(4, "§9X: §f" + location.getBlockX())
            .setScore(3, "§9Y: §f" + location.getBlockY())
            .setScore(2, "§9Z: §f" + location.getBlockZ()));
    user.setSideboard(sideboard);

    BukkitTask task = Server.runTaskTimerSynchrony(() -> {
      Location userLoc = user.getLocation();

      if (!userLoc.getWorld().equals(location.getWorld())) {
        sideboard.setScore(1, "§6§l  other world");
        sideboard.setScore(0, "§6    ");
        return;
      }

      Vector locVector = location.toVector().subtract(userLoc.toVector());

      String direction = "#";

      if (locVector.clone().setY(0).length() > 4) {
        double locAngle = Math.atan2(locVector.getZ(), locVector.getX());
        double userAngle = Math.atan2(userLoc.getDirection().getZ(),
            userLoc.getDirection().getX());

        double angle = userAngle - locAngle;

        while (angle > Math.PI) {
          angle = angle - 2 * Math.PI;
        }

        while (angle < -Math.PI) {
          angle = angle + 2 * Math.PI;
        }

        if (angle < -2.749 || angle >= 2.749) { // -7/8 pi
          direction = "⬇";
        } else if (angle < -1.963) { // -5/8 pi
          direction = "⬊";
        } else if (angle < -1.178) { // -3/8 pi
          direction = "➡";
        } else if (angle < -0.393) { // -1/8 pi
          direction = "⬈";
        } else if (angle < 0.393) { // 1/8 pi
          direction = "⬆";
        } else if (angle < 1.178) { // 3/8 pi
          direction = "⬉";
        } else if (angle < 1.963) { // 5/8 p
          direction = "⬅";
        } else if (angle < 2.749) { // 7/8 pi
          direction = "⬋";
        }
      }

      int heightDelta = location.getBlockY() - userLoc.getBlockY();

      sideboard.setScore(1, "§6§l     " + direction + "    ↕ §6" + heightDelta + "m");
      sideboard.setScore(0, "§6     " + ((int) userLoc.distance(location)));
    }, 0, 20, GameEndGame.getPlugin());

    this.trackedLocationByUser.put(user, new Tuple<>(location, task));
  }

  public void reset() {
    this.locationsById.clear();
    for (Tuple<ExLocation, BukkitTask> entry : this.trackedLocationByUser.values()) {
      entry.getB().cancel();
    }

    this.trackedLocationByUser.clear();
  }
}
