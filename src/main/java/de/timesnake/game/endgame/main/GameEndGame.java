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

package de.timesnake.game.endgame.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.endgame.chat.Plugin;
import de.timesnake.game.endgame.player.EndGameCmd;
import de.timesnake.game.endgame.player.LocCmd;
import de.timesnake.game.endgame.player.LocShowCmd;
import de.timesnake.game.endgame.player.TriangulationCmd;
import de.timesnake.game.endgame.server.EndGameServerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GameEndGame extends JavaPlugin {

    public static GameEndGame getPlugin() {
        return plugin;
    }

    private static GameEndGame plugin;

    @Override
    public void onLoad() {
        ServerManager.setInstance(new EndGameServerManager());
    }

    @Override
    public void onEnable() {
        GameEndGame.plugin = this;

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(EndGameServerManager.getInstance(), this);

        Server.getCommandManager().addCommand(this, "endgame", List.of("eg"), new EndGameCmd(),
                Plugin.END_GAME);
        Server.getCommandManager().addCommand(this, "loc", List.of("location", "pos", "position"),
                new LocCmd(), Plugin.END_GAME);
        Server.getCommandManager().addCommand(this, "locshow", new LocShowCmd(), Plugin.END_GAME);
        Server.getCommandManager().addCommand(this, "triangular", List.of("stronghold", "tria"),
                new TriangulationCmd(), Plugin.END_GAME);

        EndGameServerManager.getInstance().onEndGameEnable();

    }

    @Override
    public void onDisable() {
        EndGameServerManager.getInstance().onEndGameDisable();
    }
}
