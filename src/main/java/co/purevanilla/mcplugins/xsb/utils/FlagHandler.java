/*
 * This file is part of XCatch.
 *
 * XCatch is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * XCatch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package co.purevanilla.mcplugins.xsb.utils;

import co.purevanilla.mcplugins.xsb.listeners.OnBlockBreak;
import co.purevanilla.mcplugins.xsb.data.ActionData;
import co.purevanilla.mcplugins.xsb.data.FlagData;
import co.purevanilla.mcplugins.xsb.Main;
import co.purevanilla.mcplugins.xsb.data.PersistentData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FlagHandler {
    public static final HashMap<UUID, FlagData> flags = new HashMap<>();

    public static void addFlag(BlockBreakEvent event, boolean test) {
        Block blockBroken = event.getBlock();
        Location location = test ? new Location(event.getPlayer().getWorld(), 0, 0, 0) : blockBroken.getLocation();
        UUID uuid = event.getPlayer().getUniqueId();
        String ore = test ? "test" : blockBroken.getBlockData().getMaterial().toString().toLowerCase().replace("_", " ");
        int amountMined = !test && OnBlockBreak.blocksMined.containsKey(uuid) ? OnBlockBreak.blocksMined.get(uuid).get(blockBroken.getBlockData().getMaterial()) + 1 : 1;
        if (flags.containsKey(uuid)) {
            flags.get(uuid).flags++;
            flags.get(uuid).lastFlag = Instant.now().getEpochSecond();
        } else {
            flags.put(uuid, new FlagData(1, Instant.now().getEpochSecond()));
        }
        if (!PersistentData.data.actions.containsKey(uuid)) {
            PersistentData.data.actions.put(uuid, new ArrayList<>());
        }
        HashMap<String, String> variables = new HashMap<String, String>() {{
            put("{player}", event.getPlayer().getName());
            put("{flags}", String.valueOf(flags.get(uuid).flags));
            put("{ore}", ore);
            put("{amount}", String.valueOf(amountMined));
            put("{x}", String.valueOf(location.getBlockX()));
            put("{y}", String.valueOf(location.getBlockY()));
            put("{z}", String.valueOf(location.getBlockZ()));
            put("{world}", location.getWorld().getName());
        }};
        if (Main.config.getInt("alert-flags") != 0 && flags.get(uuid).flags >= Main.config.getInt("alert-flags")) {
            Utils.broadcastTextComponent(MiniMessage.miniMessage().deserialize(Utils.replaceVariables(Main.config.getString("alert-message"), variables)), "xsb.alert");
            PersistentData.data.actions.get(uuid).add(new ActionData(ActionData.ActionType.FLAG, Instant.now().getEpochSecond(), ore, amountMined,
                    location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        } else {
            PersistentData.data.actions.get(uuid).add(new ActionData(ActionData.ActionType.FLAG, Instant.now().getEpochSecond(), ore, amountMined,
                    location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }
        if (Main.commands.containsKey(flags.get(uuid).flags)) {
            ArrayList<String> commands = Main.commands.get(flags.get(uuid).flags);
            for (String command : commands) {
                Bukkit.getScheduler().runTask(Main.getPlugin(Main.class), () -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), Utils.replaceVariables(command, variables)));
            }
        }
        PersistentData.data.totalFlags++;
        Main.metricFlags++;
        OnBlockBreak.data.remove(uuid);
        OnBlockBreak.blocksMined.remove(uuid);
    }
}
