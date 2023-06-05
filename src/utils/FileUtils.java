package utils;

import java.io.*;

public class FileUtils {
    /**
     * Write to a file
     * @param fileName
     * @param data
     * @throws Exception
     */
    public static void writeToFile(String fileName, String data) throws Exception {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(data);

        bw.close();
    }

    /**
     * Read from a file
     * @param fileName
     * @return
     * @throws Exception
     */
    public static String readFile(String fileName) throws Exception {
        String data, line;
        data = line = "";

        File file = new File(fileName);
        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((line = br.readLine()) != null) {
            data += line;
        }
        br.close();

        return data;
    }
}
