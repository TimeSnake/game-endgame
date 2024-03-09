/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.user;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroup;
import de.timesnake.basic.game.util.game.TablistGroupType;
import de.timesnake.basic.game.util.user.SpectatorUser;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.basic.util.Status;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class EndGameUser extends SpectatorUser {

  public EndGameUser(Player player) {
    super(player);
  }

  @Override
  public TablistGroup getTablistGroup(de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType type) {
    if (type.equals(TablistGroupType.GAME_TEAM)) {
      return this.hasStatus(Status.User.SPECTATOR, Status.User.OUT_GAME) ? null : EndGameServer.getTablistManager().getGameTeam();
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
