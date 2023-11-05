package co.purevanilla.mcplugins.xsb.listeners;

import co.purevanilla.mcplugins.xsb.Main;
import co.purevanilla.mcplugins.xsb.data.FlagData;
import co.purevanilla.mcplugins.xsb.utils.FlagHandler;
import co.purevanilla.mcplugins.xsb.data.XrayedBlock;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class VirtualItems implements Listener {

    private HashMap<Long, Set<XrayedBlock>> xrayed;
    private Set<UUID> virtualEntities;
    private FileConfiguration data;
    private File dataFile;
    private Plugin plugin;

    public VirtualItems(Plugin plugin){
        this.plugin=plugin;
        this.xrayed=new HashMap<>();
        this.virtualEntities=new HashSet<>();
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!this.dataFile.exists()) {
            this.dataFile.getParentFile().mkdirs();
            plugin.saveResource("data.yml", false);
        }
        this.data = YamlConfiguration.loadConfiguration(this.dataFile);
        for (String world:this.data.getConfigurationSection("worlds").getKeys(false)) {
            for (String chunkId:this.data.getConfigurationSection("worlds."+world).getKeys(false)) {
                Long chunkIdN = Long.valueOf(chunkId);
                for(String blockHash: this.data.getConfigurationSection("worlds."+world+"."+chunkId).getKeys(false)){
                    XrayedBlock block = new XrayedBlock(world, chunkId, blockHash, data);
                    if(xrayed.containsKey(chunkIdN)){
                        xrayed.get(chunkIdN).add(block);
                    } else {
                        Set<XrayedBlock> set = new HashSet<>();
                        set.add(block);
                        xrayed.put(chunkIdN,set);
                    }
                }
            }
        }
    }
    
    public void save() throws IOException {
        // do the best job we can to remove loaded virtual items
        for (UUID virtualEntity: virtualEntities) {
            Entity entity = Bukkit.getEntity(virtualEntity);
            if(entity!=null){
                entity.remove();
            }
        }
        // overwrite data
        data.set("chunk",null);
        for (Long chunkId: xrayed.keySet()) {
            for (XrayedBlock block: xrayed.get(chunkId)) {
                block.append(chunkId, data);
            }
        }
        data.save(dataFile);
    }

    private void removeOrphans(){
        Set<Long> tbr = new HashSet<>();
        for (Long chunkId:xrayed.keySet()) {
            if(xrayed.get(chunkId).isEmpty()){
                tbr.add(chunkId);
            }
        }

        for (Long removedChunk:tbr) {
            xrayed.remove(removedChunk);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(!xrayed.containsKey(event.getBlock().getChunk().getChunkKey())) return;
        Set<XrayedBlock> blocks = xrayed.get(event.getBlock().getChunk().getChunkKey());
        Set<XrayedBlock> tbr = new HashSet<>();
        for (XrayedBlock block: blocks) {
            if(Objects.equals(block.world, event.getBlock().getWorld().getName()) && event.getBlock().getX()==block.x && event.getBlock().getY()==block.y && event.getBlock().getZ()==block.z){
                tbr.add(block);
            }
        }
        for (XrayedBlock removed: tbr) {
            xrayed.get(event.getBlock().getChunk().getChunkKey()).remove(removed);
        }
        removeOrphans();
    }

    /**
     * create virtual entities
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        @Nullable FlagData data = FlagHandler.flags.get(event.getPlayer().getUniqueId());
        if(Main.rareOres.containsKey(event.getBlock().getType()) && data!=null && data.flags>=1 && event.isDropItems()){
            final Location location = event.getBlock().getLocation();

            Collection<ItemStack> fakeDrops =event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand(), event.getPlayer());
            int fakeExperience = event.getExpToDrop();
            event.setExpToDrop(0);
            event.setDropItems(false);

            for (ItemStack item: fakeDrops) {
                Item droppedItem = location.getWorld().dropItemNaturally(location, item);
                virtualEntities.add(droppedItem.getUniqueId());
            }

            if(fakeExperience>0){
                ExperienceOrb orb = location.getWorld().spawn(location, ExperienceOrb.class);
                orb.setExperience(fakeExperience);
                virtualEntities.add(orb.getUniqueId());
            }

            long key = location.getChunk().getChunkKey();
            XrayedBlock block = new XrayedBlock(event.getBlock().getType(), event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), System.currentTimeMillis()+1000*3600*4);
            if(xrayed.containsKey(key)){
                xrayed.get(key).add(block);
            } else {
                Set<XrayedBlock> set = new HashSet<>();
                set.add(block);
                xrayed.put(key, set);
            }
        }
    }

    /**
     * remove virtual entities on chunk unload
     */
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event){
        List<Entity> entities = List.of(event.getChunk().getEntities());
        for (Entity entity:entities) {
            if(virtualEntities.contains(entity.getUniqueId())){
                entity.remove();
                virtualEntities.remove(entity.getUniqueId());
            }
        }
    }

    /**
     * respawn blocks if due, and remove them from the pending list
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        if(xrayed.containsKey(event.getChunk().getChunkKey())){
            long now = System.currentTimeMillis();
            Set<XrayedBlock> blockSet = xrayed.get(event.getChunk().getChunkKey());
            Set<XrayedBlock> removedBlocks = new HashSet<>();
            for (XrayedBlock block:blockSet) {
                if(event.getChunk().getWorld().getName().equals(block.world) && block.due<now){
                    Location location = new Location(event.getWorld(), block.x, block.y, block.z);
                    location.getBlock().setType(block.material);
                    removedBlocks.add(block);
                }
            }
            for (XrayedBlock block: removedBlocks) {
                blockSet.remove(block);
            }
        }
    }

    /**
     * prevent picking up virtual item
     */
    @EventHandler
    public void onPickupVirtual(PlayerAttemptPickupItemEvent event){
        Item item = event.getItem();
        if(virtualEntities.contains(item.getUniqueId())){
            virtualEntities.remove(item.getUniqueId());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                int tbr = item.getItemStack().getAmount();
                Material type = item.getItemStack().getType();
                for (ItemStack itemStack : event.getPlayer().getInventory()) {
                    if(itemStack==null) continue;
                    if(itemStack.getType()==type){
                        int finalAmount = itemStack.getAmount()-tbr;
                        itemStack.setAmount(finalAmount);
                        if(finalAmount<0){
                            tbr = Math.abs(finalAmount);
                        } else {
                            break;
                        }
                    }
                }
            }, 1);
        }
    }

    /**
     * prevent absorbing virtual experience
     */
    @EventHandler
    public void onAbsorbVirtual(PlayerPickupExperienceEvent event){
        ExperienceOrb orb = event.getExperienceOrb();
        if(virtualEntities.contains(orb.getUniqueId())){
            event.getExperienceOrb().setExperience(0);
            virtualEntities.remove(orb.getUniqueId());
        }
    }


}
