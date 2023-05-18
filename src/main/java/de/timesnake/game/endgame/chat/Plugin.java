/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.chat;

import de.timesnake.library.basic.util.LogHelper;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

  public static final Plugin END_GAME = new Plugin("EndGame", "GEG",
      LogHelper.getLogger("EndGame", Level.INFO));

  protected Plugin(String name, String code, Logger logger) {
    super(name, code, logger);
  }
}
