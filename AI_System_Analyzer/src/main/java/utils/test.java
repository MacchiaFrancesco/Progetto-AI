package utils;

import com.google.gson.Gson;
import oshi.SystemInfo;

public class test {
    public static void main(String[] args) {
        SystemInfo si = new SystemInfo();
        Gson gson = new Gson();

        System.out.println("Funziona tutto");
        System.out.println("CPU: " + si.getHardware().getProcessor().getProcessorIdentifier().getName());
    }
}

