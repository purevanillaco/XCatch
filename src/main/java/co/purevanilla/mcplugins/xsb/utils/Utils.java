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

import co.purevanilla.mcplugins.xsb.Main;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;

public class Utils {
    public static double getAngleDistance(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360;
        return phi > 180 ? 360 - phi : phi;
    }

    public static UUID getOfflineUUID(String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getUniqueId();
        }
        return null;
    }

    public static String replaceVariables(String message, Map<String, String> variables) {
        for (String variable : variables.keySet()) {
            message = message.replace(variable, variables.get(variable));
        }
        return message;
    }

    public static void broadcastTextComponent(Component component, String permission) {
        for (Player player : Main.INSTANCE.getServer().getOnlinePlayers()) {
            if (!player.hasPermission(permission)) continue;
            player.sendMessage(component);
            player.playSound(player.getLocation(), Sound.BLOCK_NETHERITE_BLOCK_PLACE, 1.0F, 0.6F);
        }
    }
}
