package org.captainlainist.logros_puntuacion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.nio.file.Paths;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.URL;
public class DescargaCaras {

    //obtener URL de la skin
    private String getSkinURL(UUID uuid) throws Exception {
        try {
            String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "");
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            JsonObject properties = jsonObject.getAsJsonArray("properties").get(0).getAsJsonObject();
            String value = properties.get("value").getAsString();

            String decoded = new String(java.util.Base64.getDecoder().decode(value));
            JsonObject skinURLJson = JsonParser.parseString(decoded).getAsJsonObject();
            String skinURL = skinURLJson.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();

            return skinURL;
        }
        catch (IOException | JsonParseException | IllegalStateException e){
            return "pirate";
        }
    }

    //descargar la skin
    private BufferedImage downloadSkin(String skinURL) throws Exception {
        URL url = new URL(skinURL);
        BufferedImage skin = ImageIO.read(url);
        return skin;
    }

    //obtener la cara de la skin
    private BufferedImage getFace(BufferedImage skin) {
        // Las coordenadas de la cara en la skin
        int faceX = 8;
        int faceY = 8;
        int faceWidth = 8;
        int faceHeight = 8;

        // Extraer la cara
        BufferedImage face = skin.getSubimage(faceX, faceY, faceWidth, faceHeight);
        return face;
    }



    //descargar la cara
    public void descargarCara(Player player){

        String path = "server_web/static/caras_skins/" + player.getName() + "-" + player.getUniqueId() + ".png";

        //crear carpeta de caras
        File carpeta = new File("server_web/static/caras_skins");

        if (!carpeta.exists()){
            carpeta.mkdirs();
        }


        File file_abrir = new File(path);
        if (!file_abrir.exists()) {
            try {
                String url = getSkinURL(player.getUniqueId());
                //si la cuenta no es pirata
                if (!url.equals("pirate")) {
                    BufferedImage skin = downloadSkin(url);
                    BufferedImage cara = getFace(skin);
                    File file = new File(path);
                    ImageIO.write(cara, "png", file);
                    //si si es pirata
                } else {
                    Path sourcePath = Paths.get("server_web/static/caras_skins/steve-face.png");
                    Path targetPath = Paths.get(path);
                    Files.copy(sourcePath, targetPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }





    }

}
