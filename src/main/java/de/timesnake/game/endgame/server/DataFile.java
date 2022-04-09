package de.timesnake.game.endgame.server;

import de.timesnake.basic.bukkit.util.file.ExFile;

public class DataFile extends ExFile {

    public DataFile(String folder, String name) {
        super(folder, name);
    }

    public void saveTime(int time) {
        super.set("time", time).save();
    }

    public Integer getTime() {
        return super.getInt("time");
    }
}
