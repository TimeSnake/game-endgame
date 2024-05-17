/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.chat;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

  public static final Plugin END_GAME = new Plugin("EndGame", "GEG");

  protected Plugin(String name, String code) {
    super(name, code);
  }
}
