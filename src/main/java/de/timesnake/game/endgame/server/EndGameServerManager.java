/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.server;

import de.timesnake.basic.bukkit.core.user.scoreboard.tablist.Tablist2;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.game.util.game.NonTmpGame;
import de.timesnake.basic.game.util.server.GameServerManager;
import de.timesnake.basic.game.util.user.SpectatorManager;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.game.endgame.main.GameEndGame;
import de.timesnake.game.endgame.user.EndGameUser;
import de.timesnake.game.endgame.user.LocShowManager;
import de.timesnake.game.endgame.user.TablistManager;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.Chat;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.chat.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EndGameServerManager extends GameServerManager<NonTmpGame> implements Listener {

  public static EndGameServerManager getInstance() {
    return (EndGameServerManager) ServerManager.getInstance();
  }

  private DataFile file;

  private ExWorld gameWorld;
  private ExWorld gameWorldNether;
  private ExWorld gameWorldEnd;

  private Integer time;
  private boolean started = false;
  private boolean ended = false;
  private boolean netherPortalLocated = false;
  private final Set<UUID> playingUsers = new HashSet<>();
  private boolean isTimeRunning = false;
  private BukkitTask timeTask;

  private EndGameMode mode;

  private TablistManager tablistManager;

  private boolean reset = false;

  private LocShowManager locShowManager;

  public void onEndGameEnable() {
    super.onGameEnable();

    gameWorld = Server.getWorld("world");
    gameWorldNether = Server.getWorld("world_nether");
    gameWorldEnd = Server.getWorld("world_the_end");

    this.tablistManager = new TablistManager();

    this.setMode(EndGameMode.EASY);
    this.gameWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

    file = new DataFile("endgame", "data");
    this.time = file.getTime();
    if (this.time == null) {
      this.time = 0;
    }

    this.locShowManager = new LocShowManager();
    this.updateTime();

    Server.getWorldManager().setCacheWorldSpawns(true);
  }

  public void onEndGameDisable() {
    this.file.saveTime(this.time);
  }

  @Override
  public User loadUser(Player player) {
    return new EndGameUser(player);
  }

  @Override
  public Sideboard getGameSideboard() {
    return null;
  }

  @Override
  public Tablist2 getGameTablist() {
    return EndGameServerManager.getInstance().getTablistManager().getTablist();
  }

  @Override
  protected SpectatorManager initSpectatorManager() {
    return new SpectatorManager() {

      @Override
      public @Nullable Sideboard getSpectatorSideboard() {
        return null;
      }

      @Override
      public de.timesnake.basic.bukkit.util.chat.@Nullable Chat getSpectatorChat() {
        return null;
      }

      @Override
      public @NotNull ExLocation getSpectatorSpawn() {
        return ExLocation.fromLocation(
            EndGameServerManager.this.gameWorld.getSpawnLocation());
      }

      @Override
      public boolean loadSpectatorTools() {
        return true;
      }
    };
  }

  @EventHandler
  public void onUserJoin(UserJoinEvent e) {
    EndGameUser user = (EndGameUser) e.getUser();
    user.sendPluginMessage(Plugin.GAME,
        Component.text("If you or one of your friends die, the whole " +
            "world will be set back!", ExTextColor.WARNING, TextDecoration.BOLD));
    user.sendPluginMessage(Plugin.GAME, Component.text("To play normal survival-build, " +
        "please use the survival-server", ExTextColor.PUBLIC));

    if (!this.started || this.playingUsers.contains(user.getUniqueId())) {
      user.setStatus(Status.User.IN_GAME);
      if (this.playingUsers.contains(user.getUniqueId()) && this.isTimeRunning) {
        user.setGameMode(GameMode.SURVIVAL);
        user.unlockInventory();
        user.unlockLocation();
        user.setInvulnerable(false);
        user.unlockBlockBreakPlace();
      } else {
        user.setGameMode(GameMode.ADVENTURE);
        user.lockInventory();
        user.lockLocation();
        user.setInvulnerable(true);
        user.lockBlockBreakPlace();
      }
    } else {
      user.setStatus(Status.User.SPECTATOR);
      user.joinSpectator();
    }
  }

  public void resetGame() {
    this.pauseGame();
    this.started = false;
    this.playingUsers.clear();
    this.isTimeRunning = false;
    if (this.timeTask != null) {
      this.timeTask.cancel();
    }

    this.locShowManager.reset();
    Server.broadcastMessage(Plugin.GAME,
        Component.text("Starting world reset", ExTextColor.WARNING));
    for (User user : Server.getUsers()) {
      user.resetSideboard();
      user.switchToLobbyLast();
    }
    this.reset = true;
    Server.runTaskLaterSynchrony(() -> {
      Server.setStatus(Status.Server.SERVICE);

      this.gameWorld.setAutoSave(false);
      this.gameWorldNether.setAutoSave(false);
      this.gameWorldEnd.setAutoSave(false);

      for (Chunk chunk : gameWorld.getLoadedChunks()) {
        chunk.unload(false);
      }

      for (Chunk chunk : gameWorldNether.getLoadedChunks()) {
        chunk.unload(false);
      }

      for (Chunk chunk : gameWorldEnd.getLoadedChunks()) {
        chunk.unload(false);
      }

      Server.getWorldManager().unloadWorld(gameWorld, false);
      Server.getWorldManager().unloadWorld(gameWorldNether, false);
      Server.getWorldManager().unloadWorld(gameWorldEnd, false);

      this.time = 0;
      this.updateTime();
      this.getChannel().sendMessage(
          new ChannelServerMessage<>(this.getName(), MessageType.Server.KILL_DESTROY,
              ProcessHandle.current().pid()));
    }, 5 * 20, GameEndGame.getPlugin());
  }

  public void broadcastGameMessage(Component message) {
    Server.broadcastMessage(Plugin.GAME, message);
  }

  public void pauseGame() {
    this.broadcastGameMessage(Component.text("Game paused", ExTextColor.PUBLIC));
    Server.getUsers().forEach(user -> ((EndGameUser) user).pause());
    this.setTimeRunning(false);
    this.gameWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
  }

  public void resumeGame() {
    if (!this.started) {
      this.playingUsers.addAll(Server.getUsers().stream().map(User::getUniqueId).toList());
      Server.getUsers().forEach(u -> u.setStatus(Status.User.IN_GAME));
      this.started = true;
    }

    this.broadcastGameMessage(Component.text("Game resumed", ExTextColor.PUBLIC));
    Server.getInGameUsers().forEach(user -> ((EndGameUser) user).resume());
    this.gameWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
    this.setTimeRunning(true);
  }

  public boolean isTimeRunning() {
    return this.isTimeRunning;
  }

  public void setTimeRunning(boolean run) {
    if (run) {
      this.isTimeRunning = true;
      if (this.timeTask != null) {
        this.timeTask.cancel();
      }
      this.timeTask = Server.runTaskTimerAsynchrony(() -> {
        time++;
        updateTime();
      }, 0, 20, GameEndGame.getPlugin());
    } else {
      this.isTimeRunning = false;
      if (this.timeTask != null) {
        this.timeTask.cancel();
      }
      this.timeTask = Server.runTaskTimerAsynchrony(this::updateTime, 0, 20,
          GameEndGame.getPlugin());
    }
  }

  private void updateTime() {
    Server.getUsers().forEach(u -> u.sendActionBarText(
        Component.text(Chat.getTimeString(this.time), ExTextColor.DARK_AQUA)));
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent e) {
    Server.runTaskLaterSynchrony(() -> {
      this.broadcastGameMessage(Component.text("Game stopped", ExTextColor.WARNING));
      this.setTimeRunning(false);
      this.ended = true;
      for (User u : Server.getUsers()) {
        u.setGameMode(GameMode.SPECTATOR);
        u.asSender(Plugin.GAME)
            .sendMessageCommandHelp(Component.text("Reset", ExTextColor.PERSONAL),
                Component.text("eg reset", ExTextColor.VALUE));
      }
    }, 1, GameEndGame.getPlugin());

  }

  @EventHandler
  public void onUserDamageByUser(UserDamageByUserEvent e) {
    e.setCancelled(true);
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent e) {
    if (e.getEntity().getType().equals(EntityType.ENDER_DRAGON)) {
      this.setTimeRunning(false);
      this.ended = true;
      this.broadcastGameMessage(
          Component.text("You defeated Minecraft in ", ExTextColor.WARNING)
              .append(Component.text(Chat.getTimeString(this.time),
                  ExTextColor.VALUE)));
    }
  }

  @EventHandler
  public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
    EndGameUser user = ((EndGameUser) Server.getUser(e.getPlayer()));

    if (this.netherPortalLocated) {
      return;
    }

    if (this.playingUsers.contains(user.getUniqueId())) {
      if (user.getExWorld().equals(this.gameWorldNether)) {
        Location loc = user.getLocation();

        int id = EndGameServer.getLocShowManager().addLocation("nether portal", ExLocation.fromLocation(loc));

        this.netherPortalLocated = true;

        Server.broadcastMessage(
            Chat.getSenderPlugin(Plugin.GAME)
                .append(Component.text("nether portal", ExTextColor.PUBLIC))
                .append(Component.text(
                    " " + loc.getBlockX() + " " + loc.getBlockY()
                        + " " + loc.getBlockZ(), ExTextColor.VALUE))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/locshow " + id))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Component.text(
                        "Click to save location in sideboard"))));
      }
    }
  }

  public boolean isReset() {
    return this.reset;
  }

  public boolean isStarted() {
    return started;
  }

  public boolean isEnded() {
    return ended;
  }

  public Set<UUID> getPlayingUsers() {
    return playingUsers;
  }

  public EndGameMode getMode() {
    return mode;
  }

  public void setMode(EndGameMode mode) {
    this.mode = mode;

    this.broadcastGameMessage(Component.text("Difficulty: ", ExTextColor.WARNING)
        .append(Component.text(mode.getName().replace("_", " "), ExTextColor.VALUE)));

    this.gameWorld.setDifficulty(mode.getDifficulty());
    this.gameWorldNether.setDifficulty(mode.getDifficulty());
    this.gameWorldEnd.setDifficulty(mode.getDifficulty());

    this.gameWorld.setHardcore(mode.isHardcore());
    this.gameWorldNether.setHardcore(mode.isHardcore());
    this.gameWorldEnd.setHardcore(mode.isHardcore());

    this.gameWorld.setGameRule(GameRule.NATURAL_REGENERATION, !mode.isHardcore());
    this.gameWorldNether.setGameRule(GameRule.NATURAL_REGENERATION, !mode.isHardcore());
    this.gameWorldEnd.setGameRule(GameRule.NATURAL_REGENERATION, !mode.isHardcore());
  }

  public LocShowManager getLocShowManager() {
    return locShowManager;
  }

  public TablistManager getTablistManager() {
    return tablistManager;
  }
}
