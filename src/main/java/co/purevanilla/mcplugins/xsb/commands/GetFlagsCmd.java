package co.purevanilla.mcplugins.xsb.commands;

import co.purevanilla.mcplugins.xsb.utils.FlagHandler;
import co.purevanilla.mcplugins.xsb.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.Instant;
import java.util.UUID;

public class GetFlagsCmd implements CommandExecutor {
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

        long delta = Instant.now().getEpochSecond()-FlagHandler.flags.get(uuid).lastFlag;
        if(delta>1800){
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_purple>•</dark_purple> <gray>Player <dark_purple>"+args[0]+" was flagged, but the flag expired"));
            return true;
        }
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_purple>•</dark_purple> <gray>Player <dark_purple>"+args[0]+"</dark_purple> has been flagged <dark_purple>"+FlagHandler.flags.get(uuid).flags+"</dark_purple> times, last time was <dark_purple>"+delta+" seconds ago</<dark_purple></gray>"));
        return true;
    }
}
