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

package co.purevanilla.mcplugins.xsb;

import co.purevanilla.mcplugins.xsb.commands.ForceFlagCmd;
import co.purevanilla.mcplugins.xsb.commands.GetFlagsCmd;
import co.purevanilla.mcplugins.xsb.commands.ResetFlagsCmd;
import co.purevanilla.mcplugins.xsb.data.PersistentData;
import co.purevanilla.mcplugins.xsb.listeners.OnBlockBreak;
import co.purevanilla.mcplugins.xsb.listeners.VirtualItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {
    public static Main INSTANCE;
    public final Logger logger = getLogger();
    public static FileConfiguration config;
    public static final HashMap<Material, Integer> rareOres = new HashMap<>();
    public static final HashMap<Integer, ArrayList<String>> commands = new HashMap<>();
    public static int metricFlags = 0;
    private VirtualItems virtualItemsListener;
    private NamespacedKey actionDataKey;

    @Override
    public void onEnable() {
        INSTANCE = this;
        actionDataKey = new NamespacedKey(this, "action_data");

        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        } else {
            // System to update config
            FileConfiguration newConfig = (FileConfiguration) getConfig().getDefaults();
            boolean changed = false;
            for (String option : newConfig.getKeys(true)) {
                if (getConfig().contains(option, true))
                    newConfig.set(option, getConfig().get(option));
                else
                    changed = true;
            }
            if (changed) {
                try {
                    newConfig.save(new File(getDataFolder(), "config.yml"));
                    reloadConfig();
                } catch (IOException ignored) {
                }
            }
        }
        config = getConfig();
        loadConfigParts();

        if (new File(getDataFolder().getAbsolutePath() + "/data.json.gz").exists())
            PersistentData.loadData(getDataFolder().getAbsolutePath() + "/data.json.gz");

        this.virtualItemsListener = new VirtualItems(this);
        getServer().getPluginManager().registerEvents(new OnBlockBreak(), this);
        getServer().getPluginManager().registerEvents(this.virtualItemsListener, this);

        Objects.requireNonNull(getCommand("xreset")).setExecutor(new ResetFlagsCmd());
        Objects.requireNonNull(getCommand("xray")).setExecutor(new GetFlagsCmd());
        Objects.requireNonNull(getCommand("xflag")).setExecutor(new ForceFlagCmd());

        logger.info("XShadowBan has been initialized");
    }

    @Override
    public void onDisable() {
        PersistentData.saveData(getDataFolder().getAbsolutePath() + "/data.json.gz");
        try {
            this.virtualItemsListener.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NamespacedKey getActionDataKey() {
        return actionDataKey;
    }

    public static void loadConfigParts() {
        rareOres.clear();
        ArrayList<HashMap<String, Integer>> list = (ArrayList<HashMap<String, Integer>>) config.get("rare-ores");
        if (list != null) {
            for (HashMap<String, Integer> map : list) {
                String ore = map.keySet().stream().findFirst().get();
                Material material = Material.getMaterial(ore.toUpperCase());
                if (material != null)
                    rareOres.put(material, map.get(ore));
            }
        }
    }
}
