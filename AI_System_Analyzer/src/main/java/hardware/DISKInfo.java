package hardware;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import com.google.gson.Gson;

import java.util.*;

public class DISKInfo {
    private final SystemInfo si;

    public DISKInfo(SystemInfo si) {
        this.si = si;
    }

    public List<Map<String, Object>> getData() {
        List<Map<String, Object>> disksList = new ArrayList<>();

        for (HWDiskStore disk : si.getHardware().getDiskStores()) {
            Map<String, Object> diskData = new HashMap<>();

            // Informazioni reali disponibili
            diskData.put("name", disk.getName());
            diskData.put("model", disk.getModel());
            diskData.put("serial", disk.getSerial());
            diskData.put("sizeBytes", disk.getSize());
            diskData.put("reads", disk.getReads());
            diskData.put("writes", disk.getWrites());
            diskData.put("transferTime", disk.getTransferTime());

            // Tipo di disco (SSD o HDD) determinato dal modello
            String type = disk.getModel().toLowerCase().contains("ssd") ? "SSD" : "HDD";
            diskData.put("type", type);
            disksList.add(diskData);
        }

        return disksList;
    }

    public String toJson() {
        return new Gson().toJson(getData());
    }
}

