/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.endgame.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.endgame.server.EndGameServer;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;

public class TriangulationCmd implements CommandListener {

    private Location firstLocation;
    private Location secondLocation;
    private Code perm;
    private Code invalidIndex;
    private Code tooFewLocations;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd,
            Arguments<Argument> args) {
        if (!sender.hasPermission(this.perm)) {
            return;
        }

        if (!sender.isPlayer(true)) {
            return;
        }

        if (!args.isLengthEquals(1, true)) {
            return;
        }

        User user = sender.getUser();

        if (args.get(0).isInt(false)) {
            int index = args.get(0).toInt();

            if (index == 1) {
                firstLocation = user.getLocation();
            } else if (index == 2) {
                secondLocation = user.getLocation();
            } else {
                sender.sendPluginMessage(Component.text("Invalid location index, must be 1 or 2 ",
                                ExTextColor.WARNING)
                        .append(Chat.getMessageCode(this.invalidIndex, ExTextColor.WARNING)));
                return;
            }

            sender.sendPluginMessage(
                    Component.text("Saved location " + index, ExTextColor.PERSONAL));

        } else if (args.get(0).equalsIgnoreCase("calc", "calculate")) {
            if (firstLocation == null || secondLocation == null) {
                sender.sendPluginMessage(
                        Component.text("Too few locations set ", ExTextColor.WARNING)
                                .append(Chat.getMessageCode(this.tooFewLocations,
                                        ExTextColor.WARNING)));
                return;
            }

            Location stronghold = this.calculate(this.firstLocation, this.secondLocation);

            if (stronghold == null) {
                sender.sendPluginMessage(
                        Component.text("Unable to calculate position", ExTextColor.WARNING));
                return;
            }

            UUID uuid = UUID.randomUUID();
            EndGameServer.getLocShowManager()
                    .addLocation(uuid, "stronghold", ExLocation.fromLocation(stronghold));

            Server.broadcastMessage(
                    Chat.getSenderPlugin(de.timesnake.game.endgame.chat.Plugin.END_GAME)
                            .append(Component.text("stronghold", ExTextColor.PUBLIC))
                            .append(Component.text(
                                    " " + stronghold.getBlockX() + " " + stronghold.getBlockY()
                                            + " " + stronghold.getBlockZ(), ExTextColor.VALUE))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/locshow " + uuid))
                            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.text(
                                            "Click to save the location in the sideboard"))));
        } else {
            sender.sendMessageCommandHelp(Component.text("Triangulate"),
                    Component.text("tria <1/2/calc>"));
        }

    }

    public Location calculate(Location loc1, Location loc2) {
        double x1 = loc1.getX();
        double z1 = loc1.getZ();
        double x2 = loc2.getX();
        double z2 = loc2.getZ();

        float a1 = (loc1.getYaw() + 360 + 270) % 180;
        float a2 = (loc2.getYaw() + 360 + 270) % 180;

        a1 = ((int) (a1 * 1000)) / 1000F;
        a2 = ((int) (a2 * 1000)) / 1000F;

        // Als Erstes berechnen wir die Steigung der jeweiligen Geraden
        double s1;
        double s2;
        if (a1 == 0.0) { // Undefinierten Fall abfangen, und Flag setzen
            a1 = 0.001F;
        }
        s1 = Math.tan(Math.toRadians(a1));

        if (a2 == 0.0) { // s.o.
            a2 = 0.001F;
        }
        s2 = Math.tan(Math.toRadians(a2));

        // Nun berechnen wir den Schnittpunkt
        if (s1 == s2) {
            return null;
        }

        // Berechne Achsenabschnitt
        double t1 = z1 - (s1 * x1);
        double t2 = z2 - (s2 * x2);

        double x = (t2 - t1) / (s1 - s2);
        double z = s1 * x + t1;
        return new Location(loc1.getWorld(), x,
                loc1.getWorld().getHighestBlockYAt((int) x, (int) z), z);
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd,
            Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("1", "2", "calc", "calculate");
        }
        return List.of();
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.perm = plugin.createPermssionCode("game.endgame.triangulator");
        this.invalidIndex = plugin.createHelpCode("Invalid location index, must be 1 or 2");
        this.tooFewLocations = plugin.createHelpCode("Too few locations");
    }
}
