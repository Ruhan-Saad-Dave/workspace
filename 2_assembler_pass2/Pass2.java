import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pass2 {
    private static final List<Integer> symAddresses = new ArrayList<>();

    public static void main(String[] args) {
        try {      
            loadTables();
            generateTargetCode();
            System.out.println("Pass 2 complete. Check target_code.txt.");
        } catch (IOException e) {
            System.err.println("Error during Pass 2: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadTables() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("symtab.txt"))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 2) {
                    symAddresses.add(Integer.parseInt(parts[1]));
                }
            }
        }
    }

    private static void generateTargetCode() throws IOException {
        try (BufferedReader icReader = new BufferedReader(new FileReader("intermediate_code.txt"));
             FileWriter targetWriter = new FileWriter("target_code.txt")) {
            int locationCounter = 0;
            
            String icRegex = "^\\((\\w+),(\\d+)\\)\\s*(?:\\((\\d+)\\))?\\s*(?:\\((C|S|L),(\\d+)\\))?";
            Pattern pattern = Pattern.compile(icRegex);

            String line;
            while ((line = icReader.readLine()) != null) {
                Matcher m = pattern.matcher(line.trim());
                if (!m.find()) continue;

                String type = m.group(1);   // AD, IS, DL
                String code = m.group(2);   // 01, 04, etc.
                String reg = m.group(3);    // 1, 2, etc.
                String opType = m.group(4); // C, S, L
                String opVal = m.group(5);  // 100, 1, 2, 3

                if ("AD".equals(type)) {
                    if ("01".equals(code)) {  // START
                        locationCounter = Integer.parseInt(opVal);
                    } else if ("02".equals(code)) { // END
                        break; // End of program
                    }
                } else if ("DL".equals(type)) {
                    int size = Integer.parseInt(opVal);
                    if ("01".equals(code)) { 
                         locationCounter += size;
                    } else if ("02".equals(code)) { 
                         targetWriter.write(String.format("%d) + 00 0 %03d\n", locationCounter, Integer.parseInt(opVal)));
                         locationCounter++;
                    }
                } else if ("IS".equals(type)) {
                    String opCodeStr = code;
                    String regStr = (reg != null) ? reg : "0"; 
                    int operandAddr = 0;

                    if (opType != null) {
                        int index = Integer.parseInt(opVal);
                        if ("S".equals(opType)) { 
                            if (index > 0 && index <= symAddresses.size()) {
                                operandAddr = symAddresses.get(index - 1);
                            } else {
                                System.err.println("Invalid symbol index: " + index);
                                operandAddr = 999; // Error address
                            }
                        }
                    }
                    
                    targetWriter.write(String.format("%d) + %s %s %03d\n",
                            locationCounter, opCodeStr, regStr, operandAddr));
                    locationCounter++;
                }
            }
        }
    }
}