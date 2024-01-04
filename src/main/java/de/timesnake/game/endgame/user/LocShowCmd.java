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
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import de.timesnake.library.chat.Code;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.UUID;

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
      sender.sendMessageCommandHelp(Component.text("Show location"),
          Component.text("locshow <name>"));
      return;
    }

    HashMap<UUID, Tuple<String, ExLocation>> locationsByName = EndGameServer.getLocShowManager()
        .getLocationsById();

    if (!args.get(0).isUUID(false)) {
      sender.sendPluginMessage(Component.text("Invalid location name ", ExTextColor.WARNING));
      return;
    }

    UUID uuid = UUID.fromString(args.getString(0));
    if (locationsByName.get(uuid) == null) {
      sender.sendMessageNotExist(uuid.toString(), this.locationNotExists, "location");
      return;
    }

    Tuple<String, ExLocation> loc = locationsByName.get(uuid);

    String name = loc.getA();
    if (name.length() >= 13) {
      name = name.substring(0, 13);
    }

    if (EndGameServer.isEnded() || !EndGameServer.getPlayingUsers().contains(user.getUniqueId())) {
      user.teleport(loc.getB());
    }

    EndGameServer.getLocShowManager().setTrackedLocation(user, name, loc.getB());
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
