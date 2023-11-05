package co.purevanilla.mcplugins.xsb.data;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class XrayedBlock {

    public final Material material;
    public final String world;
    public final int x;
    public final int y;
    public final int z;
    public final long due;

    public XrayedBlock(Material material, String world, int x, int y, int z, long due){
        this.material=material;
        this.due=due;
        this.world=world;
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public XrayedBlock(String world, String chunkId, String blockHash, FileConfiguration configuration){
        this.world=world;
        String key = "worlds."+world+"."+chunkId+"."+blockHash;
        List<String> parts = List.of(blockHash.split("\\|"));
        this.x=Integer.parseInt(parts.get(0));
        this.y=Integer.parseInt(parts.get(1));
        this.z=Integer.parseInt(parts.get(2));
        this.material=Material.valueOf(configuration.getString(key+".mat"));
        this.due=configuration.getLong(key+".due");
    }

    public void append(long chunkId, FileConfiguration configuration){
        final String key = "worlds."+world+"."+chunkId+"."+x+"|"+y+"|"+z;
        configuration.set(key+".due", this.due);
        configuration.set(key+".mat", this.material.toString());
    }

}
