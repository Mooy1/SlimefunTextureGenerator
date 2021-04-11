package io.github.mooy1.resourcepackgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
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
        console.status("Close the window to exit...");
    }

    private static void createPack(Console console) throws Exception {
        // start
        console.status("Welcome to the Slimefun Texture Generator!");
        console.print("Author: Mooy1");
        console.print("Version: 1.0\n");

        // load config
        console.status("Loading Config...");
        File configFile = new File("pack-config.yml");
        if (!configFile.exists()) {
            console.status("Saving default config as 'pack-config.yml'...");
            String defaultConfig = "name: CHANGE ME\ndescription: CHANGE ME\n";
            Files.write(configFile.toPath(), defaultConfig.getBytes());
            console.status("Make sure to setup the config before running again!");
            return;
        }

        // read config
        console.status("Reading config...");
        InputStream input = new FileInputStream(configFile);
        byte[] buff = new byte[input.available()];
        input.read(buff);
        input.close();
        String[] lines = new String(buff).split("\n");
        if (lines.length < 2) {
            console.status("Invalid Config! Delete it to generate a new one!");
            return;
        }
        String name = lines[0].replace("name: ", "");
        String desc = lines[1].replace("description: ", "");
        console.print("Pack Name: " + name);
        console.print("Pack Description: " + desc + "\n");

        // load textures
        console.status("Loading textures...");
        Set<File> textures = new HashSet<>();
        searchForTextures(Pattern.compile("\\w+.png"), configFile.getAbsoluteFile().getParentFile(), textures);
        File packPNG = new File("pack.png");
        if (!packPNG.exists()) {
            console.status("Missing pack.png image, add one to show in your pack!");
            return;
        }

        // prepare zip folder
        console.status("Preparing pack folder...");
        File pack = new File(name + ".zip");
        if (pack.exists()) {
            pack.delete();
        }
        ZipOutputStream zop = new ZipOutputStream(new FileOutputStream(pack));

        // copying and creating files
        console.status("Creating, copying, and zipping texture files...");
        addZipEntry("pack.png", packPNG, zop);
        String mcmMeta = "{\n" +
                "\t\"pack\":{\n" +
                "\t\t\"pack_format\":6,\n" +
                "\t\t\"description\":\"" + desc + "\"\n" +
                "\t}\n" +
                "}";
        addZipEntry("pack.mcmeta", mcmMeta.getBytes(), zop);
        for (File file : textures) {
            String id = file.getName().substring(0, file.getName().length() - 4).toUpperCase(Locale.ROOT);
            addZipEntry("minecraft/optifine/cit/" + id + ".png", file, zop);
            String props = "type=item\n" +
                    "items=iron_ingot\n" + // TODO add
                    "nbt.PublicBukkitValues.*=slimefun:slimefun_item:" + id; // TODO test
            addZipEntry("minecraft/optifine/cit/" + id + ".properties", props.getBytes(), zop);
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

    private static void searchForTextures(Pattern textureMatcher, File folder, Set<File> textures) {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                searchForTextures(textureMatcher, file, textures);
            } else if (textureMatcher.matcher(file.getName()).matches() && !file.getName().equals("pack.png")) {
                textures.add(file);
            }
        }
    }

}
