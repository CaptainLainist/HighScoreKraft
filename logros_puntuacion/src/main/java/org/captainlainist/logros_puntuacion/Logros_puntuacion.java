package org.captainlainist.logros_puntuacion;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.ChatColor;
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

import org.bukkit.event.block.BlockPlaceEvent;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.event.player.PlayerPickupItemEvent;
public final class Logros_puntuacion extends JavaPlugin implements Listener, CommandExecutor {


    //Set para saber que bloques son virgenes i cuales no (guarda los no virgenes)
    private final Set<String> placedBlocks = new HashSet<>();

    //guarda los bloques no virgenes puestos en el set
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
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

    //Mapa con los usuarios (por nombre) y sus puntos
    private Map<String, Integer> playerPoints = new HashMap<>();

    //archivo de puntos
    private File pointsFile;
    private FileConfiguration pointsConfig;


    //al encenderse el plugin
    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("puntos").setExecutor(this);
        this.getCommand("darpuntos").setExecutor(this);
        this.getCommand("setpuntos").setExecutor(this);
        getLogger().info("Puntuacion_logros se ha habilitado");

    }

    //al desactivarse
    @Override
    public void onDisable() {
        getLogger().info("Puntuacion_logros se ha deshabilitado");
    }


    //cuando el jugador se une
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        String name = player.getName();
        // inicializa los puntos a 75 de todos los jugadores
        playerPoints.putIfAbsent(name, 75);

        //crea el archivo "points.yml" o lo carga (es donde se guardan los puntos de los usuarios)
        pointsFile = new File(JavaPlugin.getPlugin(Logros_puntuacion.class).getDataFolder(), "points.yml");
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
                int points = playerPoints.getOrDefault(name, 0);
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

            int puntosOG = playerPoints.getOrDefault(targetPlayer.getName(), 0);

            int puntosTotales = puntosOG + pointsToAdd;

            playerPoints.put(targetPlayer.getName(), puntosTotales);

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

        int puntosAnteriores = playerPoints.getOrDefault(targetPlayer.getName(), 0);

        playerPoints.put(targetPlayer.getName(), pointsToAdd);


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
    private int getSumPoints(String achievement) {
        int points;

        switch (achievement) {
            case "story/upgrade_tools":
            case "story/smelt_iron":
            case "story/iron_tools":
                points = 40;
                break;

            case "story/obtain_armor":
            case "story/mine_diamond":
            case "nether/obtain_ancient_debris":
            case "nether/fast_travel":
            case "nether/ride_strider":
                points = 50;
                break;

            case "story/lava_bucket":
            case "story/follow_ender_eye":
            case "nether/distract_piglin":
            case "nether/brew_potion":
                points = 60;
                break;

            case "story/deflect_arrow":
            case "story/form_obsidian":
            case "story/shiny_gear":
            case "story/enchant_item":
            case "nether/obtain_crying_obsidian":
            case "nether/netherite_armor":
                points = 80;
                break;

            case "nether/get_wither_skull":
                points = 90;
                break;

            case "story/cure_zombie_villager":
            case "story/enter_the_end":
            case "nether/summon_wither":
                points = 100;
                break;

            case "end/kill_dragon":
            case "end/dragon_egg":
            case "end/enter_end_gateway":
            case "end/respawn_dragon":
                points = 150;
                break;

            case "adventure/voluntary_exile":
            case "adventure/kill_all_mobs":
            case "adventure/adventuring_time":
            case "adventure/hero_of_the_village":
                points = 120;
                break;

            case "husbandry/bred_all_animals":
            case "husbandry/complete_catalogue":
            case "husbandry/balanced_diet":
                points = 100;
                break;

            case "nether/all_potions":
            case "nether/all_effects":
                points = 200;
                break;

            default:
                points = 30;
                break;
        }

        return points;
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

    //cuano se rompe un bloque
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        String blockLocation = event.getBlock().getLocation().toString();

        Player player = event.getPlayer();

        //si no es virgen se anula
        if (isBlockPlaced(blockLocation)){
            return;
        }

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
            player.sendMessage(ChatColor.YELLOW + "+" + sum_points + ChatColor.WHITE + " puntos!");

            //aplica y guarda los puntos
            int newPoints = playerPoints.getOrDefault(player.getName(), 0) + sum_points;
            playerPoints.put(player.getName(), newPoints);

            savePoints();

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


    //cambia la string del achievement por algo mas leible "minecraft/hacer_una_cosa" -> "Hacer Una Cosa"
    public String remodelateAchievement(String cadena){

        String cadena_2 = "";
        String cadena_3 = "";
        int puntoCortar = 0;
        //detecta la posicion de "/"
        for (int i = 0; i < cadena.length(); i++){
            if (cadena.charAt(i) == '/'){
                puntoCortar = i;
                break;
            }
        }

        //corta por esa posicion y de paso intercambia todos los "_" por espacios
        cadena_2 = cadena.substring(puntoCortar+1).replace("_", " ");


        //pone todas las palabras con la primera letra en mayusculas
        for (int i = 0; i < cadena_2.length(); i++){

            if (i == 0){
                cadena_3 += Character.toString(cadena_2.charAt(i)).toUpperCase();
            } else if (cadena_2.charAt(i-1) == ' '){
                cadena_3 += Character.toString(cadena_2.charAt(i)).toUpperCase();
            } else {
                cadena_3 += Character.toString(cadena_2.charAt(i));
            }

        }

        return cadena_3;

    }

    //al hacerse un achievement
    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();


        Advancement advancement = event.getAdvancement();
        String advancementName = advancement.getKey().getKey();
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        //si el achievement esta hecho
        if (progress.isDone()) {
            //si el achievement es de una de estas categorias
            if (advancementName.startsWith("adventure") || advancementName.startsWith("story") || advancementName.startsWith("minecraft") || advancementName.startsWith("nether") || advancementName.startsWith("end") || advancementName.startsWith("husbandry") ) {

                //suma puntos
                int sum_points = getSumPoints(advancementName);

                int newPoints = playerPoints.getOrDefault(playerName, 0) + sum_points;
                playerPoints.put(playerName, newPoints);

                savePoints();

                player.sendMessage("Has conseguido un logro! [" + ChatColor.AQUA + remodelateAchievement(advancementName) + ChatColor.WHITE + "] por " + ChatColor.YELLOW + sum_points + ChatColor.WHITE + " puntos, Puntos Totales : " + ChatColor.YELLOW + newPoints + ChatColor.WHITE);
            }

        }
    }
}
