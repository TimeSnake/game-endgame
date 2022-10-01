package de.timesnake.game.endgame.player;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

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
            sender.sendMessageCommandHelp(Component.text("Show location"), Component.text("locshow <name>"));
            return;
        }

        HashMap<String, ExLocation> locationsByName = EndGameServer.getLocShowManager().getLocationsByName();
        String name = args.toMessage();
        if (locationsByName.get(name) == null) {
            sender.sendMessageNotExist(name, 2503, "location");
            return;
        }

        ExLocation loc = locationsByName.get(name);

        if (name.length() >= 13) {
            name = name.substring(0, 13);
        }

        EndGameServer.getLocShowManager().setTrackedLocation(user, name, loc);
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }
}
