package hardware;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class CPUInfo {
    private final CentralProcessor processor;
    private long[] prevTicks;

    public CPUInfo(SystemInfo si) {
        this.processor = si.getHardware().getProcessor();
        this.prevTicks = processor.getSystemCpuLoadTicks();
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        ProcessorIdentifier id = processor.getProcessorIdentifier();

        // Informazioni generali
        data.put("name", id.getName());
        data.put("vendor", id.getVendor());
        data.put("family", id.getFamily());
        data.put("model", id.getModel());
        data.put("stepping", id.getStepping());
        data.put("physicalCores", processor.getPhysicalProcessorCount());
        data.put("logicalCores", processor.getLogicalProcessorCount());
        data.put("hyperThreading", processor.getLogicalProcessorCount() > processor.getPhysicalProcessorCount());

        // Frequenze
        data.put("maxFrequencyHz", processor.getMaxFreq()); // -1 se non disponibile
        data.put("currentFrequenciesHz", processor.getCurrentFreq());

        // Load CPU
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        double load = processor.getSystemCpuLoadBetweenTicks(prevTicks);
        prevTicks = processor.getSystemCpuLoadTicks();
        data.put("cpuUsage", load);

        return data;
    }

    public String toJson() {
        return new Gson().toJson(getData());
    }
}
