package de.timesnake.game.endgame.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.endgame.chat.Plugin;
import de.timesnake.game.endgame.player.EndGameCmd;
import de.timesnake.game.endgame.player.LocCmd;
import de.timesnake.game.endgame.player.LocShowCmd;
import de.timesnake.game.endgame.server.EndGameServerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GameEndGame extends JavaPlugin {

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

        Server.getCommandManager().addCommand(this, "endgame", List.of("eg"), new EndGameCmd(), Plugin.END_GAME);
        Server.getCommandManager().addCommand(this, "loc", List.of("location", "pos", "position"), new LocCmd(), Plugin.END_GAME);
        Server.getCommandManager().addCommand(this, "locshow", new LocShowCmd(), Plugin.END_GAME);

        EndGameServerManager.getInstance().onEndGameEnable();

    }

    @Override
    public void onDisable() {
        EndGameServerManager.getInstance().onEndGameDisable();
    }

    public static GameEndGame getPlugin() {
        return plugin;
    }
}
