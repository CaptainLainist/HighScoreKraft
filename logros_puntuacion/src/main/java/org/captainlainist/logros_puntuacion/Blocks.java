package org.captainlainist.logros_puntuacion;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;

public class Blocks {



    //Set para saber que bloques son virgenes i cuales no (guarda los no virgenes)
    private final Set<String> placedBlocks = new HashSet<>();
    private File placedBlocksFile;
    private FileConfiguration placedBlocksConfig;

    //guarda los bloques no virgenes puestos en el set

    public void blockPlaced(BlockPlaceEvent event){
        String blockLocation = event.getBlock().getLocation().toString();
        placedBlocks.add(blockLocation);
    }

    //si el bloque no es virgen
    public boolean isBlockPlaced(String blockLocation) {
        return placedBlocks.contains(blockLocation);
    }

    //borrar bloque del set
    public void removeBlock(String blockLocation) {
        placedBlocks.remove(blockLocation);
    }


    //cargar placed blocks
    public void loadPlacedBlocks() {
        placedBlocks.clear();
        placedBlocks.addAll(placedBlocksConfig.getStringList("placedBlocks"));
    }

    //guardar placed blocks
    public void savePlacedBlocks() {
        placedBlocksConfig.set("placedBlocks", new ArrayList<>(placedBlocks));
        try {
            placedBlocksConfig.save(placedBlocksFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //crear archivo
    public void createFile(File file_ruta){
        //crear y cargar archivo de placeBlocks
        placedBlocksFile = new File(file_ruta, "placed_blocks.yml");
        if (!placedBlocksFile.exists()) {
            placedBlocksFile.getParentFile().mkdirs();
            try {
                placedBlocksFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        placedBlocksConfig = YamlConfiguration.loadConfiguration(placedBlocksFile);
        loadPlacedBlocks();

    }

    //cuando se rompe el bloque
    public int blockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();

        Material blockType = event.getBlock().getType();

        int sum_points;
        //dependiendo del material dara mas o menos puntos
        switch (blockType) {
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
                sum_points = 2;
                break;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                sum_points = 10;
                break;
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                sum_points = 5;
                break;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                sum_points = 15;
                break;
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
                sum_points = 7;
                break;
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
                sum_points = 8;
                break;
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                sum_points = 20;
                break;
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                sum_points = 30;
                break;
            case NETHER_GOLD_ORE:
            case NETHER_QUARTZ_ORE:
                sum_points = 12;
                break;
            case ANCIENT_DEBRIS:
                sum_points = 50;
                break;
            default:
                sum_points = 0;
                break;
        }
        if (sum_points != 0) {
            player.sendMessage(ChatColor.YELLOW + "+" + sum_points + ChatColor.WHITE + " score!");



        }
        return sum_points;
    }


}
