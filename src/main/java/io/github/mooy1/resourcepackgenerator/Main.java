package io.github.mooy1.resourcepackgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Main {

    public static void main(String[] args) throws InterruptedException {
        Console console = new Console();
        try {
            createPack(console);
        } catch (Exception e) {
            console.status("Error: " + e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : ""));
            for (StackTraceElement element : e.getStackTrace()) {
                console.print("\tat " + element.toString());
            }
            console.status("\nPlease report this error on Github or Discord.");
        }
        console.status("Close the window to exit.");
    }

    private static void createPack(Console console) throws Exception {
        // start
        console.status("Welcome to the Slimefun Texture Generator!");
        console.print("Author: Mooy1");
        console.print("Version: 1.1\n");

        // load config
        console.status("Loading Config...");
        File configFile = new File("pack.properties");
        if (!configFile.exists()) {
            console.status("Saving default config as 'pack.properties'...");
            String defaultConfig = "name=CHANGE ME\ndescription=CHANGE ME\n";
            Files.write(configFile.toPath(), defaultConfig.getBytes());
            console.status("Make sure to setup the config before running again!");
            return;
        }

        // read config
        console.status("Reading config...");
        Properties config = new Properties();
        config.load(new FileInputStream(configFile));
        String name = config.getProperty("name");
        if (name == null) {
            console.status("No name found in pack.properties file! Add one and run again.");
            return;
        }
        String desc = config.getProperty("description");
        if (desc == null) {
            console.status("No description found in pack.properties file! Add one and run again.");
            return;
        }
        console.print("Pack Name: " + name);
        console.print("Pack Description: " + desc + "\n");

        // load icon
        console.status("Locating pack icon...");
        File packPNG = new File("pack.png");
        if (!packPNG.exists()) {
            console.status("Missing pack.png image, add one to show in your pack!");
            return;
        }

        // load item textures
        console.status("Locating item textures...");
        File textureFolder = new File("textures");
        File itemsFolder = new File(textureFolder, "items");
        if (!textureFolder.exists() || !textureFolder.isDirectory()) {
            console.status("Couldn't find 'textures' folder! An empty one has been created to add textures to!");
            Files.createDirectories(itemsFolder.toPath());
            return;
        }
        if (!itemsFolder.exists() || !itemsFolder.isDirectory()) {
            console.status("Couldn't find 'items' folder under 'textures' folder! An empty one has been created to add textures to!");
            Files.createDirectories(itemsFolder.toPath());
            return;
        }
        List<File> itemTextures = new ArrayList<>();
        searchForTextures(Pattern.compile("\\w+\\.\\w+\\.png"), console, itemsFolder, itemTextures);

        // load other textures
        console.status("Locating other files...");
        List<File> otherFiles = new ArrayList<>();
        for (File file : Objects.requireNonNull(textureFolder.listFiles())) {
            if (file.isDirectory()) {
                if (!file.getName().equals("items")) {
                    searchForFiles(file, otherFiles);
                }
            } else {
                otherFiles.add(file);
            }
        }

        // prepare zip folder
        console.status("Preparing zip folder...");
        File pack = new File(name + ".zip");
        if (pack.exists()) {
            pack.delete();
        }
        ZipOutputStream zop = new ZipOutputStream(new FileOutputStream(pack));

        // pack icon
        console.status("Copying pack.png...");
        addZipEntry("pack.png", packPNG, zop);

        // pack mcmeta
        console.status("Creating pack.mcmeta...");
        String mcmMeta = "{\n" +
                "\t\"pack\":{\n" +
                "\t\t\"pack_format\":6,\n" +
                "\t\t\"description\":\"" + desc + "\"\n" +
                "\t}\n" +
                "}";
        addZipEntry("pack.mcmeta", mcmMeta.getBytes(), zop);

        // textures
        console.status("Copying and creating files for " + itemTextures.size() + " item texture(s)...");
        String texturePath = "assets/minecraft/optifine/cit/";
        boolean needsPlayerHeadFile = false;
        for (File file : itemTextures) {
            String[] split = file.getName().substring(0, file.getName().length() - 4).toLowerCase(Locale.ROOT).split("\\.");
            addZipEntry(texturePath + split[0] + ".png", file, zop);
            StringBuilder props = new StringBuilder();
            props.append("# Generated by https://github.com/Mooy1/SlimefunTextureGenerator\n");
            props.append("type=item\n");
            props.append("matchItems=").append(split[1]).append('\n');
            if (split[1].equals("player_head")) {
                props.append("model=skull_model\n");
                props.append("texture=").append(split[0]).append('\n');
                needsPlayerHeadFile = true;
            }
            props.append("nbt.PublicBukkitValues.slimefun\\:slimefun_item=").append(split[0].toUpperCase(Locale.ROOT));
            addZipEntry(texturePath + split[0] + ".properties", props.toString().getBytes(), zop);
        }
        if (needsPlayerHeadFile) {
            String playerHeadFile = "{\n" +
                    "\t\"parent\":\"item/generated\",\n" +
                    "\t\"textures\":{\n" +
                    "\t\t\"layer0\":\"items/empty_armor_slot_helmet\"\n" +
                    "\t},\n" +
                    "\t\"display\":{\n" +
                    "\t\t\"gui\":{\n" +
                    "\t\t\t\"scale\":[1,1,1]\n" +
                    "\t\t}\n" +
                    "\t}\n" +
                    "}";
            addZipEntry(texturePath + "skull_model.json", playerHeadFile.getBytes(), zop);
        }
        
        // other files
        console.status("Copying " + otherFiles.size() + " other file(s)...");
        for (File file : otherFiles) {
            addZipEntry(texturePath + file.getName(), file, zop);
        }
        zop.close();

        // done
        console.status("Finished, your texture pack is saved as '" + pack.getName() + "'!");
    }

    private static void addZipEntry(String path, File source, ZipOutputStream zop) throws IOException {
        FileInputStream stream = new FileInputStream(source);
        byte[] data = new byte[stream.available()];
        stream.read(data);
        stream.close();
        addZipEntry(path, data, zop);
    }

    private static void addZipEntry(String path, byte[] data, ZipOutputStream zop) throws IOException {
        zop.putNextEntry(new ZipEntry(path));
        zop.write(data);
        zop.closeEntry();
    }

    private static void searchForTextures(Pattern pattern, Console console, File folder, List<File> textures) throws InterruptedException {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                searchForTextures(pattern, console, file, textures);
            } else if (pattern.matcher(file.getName()).matches()) {
                textures.add(file);
            } else {
                console.status("Item texture '" + file.getName() + "' has an invalid name, format is: '[slimefun_id].[minecraft_id].png'");
            }
        }
    }

    private static void searchForFiles(File folder, List<File> files) {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                searchForFiles(folder, files);
            } else {
                files.add(file);
            }
        }
    }

}
