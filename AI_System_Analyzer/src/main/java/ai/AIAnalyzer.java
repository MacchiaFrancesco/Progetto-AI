package ai;

import antivirus.AntivirusScanner;
import network.NetworkScanner;
import hardware.CPUInfo;
import hardware.DISKInfo;
import hardware.GPUInfo;
import hardware.RAMInfo;
import software.SoftwareInfo;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * AIAnalyzer locale che analizza hardware, software, antivirus e rete.
 * Non invia dati a nessun server.
 * Può anche generare consigli basati sui dati raccolti.
 */
public class AIAnalyzer {

    private CPUInfo cpuInfo;
    private DISKInfo diskInfo;
    private GPUInfo gpuInfo;
    private RAMInfo ramInfo;
    private SoftwareInfo softwareInfo;
    private AntivirusScanner antivirusScanner;
    private NetworkScanner networkScanner;

    private Map<String, Object> hardwareData;
    private Map<String, Object> softwareData;
    private Map<String, Object> antivirusData;
    private Map<String, Object> networkData;

    private final Gson gson = new Gson();

    public AIAnalyzer(CPUInfo cpuInfo, DISKInfo diskInfo, GPUInfo gpuInfo,
                      RAMInfo ramInfo, SoftwareInfo softwareInfo,
                      AntivirusScanner antivirusScanner, NetworkScanner networkScanner) {
        this.cpuInfo = cpuInfo;
        this.diskInfo = diskInfo;
        this.gpuInfo = gpuInfo;
        this.ramInfo = ramInfo;
        this.softwareInfo = softwareInfo;
        this.antivirusScanner = antivirusScanner;
        this.networkScanner = networkScanner;

        this.hardwareData = new HashMap<>();
        this.softwareData = new HashMap<>();
        this.antivirusData = new HashMap<>();
        this.networkData = new HashMap<>();
    }

    /** Raccoglie tutti i dati di base senza scansioni */
    public void collectBaseData() {
        hardwareData.put("CPU", cpuInfo.getData());
        hardwareData.put("GPU", gpuInfo.getData());
        hardwareData.put("RAM", ramInfo.getData());
        hardwareData.put("Disk", diskInfo.getData());

        // Usa toJson() di SoftwareInfo
        softwareData.put("Software", gson.fromJson(softwareInfo.toJson(), Object.class));
    }

    /** Esegue scansione antivirus sul percorso specificato */
    public String runAntivirusScan(String path) {
        String jsonResult = antivirusScanner.scanToJson(path);
        antivirusData.put("LastScan", jsonResult);
        return jsonResult;
    }

    /** Esegue scansione di rete sul target specificato */
    public String runNetworkScan(String target) {
        String jsonResult = networkScanner.scanToJson(target);
        networkData.put("LastScan", jsonResult);
        return jsonResult;
    }

    /** Restituisce tutti i dati raccolti in JSON */
    public String getAllDataAsJson() {
        Map<String, Object> allData = new HashMap<>();
        allData.put("Hardware", hardwareData);
        allData.put("Software", softwareData);
        allData.put("Antivirus", antivirusData);
        allData.put("Network", networkData);
        return gson.toJson(allData);
    }

    /** Risponde a domande su hardware, software, antivirus e rete */
    public String answerQuestion(String question) {
        question = question.toLowerCase();
        if (question.contains("cpu")) return gson.toJson(hardwareData.get("CPU"));
        if (question.contains("gpu")) return gson.toJson(hardwareData.get("GPU"));
        if (question.contains("ram")) return gson.toJson(hardwareData.get("RAM"));
        if (question.contains("disk") || question.contains("storage")) return gson.toJson(hardwareData.get("Disk"));
        if (question.contains("software")) return gson.toJson(softwareData.get("Software"));
        if (question.contains("virus") || question.contains("antivirus")) return gson.toJson(antivirusData.get("LastScan"));
        if (question.contains("network") || question.contains("ip") || question.contains("port")) return gson.toJson(networkData.get("LastScan"));
        return "{\"error\":\"Domanda non riconosciuta. Posso rispondere solo su hardware, software, antivirus e rete locale.\"}";
    }

    /** Genera consigli basati sui dati raccolti e sulla richiesta */
    public String getAdvice(String request) {
        request = request.toLowerCase();
        StringBuilder advice = new StringBuilder();

        // Consigli hardware
        if (request.contains("hardware") || request.contains("performance")) {
            advice.append("Consigli hardware:\n");
            // RAM
            Map<String, Object> ramMap = (Map<String, Object>) hardwareData.get("RAM");
            if (ramMap != null && ramMap.containsKey("total")) {
                long totalRam = (long) ramMap.get("total");
                if (totalRam < 16L * 1024 * 1024 * 1024) {
                    advice.append("- Considera un upgrade della RAM.\n");
                }
            }
            // Disco
            Map<String, Object> diskMap = (Map<String, Object>) hardwareData.get("Disk");
            if (diskMap != null && diskMap.containsKey("freeSpace")) {
                long freeSpace = (long) diskMap.get("freeSpace");
                if (freeSpace < 50L * 1024 * 1024 * 1024) {
                    advice.append("- Pochi GB liberi, valuta pulizia o upgrade.\n");
                }
            }
        }

        // Consigli antivirus
        if (request.contains("antivirus") || request.contains("virus") || request.contains("security")) {
            advice.append("Consigli sicurezza:\n");
            if (antivirusData.containsKey("LastScan")) {
                advice.append("- Esegui regolarmente scansioni antivirus.\n");
            } else {
                advice.append("- Nessuna scansione antivirus recente rilevata.\n");
            }
        }

        // Consigli rete
        if (request.contains("network") || request.contains("ip") || request.contains("porta")) {
            advice.append("Consigli rete:\n");
            advice.append("- Mantieni il firewall attivo e monitora le porte.\n");
        }

        if (advice.length() == 0) {
            return "{\"error\":\"Non ho consigli per questa richiesta specifica.\"}";
        }

        return advice.toString();
    }
}