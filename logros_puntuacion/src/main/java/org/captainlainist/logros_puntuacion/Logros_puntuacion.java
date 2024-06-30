package org.captainlainist.logros_puntuacion;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;
import java.io.IOException;
import java.io.File;
import java.util.logging.Level;

import org.bukkit.event.player.PlayerPickupItemEvent;
public final class Logros_puntuacion extends JavaPlugin implements Listener, CommandExecutor {

    Blocks blocks = new Blocks();


    Enemies enemies = new Enemies();

    Achievements logros = new Achievements();



    //Mapa con los usuarios (por nombre) y sus puntos
    private Map<String, Integer> playerPoints = new HashMap<>();

    //archivo de puntos
    private File pointsFile;
    private FileConfiguration pointsConfig;

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        blocks.blockPlaced(event);
    }
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        enemies.creatureSpawn(event);
    }



    //al encenderse el plugin
    @Override
    public void onEnable() {



        //comandos
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("puntos").setExecutor(this);
        this.getCommand("darpuntos").setExecutor(this);
        this.getCommand("setpuntos").setExecutor(this);


        blocks.createFile(getDataFolder());

        enemies.createFile(getDataFolder());

        getLogger().info("Logros_Puntuacion se ha habilitado");

    }

    //al desactivarse
    @Override
    public void onDisable() {
        blocks.savePlacedBlocks();
        enemies.saveSpawnerEntities();
        getLogger().info("Logros_Puntuacion se ha deshabilitado");
    }


    //obtener puntos por enemigo


    //sumar puntos al matar mobs
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        if (event.getEntity().getKiller() instanceof Player) {
            if (enemies.isFromSpawner(event.getEntity())) {
                return;
            }

            Player player = event.getEntity().getKiller();
            int sum_points = enemies.entityDeath(event);

            int newPoints = playerPoints.getOrDefault(player.getName() + "-" + player.getUniqueId(), 0) + sum_points;
            playerPoints.put(player.getName() + "-" + player.getUniqueId(), newPoints);
            if (sum_points != 0) {
                player.sendMessage(ChatColor.YELLOW + "+" + sum_points + ChatColor.WHITE + " puntos por ese " + ChatColor.AQUA + event.getEntity().getName());
            }
            savePoints();
        }
    }



    //cuando el jugador se une
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        String name = player.getName();

        String completeName = name + "-" + player.getUniqueId();
        // inicializa los puntos a 75 de todos los jugadores
        playerPoints.putIfAbsent(completeName, 75);


        //crear y cargar archivo de puntos
        pointsFile = new File(getDataFolder(), "points.yml");
        if (!pointsFile.exists()) {
            pointsFile.getParentFile().mkdirs();
            try {
                pointsFile.createNewFile();
                getLogger().info("Archivo de puntos creado correctamente");
            } catch (IOException e) {

                getLogger().info("ERROR: Archivo de puntos no se pudo crear");
                e.printStackTrace();
            }
        }

        pointsConfig = YamlConfiguration.loadConfiguration(pointsFile);
        loadPoints();

    }



    //lista de comandos
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //puntos, muestra los puntos
        if (cmd.getName().equalsIgnoreCase("puntos")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String name = player.getName();
                int points = playerPoints.getOrDefault(name + "-" + player.getUniqueId(), 0);
                player.sendMessage("Tus puntos: " + ChatColor.YELLOW + points);
            } else {
                sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            }
            return true;
            //darpuntos, da o quita puntos
        } else if (cmd.getName().toLowerCase().startsWith("darpuntos")){

            if (!(sender instanceof Player)) {
                sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
                return true;
            }

            if (!sender.hasPermission("logros_puntuacion.admin")) {
                sender.sendMessage("No tienes permisos para ejecutar este comando.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("Uso incorrecto: /darpuntos <jugador> <puntos>");
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                sender.sendMessage("El jugador especificado no está en línea.");
                return true;
            }

            int pointsToAdd;
            try {
                pointsToAdd = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("La cantidad de puntos debe ser un número válido.");
                return true;
            }

            int puntosOG = playerPoints.getOrDefault(targetPlayer.getName() + "-" + targetPlayer.getUniqueId(), 0);

            int puntosTotales = puntosOG + pointsToAdd;

            playerPoints.put(targetPlayer.getName() + "-" + targetPlayer.getUniqueId(), puntosTotales);

            if (pointsToAdd >= 0) {
                sender.sendMessage("Puntos añadidos al usuario " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.WHITE + ": " + ChatColor.YELLOW + pointsToAdd + ChatColor.WHITE + ". Puntos Totales: " + ChatColor.YELLOW + puntosTotales);
            } else {
                int puntosPos = pointsToAdd * -1;
                sender.sendMessage("Puntos quitados del usuario " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.WHITE + ": " + ChatColor.RED + puntosPos + ChatColor.WHITE + ". Puntos Totales: " + ChatColor.YELLOW + puntosTotales);
            }
            if (pointsToAdd > 0) {
                targetPlayer.sendMessage(ChatColor.GREEN + "Puntos añadidos al usuario " + targetPlayer.getName() + ": " + pointsToAdd + ". Puntos Totales: " + puntosTotales);
            }  else if (pointsToAdd < 0){
                int puntosPos = pointsToAdd * -1;
                targetPlayer.sendMessage(ChatColor.RED + "Puntos quitados del usuario " + targetPlayer.getName() + ": " + puntosPos + ". Puntos Totales: " + puntosTotales);
            } else {
                targetPlayer.sendMessage("Puntos añadidos al usuario " + targetPlayer.getName() + ": " + pointsToAdd + ". Puntos Totales: " + puntosTotales);

            }
            savePoints();

            return true;

            //setpuntos, hace que el usuario tenga ciertos puntos
        } else if (cmd.getName().toLowerCase().startsWith("setpuntos")){

        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        if (!sender.hasPermission("logros_puntuacion.admin")) {
            sender.sendMessage("No tienes permisos para ejecutar este comando.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Uso incorrecto: /setpuntos <jugador> <puntos>");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage("El jugador especificado no está en línea.");
            return true;
        }

        int pointsToAdd;
        try {
            pointsToAdd = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("La cantidad de puntos debe ser un número válido.");
            return true;
        }

        int puntosAnteriores = playerPoints.getOrDefault(targetPlayer.getName() + "-" + targetPlayer.getUniqueId(), 0);

        playerPoints.put(targetPlayer.getName() + "-" + targetPlayer.getUniqueId(), pointsToAdd);


        sender.sendMessage("Puntos aplicados al usuario " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.WHITE + ": " + ChatColor.YELLOW + pointsToAdd);

        if (puntosAnteriores < pointsToAdd) {
            targetPlayer.sendMessage(ChatColor.GREEN + "Puntos aplicados al usuario " + targetPlayer.getName() + ": " + pointsToAdd);
        } else if (puntosAnteriores > pointsToAdd){
            targetPlayer.sendMessage(ChatColor.RED + "Puntos aplicados al usuario " + targetPlayer.getName() + ": " + pointsToAdd);
        }

        savePoints();

        return true;

    }
        return false;
    }



    //obtener que puntos deberia sumar dependiendo del achievement


    //cargar puntos del archivo
    private void loadPoints() {
        if (pointsFile.exists()) {
            for (String playerName : pointsConfig.getKeys(false)) {
                int points = pointsConfig.getInt(playerName);
                playerPoints.put(playerName, points);
            }
        }
    }

    //guarda los puntos en el archivo
    private void savePoints() {
        for (String playerName : playerPoints.keySet()) {
            pointsConfig.set(playerName, playerPoints.get(playerName));
        }
        try {
            pointsConfig.save(pointsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //cuano se rompe un bloque
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        String blockLocation = event.getBlock().getLocation().toString();

        //si no es virgen se anula
        if (blocks.isBlockPlaced(blockLocation)) {
            return;
        }
        int sum_points = blocks.blockBreak(event);

        int total_points = playerPoints.getOrDefault(event.getPlayer().getName() + "-" + event.getPlayer().getUniqueId(), 0) + sum_points;

        playerPoints.put(event.getPlayer().getName() + "-" + event.getPlayer().getUniqueId(), total_points);

        savePoints();
    }








    //al hacerse un achievement
    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {

        Player player = event.getPlayer();

        String playerName = player.getName();

        int sum_points = logros.achievementDone(event);

        if (sum_points != 0) {
            int newPoints = playerPoints.getOrDefault(playerName + "-" + player.getUniqueId(), 0) + sum_points;
            playerPoints.put(playerName + "-" + player.getUniqueId(), newPoints);

            savePoints();

            player.sendMessage("Has conseguido un logro! [" + ChatColor.AQUA + logros.remodelateAchievement(event.getAdvancement().getKey().getKey()) + ChatColor.WHITE + "] por " + ChatColor.YELLOW + sum_points + ChatColor.WHITE + " puntos, Puntos Totales : " + ChatColor.YELLOW + newPoints + ChatColor.WHITE);

        }
    }
}
