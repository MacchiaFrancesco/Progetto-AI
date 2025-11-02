package network;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

/**
 * NetworkScanner con fallback a percorsi assoluti di nmap.
 */
public class NetworkScanner {

    private final Gson gson = new Gson();

    /**
     * Scansione Nmap su un intervallo IP o singolo host.
     * Se nmap non è nel PATH, prova percorsi assoluti comuni.
     *
     * @param target es: "192.168.1.0/24" oppure "scanme.nmap.org"
     * @return JSON con i risultati della scansione o errore
     */
    public String scanToJson(String target) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> hosts = new ArrayList<>();

        String nmapExec = findNmapExecutable();
        if (nmapExec == null) {
            result.put("status", "failed");
            result.put("error", "nmap non trovato. Assicurati che nmap sia installato o specifica il percorso assoluto.");
            return gson.toJson(result);
        }

        if (target == null || target.isBlank()) {
            result.put("status", "failed");
            result.put("error", "Nessun target specificato.");
            return gson.toJson(result);
        }

        try {
            // Costruisci il comando usando l'eseguibile risolto
            List<String> cmd = new ArrayList<>();
            cmd.add(nmapExec);
            cmd.add("-sS");
            cmd.add("-T4");
            cmd.add("-p");
            cmd.add("1-1024");
            cmd.add(target);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Map<String, String> currentHost = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("Nmap scan report for")) {
                    if (currentHost != null) {
                        hosts.add(currentHost);
                    }
                    currentHost = new HashMap<>();
                    currentHost.put("host", line.replace("Nmap scan report for", "").trim());
                } else if (line.startsWith("Host is up")) {
                    if (currentHost != null)
                        currentHost.put("status", "up");
                } else if (line.contains("/tcp")) {
                    // esempio riga: 80/tcp open  http
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 3) {
                        String port = parts[0];
                        String state = parts[1];
                        String service = parts[2];
                        currentHost.put(port, state + " (" + service + ")");
                    }
                }
            }

            if (currentHost != null) {
                hosts.add(currentHost);
            }

            int exit = process.waitFor();

            result.put("nmapExecutable", nmapExec);
            result.put("target", target);
            result.put("hosts", hosts);
            result.put("status", exit == 0 ? "completed" : "completed_with_errors");
            result.put("exitCode", exit);

        } catch (Exception e) {
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }

        return gson.toJson(result);
    }

    /**
     * Cerca un eseguibile nmap funzionante:
     * 1) prova "nmap" (PATH)
     * 2) prova alcuni percorsi assoluti comuni su Windows
     * @return comando/eseguibile da usare, o null se non trovato
     */
    private String findNmapExecutable() {
        // 1) prova "nmap" dal PATH
        try {
            ProcessBuilder pb = new ProcessBuilder("nmap", "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int code = p.waitFor();
            if (code == 0) return "nmap";
        } catch (Throwable ignored) {
        }

        // 2) percorsi assoluti comuni (Windows)
        String[] candidates = new String[]{
                "C:\\Program Files (x86)\\Nmap\\nmap.exe",
                "C:\\Program Files\\Nmap\\nmap.exe",
                "C:\\Program Files (x86)\\Nmap\\nmap",
                "C:\\Program Files\\Nmap\\nmap"
        };

        for (String c : candidates) {
            File f = new File(c);
            if (f.exists() && f.canExecute()) {
                return c;
            }
        }

        // non trovato
        return null;
    }

    public static void main(String[] args) {
        NetworkScanner scanner = new NetworkScanner();
        String target = args.length > 0 ? args[0] : "192.168.1.0/24";
        System.out.println(scanner.scanToJson(target));
    }
}