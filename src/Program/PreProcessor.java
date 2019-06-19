package Program;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class PreProcessor {
    private static ArrayList<String> lines = new ArrayList<>();

    static ArrayList<String> getLines() {
        return lines;
    }

    static void run(String file) {
        System.out.println("PreProcessor...");

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null)
                lines.add(tempString);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int line = 0;
        boolean deleteMode = false;
        boolean defined = false;
        HashMap<String, String> definesList = new HashMap<>();
        for (String str: lines) {
            if (str != null) {
                if (!deleteMode) {
                    if (str.contains("//")) {
                        int startIndex = str.indexOf("//");
                        String newStr = str.substring(0, startIndex);
                        lines.set(line, newStr + " ");
                        str = lines.get(line);
                    }

                    if (str.contains("/*")) {
                        deleteMode = true;
                        int startIndex = str.indexOf("/*");
                        if (str.contains("*/")) {
                            int endIndex = str.indexOf("*/");
                            String StrLeft = str.substring(0, startIndex);
                            String StrRight = str.substring(endIndex + 2);
                            lines.set(line, StrLeft + StrRight + " ");
                            deleteMode = false;
                        } else {
                            String newString = str.substring(0, startIndex);
                            lines.set(line, newString + " ");
                        }
                        str = lines.get(line);
                    }

                    if (defined && str.contains("#else")) {
                        lines.set(line, "");
                        deleteMode = true;
                    }

                    if (str.contains("#endif")) {
                        lines.set(line, "");
                        line++;
                        continue;
                    }

                    if (str.contains("#ifdef")) {
                        String array[] = str.split(" ");
                        defined = definesList.containsKey(array[1]);
                        deleteMode = !defined;
                        lines.set(line, "");
                        line++;
                        continue;
                    }

                    if (str.contains("#define")) {
                        String splitArray[] = str.split(" ");
                        definesList.put(splitArray[1], splitArray[2]);
                        lines.set(line, "");
                    }
                    else {
                        for (Object o : definesList.entrySet()) {
                            Map.Entry pair = (Map.Entry) o;
                            String defineWord = (String) pair.getKey();
                            String defineValue = (String) pair.getValue();
                            if (str.contains(defineWord)) {
                                String newString = str.replace(defineWord, defineValue);
                                lines.set(line, newString + " ");
                            }
                        }
                    }
                }
                else {
                    if (str.contains("*/")) {
                        int endIndex = str.indexOf("*/");
                        String newString = str.substring(endIndex+2);
                        lines.set(line, newString + " ");
                        deleteMode = false;
                    }

                    else if (str.contains("#endif") && defined) {
                        deleteMode = false;
                        defined = false;
                        lines.set(line, "");
                    }

                    else if (str.contains("#else") && !defined) {
                        deleteMode = false;
                        lines.set(line, "");
                    }

                    else
                        lines.set(line, "");
                }
            }
            line++;
        }

        //output the source
        for (String str: lines)
            System.out.println(str);

    }
}
