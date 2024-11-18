package net.torguet.xlsx.old;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

public class ColorReader {
    private final Process colorReader;
    private final PrintStream colorPS;
    private final BufferedReader colorBF;

    public ColorReader(String filename) throws Exception {
        Process venv = Runtime.getRuntime().exec("/opt/homebrew/bin/python3.12 -m venv ./venv");
        venv.waitFor();

        Process pip = Runtime.getRuntime().exec("./venv/bin/python3.12 -m pip install openpyxl");
        pip.waitFor();


        ArrayList<String> list = new ArrayList<>();
        list.add("./venv/bin/python3.12");
        list.add("colorReader.py");
        list.add("EDT S5 STRI 1A L3 2024-2025.xlsx");
        ProcessBuilder pb = new ProcessBuilder(list);
        pb.redirectErrorStream(true);

        colorReader = pb.start();

        colorPS = new PrintStream(colorReader.getOutputStream());
        colorBF = new BufferedReader(new InputStreamReader(colorReader.getInputStream()));

        colorBF.readLine(); // prompt
    }

    public String getColor(int row, int col) {
        colorPS.println(""+row+" "+col);
        colorPS.flush();

        try {
            String line = colorBF.readLine();
            return line;
        } catch (IOException e) {
            return null;
        }
    }

    public void close() {
        colorPS.println("FIN");
        colorPS.flush();
    }

}
