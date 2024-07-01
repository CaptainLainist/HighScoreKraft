package org.captainlainist.logros_puntuacion;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class Achievements {

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

    //obtener achievement points
    private int getAchievementPoints(String achievement) {
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
                points = 1500;
                break;

            case "nether/get_wither_skull":
                points = 1000;
                break;

            case "story/cure_zombie_villager":
            case "story/enter_the_end":
            case "nether/summon_wither":
                points = 1200;
                break;

            case "end/kill_dragon":
            case "end/dragon_egg":
            case "end/enter_end_gateway":
            case "end/respawn_dragon":
                points = 2000;
                break;

            case "adventure/voluntary_exile":
            case "adventure/kill_all_mobs":
            case "adventure/adventuring_time":
            case "adventure/hero_of_the_village":
                points = 1300;
                break;

            case "husbandry/bred_all_animals":
            case "husbandry/complete_catalogue":
            case "husbandry/balanced_diet":
                points = 100;
                break;

            case "nether/all_potions":
            case "nether/all_effects":
                points = 920;
                break;

            default:
                points = 30;
                break;
        }

        return points;
    }

    //cuando se hace el achievement
    public int achievementDone(PlayerAdvancementDoneEvent event){
        Player player = event.getPlayer();



        Advancement advancement = event.getAdvancement();
        String advancementName = advancement.getKey().getKey();
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        //si el achievement esta hecho
        if (progress.isDone()) {
            //si el achievement es de una de estas categorias
            if (advancementName.startsWith("adventure") || advancementName.startsWith("story") || advancementName.startsWith("minecraft") || advancementName.startsWith("nether") || advancementName.startsWith("end") || advancementName.startsWith("husbandry") ) {

                //suma puntos
                int sum_points = getAchievementPoints(advancementName);

                return sum_points;

            }

            return 0;

        }

        return 0;
    }

}
