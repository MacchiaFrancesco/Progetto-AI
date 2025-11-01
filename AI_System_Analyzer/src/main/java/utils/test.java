package utils;

import hardware.CPUInfo;
import hardware.RAMInfo;
import hardware.DISKInfo;
import hardware.NETWORKInfo;
import hardware.PROCESSInfo;

import oshi.SystemInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class test {
    public static void main(String[] args) {
        SystemInfo si = new SystemInfo();

        // Raccogli dati hardware
        CPUInfo cpuInfo = new CPUInfo(si);
        RAMInfo ramInfo = new RAMInfo(si);
        DISKInfo diskInfo = new DISKInfo(si);
        NETWORKInfo networkInfo = new NETWORKInfo(si);
        PROCESSInfo processInfo = new PROCESSInfo(si);

        // Mappa principale
        Map<String, Object> allData = new HashMap<>();
        allData.put("cpu", cpuInfo.getData());
        allData.put("ram", ramInfo.getData());
        allData.put("disks", diskInfo.getData());
        allData.put("network", networkInfo.getData());
        allData.put("processes", processInfo.getData());

        // Gson con Pretty Printing
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(allData);
    }
}
