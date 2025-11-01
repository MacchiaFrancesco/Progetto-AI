package hardware;

import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import com.google.gson.Gson;

import java.util.*;

public class GPUInfo {
    private final SystemInfo si;

    public GPUInfo(SystemInfo si) {
        this.si = si;
    }

    public List<Map<String, Object>> getData() {
        List<Map<String, Object>> gpus = new ArrayList<>();

        for (GraphicsCard gpu : si.getHardware().getGraphicsCards()) {
            Map<String, Object> gpuData = new HashMap<>();

            // Informazioni reali disponibili tramite OSHI
            gpuData.put("name", gpu.getName());
            gpuData.put("vendor", gpu.getVendor());
            gpuData.put("deviceId", gpu.getDeviceId());
            gpuData.put("VRAMBytes", gpu.getVRam());
            gpuData.put("versionInfo", gpu.getVersionInfo());
            gpus.add(gpuData);
        }

        return gpus;
    }

    public String toJson() {
        return new Gson().toJson(getData());
    }
}
