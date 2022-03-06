package de.timesnake.game.endgame.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.permission.Group;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.object.Type;
import de.timesnake.game.endgame.chat.Plugin;
import de.timesnake.game.endgame.main.GameEndGame;
import de.timesnake.game.endgame.player.LocShowManager;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Bukkit;
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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;

public class EndGameServerManager extends ServerManager implements Listener {

    public static EndGameServerManager getInstance() {
        return (EndGameServerManager) ServerManager.getInstance();
    }

    private UUID owner;
    private String ownerName;

    private DataFile file;

    private ExWorld lobbyWorld;

    private ExWorld gameWorld;
    private ExWorld gameWorldNether;
    private ExWorld gameWorldEnd;

    private Integer time;
    private boolean isTimeRunning = false;
    private BukkitTask timeTask;

    private EndGameMode mode;

    private Tablist gameTablist;

    private boolean reset = false;

    private File tagFile;

    private LocShowManager locShowManager;

    public void onEndGameEnable() {
        owner = Database.getEndGame().getUserFromServer(Database.getServers().getServer(Type.Server.GAME, Server.getPort()));
        if (owner == null) {
            Server.printWarning(Plugin.END_GAME, "Server-owner not defined");
        } else {
            ownerName = Database.getUsers().getUser(owner).getName();
        }

        lobbyWorld = Server.getWorldManager().createWorld("endgame");
        gameWorld = Server.getWorldManager().createWorld("world");
        gameWorldNether = Server.getWorldManager().createWorld("world_nether");
        gameWorldEnd = Server.getWorldManager().createWorld("world_the_end");

        this.setMode(EndGameMode.EASY);

        file = new DataFile("endgame", "data");
        this.time = file.getTime();
        if (this.time == null) {
            this.time = 0;
        }

        this.tagFile = new File(Bukkit.getWorldContainer() + File.separator + "eg_reset");
        if (this.tagFile.exists()) {
            this.tagFile.delete();
        }

        this.locShowManager = new LocShowManager();

        LinkedList<TablistGroupType> types = new LinkedList<>();
        types.add(Group.getTablistType());
        this.gameTablist = Server.getScoreboardManager().registerNewGroupTablist("endgame", Tablist.Type.HEALTH, types, (e, tablist) -> tablist.addEntry(e.getUser()), (e, tablist) -> tablist.removeEntry(e.getUser()));
        this.gameTablist.setHeader("§5End§6Game \n§7Server: " + Server.getName());
        this.updateTablistTime();
        Server.getScoreboardManager().setActiveTablist(this.gameTablist);
    }

    public void onDisable() {
        this.file.saveTime(this.time);
    }

    @EventHandler
    public void onUserJoin(UserJoinEvent e) {
        User user = e.getUser();
        user.sendPluginMessage(Plugin.END_GAME, ChatColor.WARNING + "" + ChatColor.BOLD + "If you or one of your " + "friends die, the whole world will be set back!");
        user.sendPluginMessage(Plugin.END_GAME, ChatColor.PUBLIC + "To play normal survival-build, " + "please use the survival-server");
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
        Server.broadcastMessage(Plugin.END_GAME, ChatColor.WARNING + "Starting world reset");
        for (User user : Server.getUsers()) {
            user.resetSideboard();
            user.switchToLobbyLast();
        }
        this.reset = true;
        Server.runTaskLaterSynchrony(() -> {
            Server.setStatus(Status.Server.SERVICE);
            Server.getWorldManager().resetUserLocations();

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

            Server.getWorldManager().unloadWorld(gameWorld);
            Server.getWorldManager().unloadWorld(gameWorldNether);
            Server.getWorldManager().unloadWorld(gameWorldEnd);

            this.time = 0;
            this.updateTablistTime();
            if (!this.tagFile.exists()) {
                try {
                    this.tagFile.createNewFile();
                    Server.printText(Plugin.END_GAME, "Created tag file");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.getChannel().sendMessageToProxy(new ChannelServerMessage<>(this.getPort(), MessageType.Server.RESTART, 40));
            Bukkit.shutdown();
        }, 5 * 20, GameEndGame.getPlugin());
    }

    public void broadcastGameMessage(String message) {
        Server.broadcastMessage(Plugin.END_GAME, ChatColor.PUBLIC + message);
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void pauseGame() {
        this.broadcastGameMessage(ChatColor.WARNING + "Game paused");
        for (User user : Server.getUsers()) {
            user.getPlayer().setGameMode(GameMode.ADVENTURE);
            user.lockInventory();
            user.lockLocation(true);
            user.getPlayer().setInvulnerable(true);
        }
        this.setTimeRunning(false);
    }

    public void resumeGame() {
        this.broadcastGameMessage(ChatColor.WARNING + "Game resumed");
        for (User user : Server.getUsers()) {
            user.getPlayer().setGameMode(GameMode.SURVIVAL);
            user.unlockInventory();
            user.unlockBlocKBreakPlace();
            user.lockLocation(false);
            user.getPlayer().setInvulnerable(false);
        }
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
        StringBuilder ts = new StringBuilder();
        if (this.time > 3600) {
            ts.append(this.time / 3600);
            ts.append("h ");
        }
        if (this.time > 60) {
            ts.append((this.time % 3600) / 60);
            ts.append("min ");
        }
        ts.append(this.time % 60);
        ts.append("s");
        this.gameTablist.setFooter("§6Time: §9" + ts);
    }



    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.setDeathMessage("");
        User user = Server.getUser(e.getEntity());
        Server.broadcastMessage(Plugin.END_GAME, ChatColor.VALUE + user.getChatName() + ChatColor.WARNING + " died!");
        this.broadcastGameMessage(ChatColor.WARNING + "Game stopped");
        this.setTimeRunning(false);
        for (User u : Server.getUsers()) {
            u.getPlayer().setGameMode(GameMode.SPECTATOR);
            u.asSender(Plugin.END_GAME).sendMessageCommandHelp("Reset", "eg reset");
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
            StringBuilder ts = new StringBuilder();
            if (this.time > 3600) {
                ts.append(this.time / 3600);
                ts.append("h ");
            }
            if (this.time > 60) {
                ts.append((this.time % 3600) / 60);
                ts.append("min ");
            }
            ts.append(this.time % 60);
            ts.append("s");
            this.broadcastGameMessage(ChatColor.WARNING + "You defeated Minecraft in " + ChatColor.VALUE + ts);
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

        this.broadcastGameMessage(ChatColor.WARNING + "Difficulty: " + mode.getName().replace("_", " "));

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
