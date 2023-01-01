/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Instrument;
import org.bukkit.Note;

import java.util.List;
import java.util.UUID;

public class LocCmd implements CommandListener {

    private Code.Permission perm;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.isPlayer(true)) {
            return;
        }

        if (!sender.hasPermission(this.perm)) {
            return;
        }

        if (!args.isLengthHigherEquals(1, true)) {
            sender.sendMessageCommandHelp("Send location", "loc <name>");
            return;
        }

        ExLocation loc = sender.getUser().getExLocation();
        String name = args.toMessage();
        UUID uuid = UUID.randomUUID();

        Server.broadcastMessage(Server.getChat().getSenderMember(sender.getUser())
                .append(Component.text(name, ExTextColor.PUBLIC))
                .append(Component.text(" " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ(), ExTextColor.VALUE))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/locshow " + uuid))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.text("Click to save the location in the sideboard"))));
        Server.broadcastNote(Instrument.PLING, Note.natural(1, Note.Tone.C));

        EndGameServer.getLocShowManager().addLocation(uuid, name, loc.getExBlock().getLocation());
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.getLength() == 1) {
            return List.of("lava", "cave", "village", "portal");
        }
        return List.of();
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.perm = plugin.createPermssionCode("end", "endgame.location");
    }
}
