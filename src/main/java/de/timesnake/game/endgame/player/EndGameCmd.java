package de.timesnake.game.endgame.player;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.database.util.object.TooLongEntryException;
import de.timesnake.game.endgame.server.EndGameMode;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.game.endgame.server.EndGameServerManager;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;

import java.util.List;

public class EndGameCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {

        if (!args.isLengthHigherEquals(1, true)) {
            return;
        }

        if (args.get(0).equalsIgnoreCase("password")) {
            if (!sender.isPlayer(true)) {
                return;
            }

            User user = sender.getUser();
            if (user.getUniqueId().equals((EndGameServerManager.getInstance().getOwner()))
                    || sender.hasPermission("endgame.password.other", 2404, false)) {
                sender.sendPluginMessage(ChatColor.WARNING + "You are not the server owner");
                return;
            }

            if (args.isLengthHigherEquals(3, false)) {
                sender.sendPluginMessage(ChatColor.WARNING + "Spaces are not allowed");
                return;
            }
            String password = args.get(1).getString();

            try {
                Server.setPassword(password);
            } catch (TooLongEntryException e) {
                sender.sendPluginMessage(ChatColor.WARNING + "Too long password, max length is 255.");
                return;
            }
            sender.sendPluginMessage(ChatColor.PERSONAL + "Updated password to " + ChatColor.VALUE + password);

        } else if (args.get(0).equalsIgnoreCase("resume")) {
            if (sender.isPlayer(false)) {
                User user = sender.getUser();
                if (user.getUniqueId().equals((EndGameServerManager.getInstance().getOwner()))
                        || sender.hasPermission("endgame.resume.other", 2407, false)) {
                    if (!EndGameServerManager.getInstance().isTimeRunning()) {
                        EndGameServerManager.getInstance().resumeGame();
                    }
                } else {
                    sender.sendPluginMessage(ChatColor.WARNING + "You are not the server owner");
                }
            } else {
                if (!EndGameServerManager.getInstance().isTimeRunning()) {
                    EndGameServerManager.getInstance().resumeGame();
                }
            }


        } else if (args.get(0).equalsIgnoreCase("pause")) {
            if (sender.isPlayer(false)) {
                User user = sender.getUser();
                if (user.getUniqueId().equals((EndGameServerManager.getInstance().getOwner())) || sender.hasPermission("endgame.pause.other", 2405, false)) {
                    if (EndGameServerManager.getInstance().isTimeRunning()) {
                        EndGameServerManager.getInstance().pauseGame();
                    }
                } else {
                    sender.sendPluginMessage(ChatColor.WARNING + "You are not the server owner");
                }
            } else {
                if (EndGameServerManager.getInstance().isTimeRunning()) {
                    EndGameServerManager.getInstance().pauseGame();
                }
            }

        } else if (args.get(0).equalsIgnoreCase("reset")) {
            if (sender.isPlayer(false)) {
                User user = sender.getUser();
                if (user.getUniqueId().equals((EndGameServerManager.getInstance().getOwner())) || sender.hasPermission("endgame.reset.other", 2406, false)) {
                    EndGameServerManager.getInstance().resetGame();
                } else {
                    sender.sendPluginMessage(ChatColor.WARNING + "You are not the server owner");
                }
            } else {
                EndGameServerManager.getInstance().resetGame();
            }
        } else if (args.get(0).equalsIgnoreCase("mode")) {
            if (!args.isLengthEquals(2, true)) {
                return;
            }

            if (EndGameMode.getNames().contains(args.get(1).toLowerCase())) {
                EndGameServer.setMode(EndGameMode.fromName(args.get(1).toLowerCase()));
            } else {
                sender.sendPluginMessage(ChatColor.WARNING + "Unknown mode");
            }
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.getLength() == 1) {
            return List.of("resume", "pause", "reset", "mode");
        }
        if (args.getLength() == 2 && args.get(0).equalsIgnoreCase("mode")) {
            return EndGameMode.getNames();
        }
        return List.of();
    }

}
