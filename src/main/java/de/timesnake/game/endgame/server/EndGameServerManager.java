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

package de.timesnake.game.endgame.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.DisplayGroup;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.game.endgame.chat.Plugin;
import de.timesnake.game.endgame.main.GameEndGame;
import de.timesnake.game.endgame.player.LocShowManager;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EndGameServerManager extends ServerManager implements Listener {

    public static EndGameServerManager getInstance() {
        return (EndGameServerManager) ServerManager.getInstance();
    }

    private DataFile file;

    private ExWorld gameWorld;
    private ExWorld gameWorldNether;
    private ExWorld gameWorldEnd;

    private Integer time;
    private boolean isTimeRunning = false;
    private BukkitTask timeTask;

    private EndGameMode mode;

    private Tablist gameTablist;

    private boolean reset = false;

    private LocShowManager locShowManager;

    public void onEndGameEnable() {
        gameWorld = Server.getWorldManager().createWorld("world");
        gameWorldNether = Server.getWorldManager().createWorld("world_nether");
        gameWorldEnd = Server.getWorldManager().createWorld("world_the_end");

        this.setMode(EndGameMode.EASY);
        this.gameWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        file = new DataFile("endgame", "data");
        this.time = file.getTime();
        if (this.time == null) {
            this.time = 0;
        }

        this.locShowManager = new LocShowManager();

        this.gameTablist = Server.getScoreboardManager().registerNewGroupTablist("endgame", Tablist.Type.HEALTH,
                DisplayGroup.MAIN_TABLIST_GROUPS, (e, tablist) -> tablist.addEntry(e.getUser()), (e, tablist) -> tablist.removeEntry(e.getUser()));
        this.gameTablist.setHeader("§5End§6Game \n§7Server: " + Server.getName());
        this.updateTablistTime();
        Server.getScoreboardManager().setActiveTablist(this.gameTablist);
    }

    public void onEndGameDisable() {
        this.file.saveTime(this.time);
    }

    @EventHandler
    public void onUserJoin(UserJoinEvent e) {
        User user = e.getUser();
        user.sendPluginMessage(Plugin.END_GAME, Component.text("If you or one of your friends die, the whole " +
                "world will be set back!", ExTextColor.WARNING, TextDecoration.BOLD));
        user.sendPluginMessage(Plugin.END_GAME, Component.text("To play normal survival-build, " +
                "please use the survival-server", ExTextColor.PUBLIC));
        user.getPlayer().setInvulnerable(false);
        if (!this.isTimeRunning) {
            user.setGameMode(GameMode.ADVENTURE);
            user.lockInventory();
            user.lockLocation(true);
            user.getPlayer().setInvulnerable(true);
            user.lockBlocKBreakPlace();
        } else {
            user.setGameMode(GameMode.SPECTATOR);
        }
    }

    public void resetGame() {
        this.pauseGame();
        this.isTimeRunning = false;
        if (this.timeTask != null) {
            this.timeTask.cancel();
        }

        this.locShowManager.reset();
        Server.broadcastMessage(Plugin.END_GAME, Component.text("Starting world reset", ExTextColor.WARNING));
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
            this.updateTablistTime();
            this.getChannel().sendMessageToProxy(new ChannelServerMessage<>(this.getName(),
                    MessageType.Server.KILL_DESTROY, ProcessHandle.current().pid()));
        }, 5 * 20, GameEndGame.getPlugin());
    }

    public void broadcastGameMessage(Component message) {
        Server.broadcastMessage(Plugin.END_GAME, message);
    }

    @Deprecated
    public void broadcastGameMessage(String message) {
        Server.broadcastMessage(Plugin.END_GAME, Component.text(message, ExTextColor.PUBLIC));
    }

    public void pauseGame() {
        this.broadcastGameMessage(Component.text("Game paused", ExTextColor.PUBLIC));
        for (User user : Server.getUsers()) {
            user.setGameMode(GameMode.ADVENTURE);
            user.lockInventory();
            user.lockLocation(true);
            user.getPlayer().setInvulnerable(true);
        }
        this.setTimeRunning(false);
        this.gameWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    }

    public void resumeGame() {
        this.broadcastGameMessage(Component.text("Game resumed", ExTextColor.PUBLIC));
        for (User user : Server.getUsers()) {
            user.setGameMode(GameMode.SURVIVAL);
            user.unlockInventory();
            user.unlockBlocKBreakPlace();
            user.lockLocation(false);
            user.getPlayer().setInvulnerable(false);
        }
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
            this.timeTask = new BukkitRunnable() {
                @Override
                public void run() {
                    time++;
                    updateTablistTime();
                }
            }.runTaskTimerAsynchronously(GameEndGame.getPlugin(), 0, 20);
        } else {
            this.isTimeRunning = false;
            if (this.timeTask != null) {
                this.timeTask.cancel();
            }
        }
    }

    private void updateTablistTime() {
        this.gameTablist.setFooter("§6Time: §9" + Chat.getTimeString(this.time));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.deathMessage(Component.empty());
        User user = Server.getUser(e.getEntity());
        Server.broadcastMessage(Plugin.END_GAME, user.getChatNameComponent()
                .append(Component.text(" died!", ExTextColor.WARNING)));
        this.broadcastGameMessage(Component.text("Game stopped", ExTextColor.WARNING));
        this.setTimeRunning(false);
        for (User u : Server.getUsers()) {
            u.setGameMode(GameMode.SPECTATOR);
            u.asSender(Plugin.END_GAME).sendMessageCommandHelp(Component.text("Reset", ExTextColor.PERSONAL),
                    Component.text("eg reset", ExTextColor.VALUE));
        }
    }

    @EventHandler
    public void onUserDamageUser(UserDamageByUserEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getType().equals(EntityType.ENDER_DRAGON)) {
            this.setTimeRunning(false);
            this.broadcastGameMessage(Component.text("You defeated Minecraft in ", ExTextColor.WARNING)
                    .append(Component.text(Chat.getTimeString(this.time), ExTextColor.VALUE)));
        }
    }

    public boolean isReset() {
        return this.reset;
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
}
