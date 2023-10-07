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

package co.purevanilla.mcplugins.xcatch.commands;

import co.purevanilla.mcplugins.xcatch.data.PersistentData;
import co.purevanilla.mcplugins.xcatch.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class XCatchCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§8[§cXCatch§8] §cMissing argument <player>.");
            return false;
        }
        UUID uuid = Utils.getOfflineUUID(args[0]);
        if (uuid == null || !PersistentData.data.actions.containsKey(uuid)) {
            sender.sendMessage("§8[§cXCatch§8] §cPlayer not found or no data available for player.");
            return false;
        }
        PersistentData.data.actions.remove(uuid);
        sender.sendMessage("§8[§cXCatch§8] §cFlags of " + args[0] + " has been cleared.");
        return true;
    }
}
