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

    public ColorReader(String filename, int sheetNumber) throws Exception {
        ArrayList<String> list = new ArrayList<>();
        list.add("python3");
        list.add("-m");
        list.add("venv");
        list.add("./venv");
        ProcessBuilder pb = new ProcessBuilder(list);
        pb.redirectErrorStream(true);
        Process venv = pb.start(); // Runtime.getRuntime().exec("/opt/homebrew/bin/python3.12 -m venv ./venv");
        venv.waitFor();

        list.clear();
        list.add("./venv/bin/python3");
        list.add("-m");
        list.add("pip");
        list.add("install");
        list.add("openpyxl");
        pb = new ProcessBuilder(list);
        pb.redirectErrorStream(true);
        Process pip = pb.start(); // Runtime.getRuntime().exec("./venv/bin/python3.12 -m pip install openpyxl");
        pip.waitFor();

        list.clear();
        list.add("./venv/bin/python3");
        list.add("colorReader.py");
        list.add(filename);
        list.add(""+sheetNumber);
        pb = new ProcessBuilder(list);
        pb.redirectErrorStream(true);

        colorReader = pb.start();

        colorPS = new PrintStream(colorReader.getOutputStream());
        colorBF = new BufferedReader(new InputStreamReader(colorReader.getInputStream()));

        colorBF.readLine(); // prompt
    }

    public String getColor(int row, int col) {
        colorPS.println(row+" "+col);
        colorPS.flush();

        try {
            return colorBF.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public void close() throws InterruptedException {
        colorPS.println("FIN");
        colorPS.flush();
        colorReader.waitFor();
    }

}
