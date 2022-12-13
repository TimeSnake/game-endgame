/*
 * workspace.game-endgame.main
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

package de.timesnake.game.endgame.user;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.game.endgame.server.EndGameMode;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.game.endgame.server.EndGameServerManager;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

import java.util.List;

public class EndGameCmd implements CommandListener {

    private Code.Permission resumePerm;
    private Code.Permission pausePerm;
    private Code.Permission resetPerm;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {

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
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.getLength() == 1) {
            return List.of("resume", "pause", "reset", "mode");
        }
        if (args.getLength() == 2 && args.get(0).equalsIgnoreCase("mode")) {
            return EndGameMode.getNames();
        }
        return List.of();
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.resumePerm = plugin.createPermssionCode("end", "endgame.resume");
        this.pausePerm = plugin.createPermssionCode("end", "endgame.pause");
        this.resetPerm = plugin.createPermssionCode("end", "endgame.reset");
    }

}
