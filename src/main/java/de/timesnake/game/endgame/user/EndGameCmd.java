/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.user;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.game.endgame.chat.Plugin;
import de.timesnake.game.endgame.server.EndGameMode;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.game.endgame.server.EndGameServerManager;
import de.timesnake.library.chat.Code;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import net.kyori.adventure.text.Component;

public class EndGameCmd implements CommandListener {

  private final Code perm = Plugin.END_GAME.createPermssionCode("endgame");
  private final Code resumePerm = Plugin.END_GAME.createPermssionCode("endgame.resume");
  private final Code pausePerm = Plugin.END_GAME.createPermssionCode("endgame.pause");
  private final Code resetPerm = Plugin.END_GAME.createPermssionCode("endgame.reset");
  private final Code modePerm = Plugin.END_GAME.createPermssionCode("endgame.mode");

  @Override
  public void onCommand(Sender sender, PluginCommand cmd,
      Arguments<Argument> args) {

    if (!args.isLengthHigherEquals(1, true)) {
      return;
    }

    if (args.get(0).equalsIgnoreCase("resume")) {
      if (!sender.hasPermission(this.resumePerm, true)) {
        return;
      }
      if (!EndGameServerManager.getInstance().isTimeRunning()) {
        EndGameServerManager.getInstance().resumeGame();
      }


    } else if (args.get(0).equalsIgnoreCase("pause")) {
      if (sender.hasPermission(this.pausePerm, true)) {
        if (EndGameServerManager.getInstance().isTimeRunning()) {
          EndGameServerManager.getInstance().pauseGame();
        }
      }

    } else if (args.get(0).equalsIgnoreCase("reset")) {
      if (sender.hasPermission(this.resetPerm, true)) {
        EndGameServerManager.getInstance().resetGame();
      }
    } else if (args.get(0).equalsIgnoreCase("mode")) {
      sender.hasPermissionElseExit(this.modePerm);

      if (!args.isLengthEquals(2, true)) {
        return;
      }

      if (EndGameMode.getNames().contains(args.get(1).toLowerCase())) {
        EndGameServer.setMode(EndGameMode.fromName(args.get(1).toLowerCase()));
      } else {
        sender.sendPluginMessage(Component.text("Unknown mode", ExTextColor.WARNING));
      }
    }
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion()
        .addArgument(new Completion(this.resumePerm, "resume"))
        .addArgument(new Completion(this.pausePerm, "pause"))
        .addArgument(new Completion(this.resetPerm, "reset"))
        .addArgument(new Completion(this.modePerm, "mode")
            .addArgument(new Completion(EndGameMode.getNames())));
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
