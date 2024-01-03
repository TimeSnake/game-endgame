/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.chat.Plugin;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import de.timesnake.library.extension.util.chat.Code;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Instrument;
import org.bukkit.Note;

import java.util.UUID;

public class LocCmd implements CommandListener {

  private final Code perm = Plugin.END_GAME.createPermssionCode("endgame.location");
  ;

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
    if (!sender.isPlayer(true)) {
      return;
    }

    if (!sender.hasPermission(this.perm)) {
      return;
    }

    if (!args.isLengthHigherEquals(1, true)) {
      sender.sendTDMessageCommandHelp("Send location", "loc <name>");
      return;
    }

    ExLocation loc = sender.getUser().getExLocation();
    String name = args.toMessage();
    UUID uuid = UUID.randomUUID();

    Server.broadcastMessage(Server.getChat().getSenderMember(sender.getUser())
        .append(Component.text(name, ExTextColor.PUBLIC))
        .append(Component.text(
            " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ(),
            ExTextColor.VALUE))
        .clickEvent(
            ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/locshow " + uuid))
        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
            Component.text("Click to save location in sideboard"))));
    Server.broadcastNote(Instrument.PLING, Note.natural(1, Note.Tone.C));

    EndGameServer.getLocShowManager().addLocation(uuid, name, loc.getExBlock().getLocation());
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.perm)
        .addArgument(new Completion("lava", "cave", "village", "portal", "hier"));
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
