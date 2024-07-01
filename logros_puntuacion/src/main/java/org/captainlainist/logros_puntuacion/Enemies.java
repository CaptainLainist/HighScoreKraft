package org.captainlainist.logros_puntuacion;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.EntityType;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Enemies {
    private static final Set<UUID> spawnerEntities = new HashSet<>();
    private File spawnerFile;
    private FileConfiguration spawnerConfig;

    public void creatureSpawn(CreatureSpawnEvent event){
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            spawnerEntities.add(event.getEntity().getUniqueId());
        }
    }

    //si es del spawner
    public static boolean isFromSpawner(Entity entity) {
        return spawnerEntities.remove(entity.getUniqueId());
    }

    //obtener puntos por entidad
    private int getPointsForEntity(EntityType entityType) {
        switch (entityType) {
            case BLAZE:
                return 30;
            case CAVE_SPIDER:
                return 15;
            case CREEPER:
                return 20;
            case DROWNED:
                return 10;
            case ELDER_GUARDIAN:
                return 100; // jefe
            case ENDERMAN:
                return 25;
            case ENDERMITE:
                return 10;
            case EVOKER:
                return 40;
            case GHAST:
                return 35;
            case GUARDIAN:
                return 30;
            case HOGLIN:
                return 20;
            case HUSK:
                return 15;
            case MAGMA_CUBE:
                return 20;
            case PHANTOM:
                return 25;
            case PIGLIN_BRUTE:
                return 30;
            case PILLAGER:
                return 20;
            case RAVAGER:
                return 50;
            case SHULKER:
                return 30;
            case SILVERFISH:
                return 10;
            case SKELETON:
                return 15;
            case SLIME:
                return 15;
            case SPIDER:
                return 10;
            case STRAY:
                return 15;
            case VEX:
                return 25;
            case VINDICATOR:
                return 30;
            case WARDEN:
                return 200; // jefe
            case WITCH:
                return 25;
            case WITHER_SKELETON:
                return 35;
            case ZOGLIN:
                return 20;
            case ZOMBIE:
                return 10;
            case ZOMBIE_VILLAGER:
                return 15;
            case ZOMBIFIED_PIGLIN:
                return 15;
            case ENDER_DRAGON:
                return 500; // jefe
            case WITHER:
                return 400; // jefe
            case VILLAGER:
                return 0;
            default:
                return 5;
        }
    }

    //guardar mobs de spawner en el archivo
    public void saveSpawnerEntities() {
        spawnerConfig.set("spawnerEntities", spawnerEntities.stream().map(UUID::toString).toList());

        try {
            spawnerConfig.save(spawnerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //cargar mobs de spawner
    public void loadSpawnerEntities() {
        spawnerEntities.clear();
        if (spawnerConfig.contains("spawnerEntities")) {
            for (String uuidStr : spawnerConfig.getStringList("spawnerEntities")) {
                spawnerEntities.add(UUID.fromString(uuidStr));
            }
        }
    }

    //crear el archivo
    public void createFile(File ruta){
        //crear y cargar archivo de spawner mobs
        spawnerFile = new File(ruta, "spawner_entities.yml");
        if (!spawnerFile.exists()) {
            try {
                spawnerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        spawnerConfig = YamlConfiguration.loadConfiguration(spawnerFile);
        loadSpawnerEntities();

    }

    //al morir la entidad
    public int entityDeath(EntityDeathEvent event){

        //si la criatura es de un spawner
        EntityType entityType = event.getEntity().getType();
        int points = getPointsForEntity(entityType);

        return points;

    }
}
