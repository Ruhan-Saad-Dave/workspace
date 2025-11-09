import java.io.*;
import java.util.*;

public class Pass1 {
    private static List<MNTEntry> mnt = new ArrayList<>();
    private static List<String> mdt = new ArrayList<>();
    private static List<String> pntab = new ArrayList<>();
    private static List<KPDEntry> kpdtab = new ArrayList<>();
    private static int mdtc = 0, pntabc = 0, kpdtabc = 0;

    static class MNTEntry {
        String n; int pp, kp, mdtp, kpdtp;
        MNTEntry(String n, int pp, int kp, int m, int k) { this.n = n; this.pp = pp; this.kp = kp; this.mdtp = m; this.kpdtp = k; }
        public String toString() { return String.format("%d\t%s\t%d\t%d\t%d\t%d", mnt.indexOf(this), n, pp, kp, mdtp, kpdtp); }
    }

    static class KPDEntry {
        String n, v;
        KPDEntry(String n, String v) { this.n = n; this.v = v; }
        public String toString() { return String.format("%d\t%s\t%s", kpdtab.indexOf(this), n, v); }
    }

    public static void main(String[] args) throws IOException {
        processMacroDefinitions("input.txt");
        writeTablesToFile();
        System.out.println("Macro Pass 1 complete. Check generated files.");
    }

    private static void processMacroDefinitions(String inFile) throws IOException {
        try (BufferedReader r = new BufferedReader(new FileReader(inFile));
             FileWriter iw = new FileWriter("intermediate.asm")) {
            String line;
            boolean inMacro = false;
            Map<String, Integer> paramMap = null;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.contains("MACRO")) {
                    inMacro = true;
                    line = r.readLine().trim(); 
                    paramMap = processPrototype(line);
                } else if (line.contains("MEND")) {
                    inMacro = false;
                    mdt.add(mdtc++ + "\t" + "MEND");
                    paramMap = null;
                } else if (inMacro) {
                    mdt.add(mdtc++ + "\t" + substituteParams(line, paramMap));
                } else {
                    iw.write(line + "\n");
                }
            }
        }
    }

    private static Map<String, Integer> processPrototype(String line) {
        String[] parts = line.split("\\s+", 2);
        String name = parts[0];
        String[] params = parts[1].split(",");
        int pp = 0, kp = 0;
        int kpdtp = kpdtabc;
        Map<String, Integer> pMap = new HashMap<>();

        for (String p : params) {
            p = p.trim().substring(1);
            if (p.contains("=")) {
                kp++;
                String[] kwp = p.split("=");
                String pName = kwp[0];
                String defVal = (kwp.length > 1) ? kwp[1] : "-";
                pntab.add(pntabc + "\t" + pName);
                kpdtab.add(new KPDEntry(pName, defVal)); kpdtabc++;
                pMap.put(pName, pntabc++);
            } else {
                pp++;
                pntab.add(pntabc + "\t" + p);
                pMap.put(p, pntabc++);
            }
        }
        mnt.add(new MNTEntry(name, pp, kp, mdtc, (kp > 0) ? kpdtp : -1));
        return pMap;
    }

    private static String substituteParams(String line, Map<String, Integer> pMap) {
        String[] parts = line.split("\\s+", 2);
        String opcode = parts[0];
        String operands = parts.length > 1 ? parts[1] : "";
        String[] ops = operands.split(",");
        List<String> newOps = new ArrayList<>();

        for (String op : ops) {
            op = op.trim();
            if (op.startsWith("&")) {
                newOps.add("#" + pMap.get(op.substring(1)));
            } else {
                newOps.add(op);
            }
        }
        return opcode + " " + String.join(",", newOps);
    }

    private static void writeTablesToFile() throws IOException {
        try (FileWriter w = new FileWriter("MNT.txt")) {
            w.write("#\tMName\t#PP\t#KP\t#MDTP\t#KPDTP\n--------------------------------------------------\n");
            for (MNTEntry e : mnt) w.write(e.toString() + "\n");
        }
        try (FileWriter w = new FileWriter("MDT.txt")) {
            w.write("Opcode\tRest\n---------------------------------\n");
            for (String l : mdt) w.write(l + "\n");
        }
        try (FileWriter w = new FileWriter("PNTAB.txt")) {
            w.write("#\tPName\n------------------\n");
            for (String l : pntab) w.write(l + "\n");
        }
        try (FileWriter w = new FileWriter("KPDTAB.txt")) {
            w.write("#\tPName\tDefault\n------------------------\n");
            for (KPDEntry e : kpdtab) w.write(e.toString() + "\n");
        }
    }
}