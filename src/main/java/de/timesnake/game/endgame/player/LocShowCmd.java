package de.timesnake.game.endgame.player;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.basic.util.cmd.Arguments;
import de.timesnake.library.basic.util.cmd.ExCommand;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;

public class LocShowCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.isPlayer(true)) {
            return;
        }

        if (!sender.hasPermission("endgame.location", 2408)) {
            return;
        }

        User user = sender.getUser();

        if (!args.isLengthHigherEquals(1, true)) {
            user.resetSideboard();
            sender.sendMessageCommandHelp("Show location", "locshow <name>");
            return;
        }

        HashMap<String, Location> locationsByName = EndGameServer.getLocationsByName();
        String name = args.toMessage();
        if (locationsByName.get(name) == null) {
            sender.sendMessageNotExist(name, 2503, "location");
            return;
        }

        Location loc = locationsByName.get(name);

        if (name.length() >= 13) {
            name = name.substring(0, 13);
        }

        Sideboard sideboard = EndGameServer.getScoreboardManager().registerNewSideboard("endgameloc", "§6§lLocation");
        sideboard.setScore(4, "§cName: §f" + name);
        sideboard.setScore(3, "-------------------");
        sideboard.setScore(2, "§9X: §f" + loc.getBlockX());
        sideboard.setScore(1, "§9Y: §f" + loc.getBlockY());
        sideboard.setScore(0, "§9Z: §f" + loc.getBlockZ());

        user.setSideboard(sideboard);
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }
}
