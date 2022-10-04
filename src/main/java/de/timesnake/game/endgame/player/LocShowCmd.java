package de.timesnake.game.endgame.player;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;

public class LocShowCmd implements CommandListener {

    private Code.Permission perm;
    private Code.Help locationNotExists;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.isPlayer(true)) {
            return;
        }

        if (!sender.hasPermission(this.perm)) {
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
            sender.sendMessageNotExist(name, this.locationNotExists, "location");
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

    @Override
    public void loadCodes(Plugin plugin) {
        this.perm = plugin.createPermssionCode("end", "endgame.location");
        this.locationNotExists = plugin.createHelpCode("end", "Location not exists");
    }
}
