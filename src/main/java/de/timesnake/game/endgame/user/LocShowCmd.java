/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.user;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.chat.Plugin;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.chat.Code;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;

import java.util.HashMap;

public class LocShowCmd implements CommandListener {

  private final Code perm = Plugin.END_GAME.createPermssionCode("endgame.location");
  private final Code locationNotExists = Plugin.END_GAME.createHelpCode("Location not exists");

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
    if (!sender.isPlayer(true)) {
      return;
    }

    if (!sender.hasPermission(this.perm)) {
      return;
    }

    User user = sender.getUser();

    if (!args.isLengthHigherEquals(1, true)) {
      user.resetSideboard();
      sender.sendTDMessageCommandHelp("Show location", "locshow <name>");
      return;
    }

    HashMap<Integer, Tuple<String, ExLocation>> locationsById = EndGameServer.getLocShowManager()
        .getLocationsById();

    Integer id = args.get(0).toIntOrExit(false);
    if (locationsById.get(id) == null) {
      sender.sendMessageNotExist(id.toString(), this.locationNotExists, "location");
      return;
    }

    Tuple<String, ExLocation> loc = locationsById.get(id);

    String name = loc.getA();
    if (name.length() >= 13) {
      name = name.substring(0, 13);
    }

    if (EndGameServer.isEnded() || !EndGameServer.getPlayingUsers().contains(user.getUniqueId())) {
      user.teleport(loc.getB());
    }

    EndGameServer.getLocShowManager().setTrackedLocation(user, id, name, loc.getB());
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.perm);
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
