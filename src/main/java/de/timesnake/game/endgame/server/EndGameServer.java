/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.server;

import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.game.endgame.user.LocShowManager;
import de.timesnake.game.endgame.user.TablistManager;

public class EndGameServer extends GameServer {

  public static void resetGame() {
    server.resetGame();
  }

  @Deprecated
  public static void broadcastGameMessage(String message) {
    server.broadcastGameMessage(message);
  }

  public static void pauseGame() {
    server.pauseGame();
  }

  public static void resumeGame() {
    server.resumeGame();
  }

  public static boolean isTimeRunning() {
    return server.isTimeRunning();
  }

  public static void setTimeRunning(boolean run) {
    server.setTimeRunning(run);
  }

  public static boolean isReset() {
    return server.isReset();
  }

  public static void setMode(EndGameMode mode) {
    server.setMode(mode);
  }

  public static LocShowManager getLocShowManager() {
    return server.getLocShowManager();
  }

  public static TablistManager getTablistManager() {
    return server.getTablistManager();
  }

  private static final EndGameServerManager server = EndGameServerManager.getInstance();
}
