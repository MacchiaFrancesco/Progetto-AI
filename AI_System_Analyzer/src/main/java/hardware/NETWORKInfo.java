package hardware;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import com.google.gson.Gson;

import java.util.*;

public class NETWORKInfo {
    private final SystemInfo si;

    public NETWORKInfo(SystemInfo si) {
        this.si = si;
    }

    public List<Map<String, Object>> getData() {
        List<Map<String, Object>> nets = new ArrayList<>();
        for (NetworkIF net : si.getHardware().getNetworkIFs()) {
            Map<String, Object> n = new HashMap<>();
            n.put("name", net.getName());
            n.put("mac", net.getMacaddr());
            n.put("ipv4", net.getIPv4addr());
            n.put("ipv6", net.getIPv6addr());
            n.put("speed", net.getSpeed());
            nets.add(n);
        }
        return nets;
    }

    public String toJson() {
        return new Gson().toJson(getData());
    }
}
