package co.purevanilla.mcplugins.xsb.commands;

import co.purevanilla.mcplugins.xsb.data.FlagData;
import co.purevanilla.mcplugins.xsb.utils.FlagHandler;
import co.purevanilla.mcplugins.xsb.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class ForceFlagCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_purple>•</dark_purple> <gray>Please, specify a player</gray>"));
            return true;
        }
        UUID uuid = Utils.getOfflineUUID(args[0]);
        if (uuid == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_purple>•</dark_purple> <gray>Unknown player/gray>"));
            return true;
        }
        if(!FlagHandler.flags.containsKey(uuid)){
            FlagHandler.flags.put(uuid, new FlagData(1, System.currentTimeMillis()));
        } else {
            FlagHandler.flags.get(uuid).flags=1;
            FlagHandler.flags.get(uuid).lastFlag=System.currentTimeMillis();
        }
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_purple>•</dark_purple> <gray>Player <dark_purple>"+args[0]+"</dark_purple> has been flagged</gray>"));
        return true;
    }
}
