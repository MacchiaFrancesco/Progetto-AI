package antivirus;

import com.google.gson.Gson;

import java.io.*;
import java.util.*;

/**
 * Classe per gestire scansioni antivirus tramite ClamAV.
 * Supporta:
 *  - Scansione di directory o file specifici
 *  - Lettura output di clamscan
 *  - Ritorno dei risultati in formato JSON
 */
public class AntivirusScanner {

    private static final Gson gson = new Gson();

    /**
     * Risultato di una scansione.
     */
    public static class ScanResult {
        public String scannedPath;
        public boolean infected;
        public List<String> infectedFiles = new ArrayList<>();
        public String rawOutput;
        public String clamscanExecutable; // percorso usato per clamscan
    }

    /**
     * Trova l'eseguibile ClamAV.
     * @return percorso dell'eseguibile o null se non trovato
     */
    private String findClamScanExecutable() {
        try {
            ProcessBuilder testPb = new ProcessBuilder("clamscan", "--version");
            testPb.redirectErrorStream(true);
            Process testProcess = testPb.start();
            testProcess.waitFor();
            return "clamscan"; // eseguibile trovato nel PATH
        } catch (Exception ignored) {
        }

        // Percorsi comuni su Windows
        String[] commonPaths = {
            "C:\\Program Files\\ClamAV\\clamscan.exe",
            "C:\\Program Files (x86)\\ClamAV\\clamscan.exe"
        };

        for (String path : commonPaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) return f.getAbsolutePath();
        }

        return null;
    }

    /**
     * Esegue una scansione del percorso specificato con ClamAV.
     */
    public ScanResult scan(String path) {
        ScanResult result = new ScanResult();
        result.scannedPath = path;

        String clamscanExec = findClamScanExecutable();
        result.clamscanExecutable = clamscanExec;

        if (clamscanExec == null) {
            result.rawOutput = "Errore: clamscan non trovato!";
            result.infected = false;
            return result;
        }

        try {
            // Esegui il comando ClamAV
            ProcessBuilder pb = new ProcessBuilder(clamscanExec, "-r", path);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Leggi l'output di ClamAV
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                if (line.contains("FOUND")) {
                    result.infectedFiles.add(line);
                }
            }

            process.waitFor();
            result.rawOutput = output.toString();
            result.infected = !result.infectedFiles.isEmpty();

        } catch (IOException | InterruptedException e) {
            result.rawOutput = "Errore durante la scansione: " + e.getMessage();
            Thread.currentThread().interrupt();
        }

        return result;
    }

    /**
     * Esegue una scansione e restituisce il risultato in formato JSON.
     */
    public String scanToJson(String path) {
        ScanResult result = scan(path);
        return gson.toJson(result);
    }

    public static void main(String[] args) {
        AntivirusScanner scanner = new AntivirusScanner();
        String json = scanner.scanToJson("C:\\Users\\Francesco\\Downloads");
        System.out.println(json);
    }
}
