package Games;

import java.io.*;
import java.util.ArrayList;
public class PGN {
    public static void main(String[] args) {
        final File folder = new File("PGNS");
        ArrayList<String> files = listFilesForFolder(folder);
        try {
            File writeFile = new File("Games.txt");
            FileWriter fw = new FileWriter(writeFile);
            BufferedWriter bfw = new BufferedWriter(fw);
            for (String fileName : files) {
                File f = new File("PGNS/" + fileName);
                FileReader fr = new FileReader(f);
                BufferedReader bfr = new BufferedReader(fr);
                String s = bfr.readLine();
                while (s != null) {
                    String original = s;
                    if (s.contains("Date") || s.contains("EventDate")) {
                        s = bfr.readLine();
                        continue;
                    }
                    if (!s.contains("[")) {
                        s = s.replaceAll("\\d*\\. ", "");
                        s = s.replaceAll("\\d*\\.", "");
                        s = s.replaceAll("  1-0", " 1-0");
                        s = s.replaceAll("  0-1", " 0-1");
                        s = s.replaceAll("  1/2-1/2", " 1/2-1/2");
                        bfw.write(s);
                    }
                    if (original.contains(" 1-0") || original.contains(" 0-1") || original.contains(" 1/2-1/2")) {
                        s = bfr.readLine();
                        if (s != null) {
                            bfw.write("\n");
                        }
                        continue;
                    }
                    if (original.matches("(.*)\\d*\\. ?[a-zA-Z](.*)") && !original.contains("["))  {
                        if (original.charAt(original.length()-1) != '.') {
                            bfw.write(" ");
                        }
                    }
                    s = bfr.readLine();
                }
                bfr.close();
            }
            bfw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                files.add(fileEntry.getName());
            }
        }
        return files;
    }
}
