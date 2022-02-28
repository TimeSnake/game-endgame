package de.timesnake.game.endgame.player;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Instrument;
import org.bukkit.Note;

import java.util.List;

public class LocCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.isPlayer(true)) {
            return;
        }

        if (!sender.hasPermission("endgame.location", 2408)) {
            return;
        }

        if (!args.isLengthHigherEquals(1, true)) {
            sender.sendMessageCommandHelp("Send location", "loc <name>");
            return;
        }

        ExLocation loc = sender.getUser().getExLocation();
        String name = args.toMessage();

        Server.broadcastClickableMessage(Server.getChat().getSenderMember(sender.getUser()) + ChatColor.PUBLIC + name + " " + ChatColor.VALUE + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ(), "/locshow " + name, "Click to save the location in the sideboard", ClickEvent.Action.RUN_COMMAND);
        Server.broadcastNote(Instrument.PLING, Note.natural(1, Note.Tone.C));

        EndGameServer.getLocShowManager().addLocation(name, loc.getExBlock().getLocation());
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.getLength() == 1) {
            return List.of("lava", "cave", "village", "portal");
        }
        return List.of();
    }
}
