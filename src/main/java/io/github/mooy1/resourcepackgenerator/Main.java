package io.github.mooy1.resourcepackgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Main {

    public static void main(String[] args) throws InterruptedException {
        Console console = new Console();
        try {
            start(console);
        } catch (Exception e) {
            console.status("Error: " + e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : ""));
            for (StackTraceElement element : e.getStackTrace()) {
                console.print("\tat " + element.toString());
            }
            console.status("\nPlease report this error on Github or Discord.");
        }
        console.status("Close the window to exit...");
    }

    private static void start(Console console) throws IOException, InterruptedException {
        console.status("Welcome to the Resource Pack Generator!");
        console.print("Author: Mooy1");
        console.print("Version: 1.0\n");
        setupConfig(console);
    }

    public static void setupConfig(Console console) throws IOException, InterruptedException {
        console.status("Loading Config...");

        File configFile = new File("pack-config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig(console, configFile);
        } else {
            loadConfig(console, configFile);
        }
    }

    private static void saveDefaultConfig(Console console, File configFile) throws IOException, InterruptedException {
        console.status("Saving default config as 'pack-config.yml'...");

        InputStream in = Main.class.getResourceAsStream("pack-config.yml");
        configFile.createNewFile();
        FileOutputStream out = new FileOutputStream(configFile);
        byte[] buff = new byte[in.available()];
        in.read(buff);
        in.close();
        out.write(buff);
        out.close();

        console.status("Make sure to setup the config before running again!");
    }

    private static void loadConfig(Console console, File configFile) throws IOException, InterruptedException {
        console.status("Reading config...");

        InputStream input = new FileInputStream(configFile);
        byte[] buff = new byte[input.available()];
        input.read(buff);
        input.close();

        String[] lines = new String(buff).split("\n");
        
        if (lines.length < 3) {
            console.status("Invalid Config! Delete it to generate a new one!");
            return;
        }
        
        String name = readStringFromConfig(lines[0], "name");
        String desc = readStringFromConfig(lines[1], "description");
        int format = Integer.parseInt(readStringFromConfig(lines[2], "format"));
        
        console.print("Pack Name: " + name);
        console.print("Pack Description: " + desc);
        console.status("Pack Format: " + format);

        prepareFolder(console, name, desc, format);
    }
    
    private static String readStringFromConfig(String line, String key) {
        return line.substring(0, line.length() - 1).replace(key + ": ", "");
    }

    private static void prepareFolder(Console console, String name, String desc, int format) throws IOException, InterruptedException {
        console.status("Preparing target folder...");
        
        File folder = new File(name);
        
        if (folder.exists()) {
            console.status("There is already a folder with the pack's name, rename or delete it!");
            return;
        }
        
        Files.createDirectory(folder.toPath());
        
        loadTextures(console, folder, format, desc);
    }

    private static void loadTextures(Console console, File folder, int format, String desc) throws IOException, InterruptedException {
        console.status("Loading textures...");

        Pattern texture = Pattern.compile("\\w+.png");
        List<String> ids = new ArrayList<>();
        List<Path> textures = new ArrayList<>();
        Path packPNG = null;

        for (String string : Objects.requireNonNull(folder.getAbsoluteFile().getParentFile().list())) {
            if (texture.matcher(string).matches()) {
                if (packPNG == null && string.equals("pack.png")) {
                    packPNG = Paths.get("pack.png");
                } else {
                    textures.add(Paths.get(string));
                    ids.add(string.substring(0, string.length() - 4).toUpperCase(Locale.ROOT));
                }
            }
        }

        if (packPNG == null) {
            console.status("Missing pack.png image, add one to show in your pack!");
            return;
        }

        Path path = folder.toPath();

        Files.copy(packPNG, path.resolve("pack.png"));

        console.status("Copying textures...");

        for (int i = 0 ; i < textures.size() ; i++) {
            Files.copy(textures.get(i), path.resolve(ids.get(i) + ".png"));
        }

        loadFiles(console, folder, format, desc, ids);
    }

    private static void loadFiles(Console console, File folder, int format, String desc, List<String> ids) throws InterruptedException, IOException {
        console.status("Creating texture files...");

        String mcmMeta = "{\n" +
                "\t\"pack\":{\n" +
                "\t\t\"pack_format\":" + format + ",\n" +
                "\t\t\"description\":\"" + desc + "\"\n" +
                "\t}\n" +
                "}";
        Files.write(new File(folder, "pack.mcmeta").toPath(), mcmMeta.getBytes());

    }

    private static void zip(Console console, File folder) {

    }

    private static void finish(Console console) {

    }

}
