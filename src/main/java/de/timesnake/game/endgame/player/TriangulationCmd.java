/*
 * game-endgame.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.endgame.player;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;

import java.util.List;

public class TriangulationCmd implements CommandListener {

    private Location firstLocation;
    private Location secondLocation;
    private Code.Permission perm;
    private Code.Help invalidIndex;
    private Code.Help tooFewLocations;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.hasPermission(this.perm)) {
            return;
        }

        if (!sender.isPlayer(true)) {
            return;
        }

        if (!args.isLengthEquals(1, true)) {
            return;
        }


        User user = sender.getUser();

        if (args.get(0).isInt(false)) {
            int index = args.get(0).toInt();

            if (index == 1) {
                firstLocation = user.getLocation();
            } else if (index == 2) {
                secondLocation = user.getLocation();
            } else {
                sender.sendPluginMessage(Component.text("Invalid location index, must be 1 or 2 ", ExTextColor.WARNING)
                        .append(Chat.getMessageCode(this.invalidIndex, ExTextColor.WARNING)));
                return;
            }

            sender.sendPluginMessage(Component.text("Saved location " + index, ExTextColor.PERSONAL));

        } else if (args.get(0).equalsIgnoreCase("calc", "calculate")) {
            if (firstLocation == null || secondLocation == null) {
                sender.sendPluginMessage(Component.text("Too few locations set ", ExTextColor.WARNING)
                        .append(Chat.getMessageCode(this.tooFewLocations, ExTextColor.WARNING)));
                return;
            }

            float angle = 180 - firstLocation.getYaw() - firstLocation.getPitch();
            double locationDistance = Math.abs(firstLocation.clone().add(0, -firstLocation.getY(), 0)
                    .distance(secondLocation.clone().add(0, -secondLocation.getY(), 0)));

            double distanceFromSecond = locationDistance / Math.sin(angle) * Math.sin(firstLocation.getPitch());

            Location stronghold = secondLocation.clone().add(secondLocation.getDirection().normalize().multiply(distanceFromSecond));
            EndGameServer.getLocShowManager().addLocation("stronghold", ExLocation.fromLocation(stronghold));

            Server.broadcastMessage(Chat.getSenderPlugin(de.timesnake.game.endgame.chat.Plugin.END_GAME)
                    .append(Component.text("stronghold", ExTextColor.PUBLIC))
                    .append(Component.text(" " + stronghold.getBlockX() + " " + stronghold.getBlockY() + " " + stronghold.getBlockZ(), ExTextColor.VALUE))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/locshow stronghold"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.text("Click to save the location in the sideboard"))));
        } else {
            sender.sendMessageCommandHelp(Component.text("Triangulate"), Component.text("tria <1/2/calc>"));
        }

    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("1", "2", "calc", "calculate");
        }
        return List.of();
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.perm = plugin.createPermssionCode("end", "game.endgame.triangulator");
        this.invalidIndex = plugin.createHelpCode("end", "Invalid location index, must be 1 or 2");
        this.tooFewLocations = plugin.createHelpCode("end", "Too few locations");
    }
}
