package hardware;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import com.google.gson.Gson;

public class CPUInfo {
    private double utilization; // percentuale
    private double frequency;   // GHz
    private int cores;

    public CPUInfo(double utilization, double frequency, int cores) {
        this.utilization = utilization;
        this.frequency = frequency;
        this.cores = cores;
    }

    /**
     * Factory method per creare un oggetto CPUInfo dai dati di OSHI 6.9.1
     */
    public static CPUInfo fromOSHI(CentralProcessor cpu) {
        double utilization = cpu.getSystemCpuLoad() * 100; // OSHI restituisce frazione 0-1
        double frequency = cpu.getMaxFreq() / 1_000_000_000.0; // Hz → GHz
        int cores = cpu.getLogicalProcessorCount();
        return new CPUInfo(utilization, frequency, cores);
    }

    /**
     * Esporta l'oggetto come JSON usando Gson 2.10.1
     */
    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Stampa le informazioni in console in forma leggibile
     */
    public void printInfo() {
        System.out.println("=== CPU Info ===");
        System.out.println("Utilization: " + utilization + "%");
        System.out.println("Frequency: " + frequency + " GHz");
        System.out.println("Cores: " + cores);
        System.out.println();
    }
}
