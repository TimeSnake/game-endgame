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

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.game.util.game.TablistGroupType;
import de.timesnake.basic.game.util.user.SpectatorUser;
import de.timesnake.game.endgame.server.EndGameServer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class EndGameUser extends SpectatorUser {

    public EndGameUser(Player player) {
        super(player);
    }

    @Override
    public TablistableGroup getTablistGroup(de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType type) {
        if (type.equals(TablistGroupType.DUMMY)) {
            return EndGameServer.getTablistManager().getGameTeam();
        }
        return super.getTablistGroup(type);
    }

    public void resume() {
        this.setGameMode(GameMode.SURVIVAL);
        this.unlockInventory();
        this.unlockBlockBreakPlace();
        this.unlockLocation();
        this.getPlayer().setInvulnerable(false);
    }

    public void pause() {
        this.setGameMode(GameMode.ADVENTURE);
        this.lockInventory();
        this.lockLocation();
        this.getPlayer().setInvulnerable(true);
    }
}
