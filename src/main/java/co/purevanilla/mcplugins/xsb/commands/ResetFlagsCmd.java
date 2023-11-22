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

package co.purevanilla.mcplugins.xsb.commands;

import co.purevanilla.mcplugins.xsb.data.PersistentData;
import co.purevanilla.mcplugins.xsb.utils.FlagHandler;
import co.purevanilla.mcplugins.xsb.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class ResetFlagsCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_purple>•</dark_purple> <gray>Please, specify a player</gray>"));
            return true;
        }
        UUID uuid = Utils.getOfflineUUID(args[0]);
        if (uuid == null || !FlagHandler.flags.containsKey(uuid)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_purple>•</dark_purple> <gray>No flag data found</gray>"));
            return true;
        }
        FlagHandler.flags.get(uuid).lastFlag=0;
        FlagHandler.flags.get(uuid).flags=0;
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_purple>•</dark_purple> <gray>Reset flag data for <dark_purple>"+args[0]+"</dark_purple></gray>"));
        return true;
    }
}
