package hardware;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import com.google.gson.Gson;

import java.util.*;

public class PROCESSInfo {
    private final OperatingSystem os;

    public PROCESSInfo(SystemInfo si) {
        this.os = si.getOperatingSystem();
    }

    public List<Map<String, Object>> getData() {
        List<Map<String, Object>> processes = new ArrayList<>();
        List<OSProcess> allProcesses = os.getProcesses();
        for (OSProcess p : allProcesses) {
            Map<String, Object> proc = new HashMap<>();
            proc.put("pid", p.getProcessID());
            proc.put("name", p.getName());
            proc.put("user", p.getUser());
            proc.put("cpu", p.getProcessCpuLoadCumulative());
            proc.put("memory", p.getResidentSetSize());
            proc.put("state", p.getState().toString());
            processes.add(proc);
        }
        return processes;
    }

    public String toJson() {
        return new Gson().toJson(getData());
    }
}
