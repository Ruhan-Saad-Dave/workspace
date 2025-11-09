import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pass2 {
    private static Map<String, MNTEntry> mnt = new HashMap<>();
    private static List<String> mdt = new ArrayList<>();
    private static List<String> intermediateCode = new ArrayList<>();

    static class MNTEntry {
        int mdtp;
        List<String> params; 
        MNTEntry(int mdtp, List<String> params) { 
            this.mdtp = mdtp; 
            this.params = params;
        }
    }

    public static void main(String[] args) throws IOException {
        loadTables();
        expandMacros();
        
        System.out.println("Macro Pass 2 complete. Check generated file: expanded.asm");
    }

    private static void loadTables() throws IOException {
        mdt.add(""); 
        try (BufferedReader r = new BufferedReader(new FileReader("MDT.txt"))) {
            r.readLine(); // Skip header
            String line;
            while ((line = r.readLine()) != null) {
                String definition = line.split("\t", 2)[1];
                mdt.add(definition);
            }
        }

        try (BufferedReader r = new BufferedReader(new FileReader("MNT.txt"))) {
            r.readLine(); // Skip header
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split("\t");
                int mdtp = Integer.parseInt(parts[0]); 
                String name = parts[1];
                List<String> params = findParamsInMDT(mdtp);
                mnt.put(name, new MNTEntry(mdtp, params));
            }
        }

        try (BufferedReader r = new BufferedReader(new FileReader("intermediate.asm"))) {
            String line;
            while ((line = r.readLine()) != null) {
                intermediateCode.add(line);
            }
        }
    }

    private static List<String> findParamsInMDT(int mdtp) {
        Set<String> foundParams = new LinkedHashSet<>();
        Pattern p = Pattern.compile("&(\\w+)"); 

        for (int i = mdtp; i < mdt.size(); i++) {
            String line = mdt.get(i);
            if (line.contains("MEND")) {
                break;
            }
            Matcher m = p.matcher(line);
            while (m.find()) {
                foundParams.add(m.group(0)); 
            }
        }
        return new ArrayList<>(foundParams);
    }

    private static void expandMacros() throws IOException {
        try (FileWriter w = new FileWriter("expanded.asm")) {
            for (String line : intermediateCode) {
                String[] parts = line.split("\\s+", 2);
                String opcode = parts[0];
                if (mnt.containsKey(opcode)) {
                    processMacroCall(line, mnt.get(opcode), w);
                } else {
                    w.write(line + "\n");
                }
            }
        }
    }

    private static void processMacroCall(String callLine, MNTEntry entry, FileWriter w) throws IOException {
        // 1. Get arguments from the call line
        String[] parts = callLine.split("\\s+", 2);
        String[] args = (parts.length > 1) ? parts[1].split(",") : new String[0];
        
        // 2. Build the Argument-Parameter Map
        Map<String, String> argMap = new HashMap<>();
        List<String> paramNames = entry.params; 
        for (int i = 0; i < paramNames.size(); i++) {
            if (i < args.length) {
                argMap.put(paramNames.get(i), args[i].trim());
            }
        }

        // 3. Loop through the MDT and perform substitution
        for (int i = entry.mdtp; i < mdt.size(); i++) {
            String mdtLine = mdt.get(i); 
            if (mdtLine.contains("MEND")) {
                break; // Stop expansion
            }
            w.write("+" + substituteParams(mdtLine, argMap) + "\n");
        }
    }

    private static String substituteParams(String mdtLine, Map<String, String> argMap) {
        String result = mdtLine;
        for (Map.Entry<String, String> e : argMap.entrySet()) {
            result = result.replace(e.getKey(), e.getValue());
        }
        return result;
    }
}