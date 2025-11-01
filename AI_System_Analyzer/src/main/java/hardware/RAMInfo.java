package hardware;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.PhysicalMemory;
import com.google.gson.Gson;

import java.util.*;

public class RAMInfo {
    private final GlobalMemory memory;

    public RAMInfo(SystemInfo si) {
        this.memory = si.getHardware().getMemory();
    }

    public List<Map<String, Object>> getData() {
        List<Map<String, Object>> ramModules = new ArrayList<>();
        List<PhysicalMemory> modules = memory.getPhysicalMemory();

        for (PhysicalMemory module : modules) {
            Map<String, Object> ramData = new HashMap<>();

            // Dati disponibili tramite OSHI
            ramData.put("capacityBytes", module.getCapacity());
            ramData.put("clockSpeedMHz", module.getClockSpeed());
            ramData.put("manufacturer", module.getManufacturer());
            ramData.put("memoryType", module.getMemoryType()); // DDR3, DDR4, DDR5 ecc. (OSHI 6.9.1)
            ramData.put("serialNumber", module.getSerialNumber());
            ramData.put("partNumber", module.getPartNumber());
            ramData.put("bankLabel", module.getBankLabel());
            ramModules.add(ramData);
        }

        return ramModules;
    }

    public String toJson() {
        return new Gson().toJson(getData());
    }
}


