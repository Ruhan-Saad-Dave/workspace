import java.io.*;
import java.util.*;

class TableRow {
    String symbol;
    int address;
    int index;
    public TableRow(String symbol, int address, int index) {
        this.symbol = symbol;
        this.address = address;
        this.index = index;
    }
    public int getIndex() { return index; }
    public String getSymbol() { return symbol; }
    public int getAddress() { return address; }
}

public class Pass_2 {
    ArrayList<TableRow> symtab, littab;

    public Pass_2() {
        symtab = new ArrayList<>();
        littab = new ArrayList<>();
    }

    public static void main(String[] args) {
        Pass_2 pass2 = new Pass_2();
        try {
            pass2.generateCode("ic.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readTables() {
        BufferedReader br;
        String line;
        int symIndex = 1, litIndex = 1;
        try {
            br = new BufferedReader(new FileReader("sym.txt"));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("symbol") || line.toLowerCase().startsWith("address") || line.contains("-")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    String symbol = parts[0];
                    int address = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                    symtab.add(new TableRow(symbol, address, symIndex++));
                }
            }
            br.close();
            br = new BufferedReader(new FileReader("lit.txt"));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("literal") || line.toLowerCase().startsWith("address") || line.contains("-")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    String literal = parts[0];
                    int address = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                    littab.add(new TableRow(literal, address, litIndex++));
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Table read error: " + e.getMessage());
        }
    }

    public void generateCode(String filename) throws Exception {
        readTables();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        BufferedWriter bw = new BufferedWriter(new FileWriter("PASS2.txt"));
        String line, code;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.contains("Intermediate Code") || line.contains("-")) continue;
            String[] tokens = line.replace("(", " (").replace(")", ") ").trim().split("\\s+");
            ArrayList<String> parts = new ArrayList<>();
            for (String token : tokens) {
                token = token.trim();
                if (!token.isEmpty()) parts.add(token);
            }
            if (parts.size() == 0) continue;
            if (parts.get(0).contains("AD") || parts.get(0).contains("DL,02")) {
                bw.write("\n");
                System.out.print("\n");
                continue;
            }
            if (parts.size() == 2) {
                if (parts.get(0).contains("DL,01")) {
                    int constant = Integer.parseInt(parts.get(1).replaceAll("[^0-9]", ""));
                    code = "00\t0\t" + String.format("%03d", constant) + "\n";
                    bw.write(code);
                    System.out.print(code);
                }
            } else if (parts.size() == 3 && parts.get(0).contains("IS")) {
                int opcode = Integer.parseInt(parts.get(0).replaceAll("[^0-9]", ""));
                int regcode = Integer.parseInt(parts.get(1).replaceAll("[^0-9]", ""));
                String field = parts.get(2);
                if (field.contains("S")) {
                    int symIndex = Integer.parseInt(field.replaceAll("[^0-9]", ""));
                    if (symIndex > 0 && symIndex <= symtab.size()) {
                        code = String.format("%02d", opcode) + "\t" + regcode + "\t" + String.format("%03d", symtab.get(symIndex - 1).getAddress()) + "\n";
                        bw.write(code);
                        System.out.print(code);
                    }
                } else if (field.contains("L")) {
                    int litIndex = Integer.parseInt(field.replaceAll("[^0-9]", ""));
                    if (litIndex > 0 && litIndex <= littab.size()) {
                        code = String.format("%02d", opcode) + "\t" + regcode + "\t" + String.format("%03d", littab.get(litIndex - 1).getAddress()) + "\n";
                        bw.write(code);
                        System.out.print(code);
                    }
                }
            } else if (parts.size() == 2 && parts.get(0).contains("IS")) {
                int opcode = Integer.parseInt(parts.get(0).replaceAll("[^0-9]", ""));
                code = String.format("%02d", opcode) + "\t0\t000\n";
                bw.write(code);
                System.out.print(code);
            } else if (parts.size() == 1 && parts.get(0).contains("IS")) {
                int opcode = Integer.parseInt(parts.get(0).replaceAll("[^0-9]", ""));
                code = String.format("%02d", opcode) + "\t0\t000\n";
                bw.write(code);
                System.out.print(code);
            } else if (parts.size() == 2 && parts.get(0).contains("DL,02")) {
                int value = Integer.parseInt(parts.get(1).replaceAll("[^0-9]", ""));
                code = "00\t0\t" + String.format("%03d", value) + "\n";
                bw.write(code);
                System.out.print(code);
            }
        }
        bw.close();
        br.close();
    }
}
