package software;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class SoftwareInfo {

    private static final Gson gson = new Gson();

    private String osName;
    private String osVersion;
    private String architecture;
    private List<Program> installedPrograms;
    private List<Driver> installedDrivers;

    public static class Program {
        public String name;
        public String version;
        public String installLocation;
        public String publisher;
    }

    public static class Driver {
        public String name;
        public String displayName;
        public String version;
        public String provider;
    }

    public SoftwareInfo() {
        installedPrograms = new ArrayList<>();
        installedDrivers = new ArrayList<>();
        detectOS();
        detectInstalledPrograms();
        detectDrivers();
    }

    private void detectOS() {
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        architecture = System.getProperty("os.arch");
    }

    /**
     * Rileva programmi installati dal registro di Windows
     */
    private void detectInstalledPrograms() {
        try {
            String[] keys = {
                "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
                "HKLM\\SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
                "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"
            };

            for (String key : keys) {
                ProcessBuilder pb = new ProcessBuilder("reg", "query", key);
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("HKEY")) {
                        Program program = queryProgramDetails(line);
                        if (program != null) {
                            installedPrograms.add(program);
                        }
                    }
                }
                process.waitFor();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Estrae nome, versione, publisher e percorso di installazione da una chiave di registro
     */
    private Program queryProgramDetails(String regKey) {
        Program program = new Program();
        try {
            String[] values = {"DisplayName", "DisplayVersion", "InstallLocation", "Publisher"};
            ProcessBuilder pb;
            BufferedReader reader;
            boolean hasName = false;

            for (String val : values) {
                pb = new ProcessBuilder("reg", "query", regKey, "/v", val);
                Process process = pb.start();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith(val)) {
                        String[] parts = line.split("    ");
                        if (parts.length >= 3) {
                            String value = parts[parts.length - 1].trim();
                            switch (val) {
                                case "DisplayName": program.name = value; hasName = true; break;
                                case "DisplayVersion": program.version = value; break;
                                case "InstallLocation": program.installLocation = value; break;
                                case "Publisher": program.publisher = value; break;
                            }
                        }
                    }
                }
                process.waitFor();
            }

            if (hasName) return program;

        } catch (Exception e) {
            // ignoriamo entry senza nome
        }
        return null;
    }

    /**
     * Rileva i driver installati usando il comando driverquery
     */
    private void detectDrivers() {
        try {
            ProcessBuilder pb = new ProcessBuilder("driverquery", "/FO", "CSV", "/V");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine(); // intestazioni
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 8) {
                    Driver driver = new Driver();
                    driver.displayName = parts[0];
                    driver.name = parts[1];
                    driver.version = parts[4];
                    driver.provider = parts[5];
                    installedDrivers.add(driver);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parser semplice CSV per driverquery
     */
    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else sb.append(c);
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    /**
     * Restituisce tutti i dati in JSON leggibile dall'AI
     */
    public String toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("osName", osName);
        obj.addProperty("osVersion", osVersion);
        obj.addProperty("architecture", architecture);

        JsonArray programsArr = new JsonArray();
        for (Program p : installedPrograms) {
            JsonObject pObj = new JsonObject();
            pObj.addProperty("name", p.name);
            pObj.addProperty("version", p.version);
            pObj.addProperty("installLocation", p.installLocation);
            pObj.addProperty("publisher", p.publisher);
            programsArr.add(pObj);
        }
        obj.add("installedPrograms", programsArr);

        JsonArray driversArr = new JsonArray();
        for (Driver d : installedDrivers) {
            JsonObject dObj = new JsonObject();
            dObj.addProperty("displayName", d.displayName);
            dObj.addProperty("name", d.name);
            dObj.addProperty("version", d.version);
            dObj.addProperty("provider", d.provider);
            driversArr.add(dObj);
        }
        obj.add("installedDrivers", driversArr);

        return gson.toJson(obj);
    }

    public static void main(String[] args) {
        SoftwareInfo sw = new SoftwareInfo();
        System.out.println(sw.toJson());
    }
}