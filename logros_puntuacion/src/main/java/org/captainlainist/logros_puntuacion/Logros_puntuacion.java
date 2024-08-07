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


    //clases usadas
    Blocks blocks = new Blocks();


    Enemies enemies = new Enemies();

    Achievements logros = new Achievements();

    DescargaCaras dc = new DescargaCaras();


    //Mapa con los usuarios (por nombre) y sus puntos
    private Map<String, Integer> playerPoints = new HashMap<>();

    //archivo de puntos
    private File pointsFile;
    private FileConfiguration pointsConfig;

    //al poner bloques
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        blocks.blockPlaced(event);
    }

    //al spawnear algun mob
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        enemies.creatureSpawn(event);
    }



    //al encenderse el plugin
    @Override
    public void onEnable() {


        //comandos
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("score").setExecutor(this);
        getCommand("givescore").setExecutor(this);
        getCommand("setscore").setExecutor(this);
        getCommand("seescore").setExecutor(this);

        //crear archivos
        blocks.createFile(getDataFolder());

        enemies.createFile(getDataFolder());



        getLogger().info("HighScoreKraft has been enabled");



    }

    //al desactivarse
    @Override
    public void onDisable() {
        blocks.savePlacedBlocks();
        enemies.saveSpawnerEntities();
        getLogger().info("HighScoreKraft has been disabled");
    }

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
                player.sendMessage(ChatColor.YELLOW + "+" + sum_points + ChatColor.WHITE + " points for this " + ChatColor.AQUA + event.getEntity().getName());
            }
            savePoints();
        }
    }



    //cuando el jugador se une
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {


        Player player = event.getPlayer();

        //crear archivo de puntos
        crearArchivoPuntos(player);

        //anyadir nuevo usuario al archivo de puntos
        anyadirNuevoUsuario(player);

        //Descargar cara
        dc.descargarCara(player);

    }

    //guardar usuario en archivo
    public void anyadirNuevoUsuario(Player player){
        // Crear archivo de puntos si no existe
        if (!pointsConfig.contains(player.getName() + "-" + player.getUniqueId())) {
            savePoints();  // Asegúrate de guardar los cambios en el archivo de configuración
        }
    }


    //crear archivo de puntos
    public void crearArchivoPuntos(Player player){



        String completeName = player.getName() + "-" + player.getUniqueId();
        // inicializa los puntos a 75 de todos los jugadores
        playerPoints.putIfAbsent(completeName, 75);


        //crear y cargar archivo de puntos
        pointsFile = new File(getDataFolder(), "points.yml");
        if (!pointsFile.exists()) {
            pointsFile.getParentFile().mkdirs();
            try {
                pointsFile.createNewFile();
                getLogger().info("Score file created");
            } catch (IOException e) {

                getLogger().info("ERROR: Score file couldn't be created");
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
        if (cmd.getName().equalsIgnoreCase("score")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String name = player.getName();
                int points = playerPoints.getOrDefault(name + "-" + player.getUniqueId(), 0);
                player.sendMessage("Your score: " + ChatColor.YELLOW + points);
            } else {
                sender.sendMessage("This command can only be used by players.");
            }
            return true;
            //darpuntos, da o quita puntos
        } else if (cmd.getName().toLowerCase().startsWith("givescore")){

            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }

            if (!sender.hasPermission("logros_puntuacion.admin")) {
                sender.sendMessage("You lack of permission to execute this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("Incorrect use: /givscore <player> <score>");
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                sender.sendMessage("The specified player isn't online.");
                return true;
            }

            int pointsToAdd;
            try {
                pointsToAdd = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("The score must be a valid number.");
                return true;
            }

            int puntosOG = playerPoints.getOrDefault(targetPlayer.getName() + "-" + targetPlayer.getUniqueId(), 0);

            int puntosTotales = puntosOG + pointsToAdd;

            playerPoints.put(targetPlayer.getName() + "-" + targetPlayer.getUniqueId(), puntosTotales);

            if (pointsToAdd >= 0) {
                sender.sendMessage("Score added to user " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.WHITE + ": " + ChatColor.YELLOW + pointsToAdd + ChatColor.WHITE + ". Total Score: " + ChatColor.YELLOW + puntosTotales);
            } else {
                int puntosPos = pointsToAdd * -1;
                sender.sendMessage("Score taken away from user " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.WHITE + ": " + ChatColor.RED + puntosPos + ChatColor.WHITE + ". Total Score: " + ChatColor.YELLOW + puntosTotales);
            }
            if (pointsToAdd > 0) {
                targetPlayer.sendMessage(ChatColor.GREEN + "Score added to user " + targetPlayer.getName() + ": " + pointsToAdd + ". Total Score: " + puntosTotales);
            }  else if (pointsToAdd < 0){
                int puntosPos = pointsToAdd * -1;
                targetPlayer.sendMessage(ChatColor.RED + "Score taken away from user " + targetPlayer.getName() + ": " + puntosPos + ". Total Score: " + puntosTotales);
            } else {
                targetPlayer.sendMessage("Score added to user " + targetPlayer.getName() + ": " + pointsToAdd + ". Total Score: " + puntosTotales);

            }
            savePoints();

            return true;

            //setpuntos, hace que el usuario tenga ciertos puntos
        } else if (cmd.getName().toLowerCase().startsWith("setscore")){

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!sender.hasPermission("logros_puntuacion.admin")) {
            sender.sendMessage("You lack of permission to execute this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Incorrect use: /setscore <player> <score>");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage("The specified player isn't online.");
            return true;
        }

        int pointsToAdd;
        try {
            pointsToAdd = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("The score must be a valid number.");
            return true;
        }

        int puntosAnteriores = playerPoints.getOrDefault(targetPlayer.getName() + "-" + targetPlayer.getUniqueId(), 0);

        playerPoints.put(targetPlayer.getName() + "-" + targetPlayer.getUniqueId(), pointsToAdd);


        sender.sendMessage("Score set to user " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.WHITE + ": " + ChatColor.YELLOW + pointsToAdd);

        if (puntosAnteriores < pointsToAdd) {
            targetPlayer.sendMessage(ChatColor.GREEN + "Score set to user " + targetPlayer.getName() + ": " + pointsToAdd);
        } else if (puntosAnteriores > pointsToAdd){
            targetPlayer.sendMessage(ChatColor.RED + "Score set to user " + targetPlayer.getName() + ": " + pointsToAdd);
        }

        savePoints();

        return true;
    //para ver los puntos
    }  else if (cmd.getName().toLowerCase().startsWith("seescore")){

            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players");
                return true;
            }

            if (!sender.hasPermission("logros_puntuacion.admin")) {
                sender.sendMessage("You lack of permission to execute this command");
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage("Incorrect use: /seescore <player>");
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage("The specified player isn't online.");
                return true;
            }


            int puntos = playerPoints.getOrDefault(targetPlayer.getName() + "-" + targetPlayer.getUniqueId(), 0);

            sender.sendMessage("The player " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.WHITE + "'s score is " + ChatColor.YELLOW + puntos);

            return true;

        }
        return false;
    }


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

            player.sendMessage("You obtained an Advancement! [" + ChatColor.AQUA + logros.remodelateAchievement(event.getAdvancement().getKey().getKey()) + ChatColor.WHITE + "] for " + ChatColor.YELLOW + sum_points + ChatColor.WHITE + " points, Total Score: " + ChatColor.YELLOW + newPoints + ChatColor.WHITE);

        }
    }
}
