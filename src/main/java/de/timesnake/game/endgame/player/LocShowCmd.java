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

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

        HashMap<UUID, Tuple<String, ExLocation>> locationsByName = EndGameServer.getLocShowManager().getLocationsById();

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

        EndGameServer.getLocShowManager().setTrackedLocation(user, name, loc.getB());
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
