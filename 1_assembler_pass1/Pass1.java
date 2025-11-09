import java.io.*;
import java.util.*;

public class Pass1 {
    private static int lc = 0;
    private static Map<String, Integer> symtab = new LinkedHashMap<>();
    private static Map<String, Integer> littab = new LinkedHashMap<>();
    private static List<Integer> pooltab = new ArrayList<>(Arrays.asList(0));
    private static Map<String, OpcodeEntry> mot = new HashMap<>(); //Machine Opcode Table

    static class OpcodeEntry {
        String type, code;
        int length;
        OpcodeEntry(String type, String code, int length) {
            this.type = type;
            this.code = code;
            this.length = length;
        }
    }

    static class AssemblyLine {
        String label, opcode, operand1, operand2;
        AssemblyLine(String label, String opcode, String operand1, String operand2) {
            this.label = label;
            this.opcode = opcode;
            this.operand1 = operand1;
            this.operand2 = operand2;
        }
    }

    static {
        mot.put("STOP", new OpcodeEntry("IS", "00", 1)); //Imperative statement
        mot.put("ADD", new OpcodeEntry("IS", "01", 1));
        mot.put("SUB", new OpcodeEntry("IS", "02", 1));
        mot.put("MULT", new OpcodeEntry("IS", "03", 1));
        mot.put("MOVER", new OpcodeEntry("IS", "04", 1));
        mot.put("MOVEM", new OpcodeEntry("IS", "05", 1));
        mot.put("COMP", new OpcodeEntry("IS", "06", 1));
        mot.put("BC", new OpcodeEntry("IS", "07", 1));
        mot.put("DIV", new OpcodeEntry("IS", "08", 1));
        mot.put("READ", new OpcodeEntry("IS", "09", 1));
        mot.put("PRINT", new OpcodeEntry("IS", "10", 1));
        mot.put("START", new OpcodeEntry("AD", "00", 0)); //Assembler Directive
        mot.put("END", new OpcodeEntry("AD", "01", 0));
        mot.put("ORIGIN", new OpcodeEntry("AD", "02", 0));
        mot.put("EQU", new OpcodeEntry("AD", "03", 0));
        mot.put("LTORG", new OpcodeEntry("AD", "04", 0));
        mot.put("DS", new OpcodeEntry("DL", "01", 0)); // Declaration language
        mot.put("DC", new OpcodeEntry("DL", "02", 0));
    }

    public static void main(String[] args) throws IOException {
        assemble("input.txt");
        System.out.println("Pass 1 complete. Check generated files (intermediate_code.txt, symtab.txt, littab.txt, pooltab.txt).");
    }

    private static int getSymbolIndex(String s) {
        if (!symtab.containsKey(s))
            symtab.put(s, -1); 
        return new ArrayList<>(symtab.keySet()).indexOf(s);
    }

    private static int getLiteralIndex(String l) {
        if (!littab.containsKey(l))
            littab.put(l, -1); 
        return new ArrayList<>(littab.keySet()).indexOf(l);
    }

    private static AssemblyLine parseLine(String line) {
        line = line.trim();
        if (line.isEmpty())
            return null;

        String[] tokens = line.split("\\s+"); 
        String label = null, opcode = null, op1 = null, op2 = null, opsStr = null;
        int opIdx = 0;

        if (mot.containsKey(tokens[0].toUpperCase())) {
            opcode = tokens[0];
            opIdx = 0;
        } else {
            label = tokens[0];
            if (tokens.length > 1) {
                opcode = tokens[1];
                opIdx = 1;
            } else {
                return new AssemblyLine(label, null, null, null);
            }
        }

        if (tokens.length > opIdx + 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = opIdx + 1; i < tokens.length; i++) {
                sb.append(tokens[i]);
            }
            opsStr = sb.toString();
        }

        if (opsStr != null) {
            String[] operands = opsStr.split(",", 2); // split into max 2 parts
            op1 = operands[0].trim();
            if (operands.length > 1)
                op2 = operands[1].trim();
        }
        return new AssemblyLine(label, opcode, op1, op2);
    }

    private static void processLiteralPool(FileWriter iw) throws IOException {
        int poolStart = pooltab.get(pooltab.size() - 1);
        List<String> litNames = new ArrayList<>(littab.keySet());
        OpcodeEntry dcOp = mot.get("DC"); 

        for (int i = poolStart; i < litNames.size(); i++) {
            String litName = litNames.get(i);
            littab.put(litName, lc); 
            String litValue = litName.replaceAll("[='\\s]", "");
            iw.write(String.format("\t(%s, %s) (C, %s)\n", dcOp.type, dcOp.code, litValue));
            lc++; 
        }
        pooltab.add(littab.size()); 
    }

    public static void assemble(String inFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inFile));
             FileWriter interwriter = new FileWriter("intermediate_code.txt");
             FileWriter symwriter = new FileWriter("symtab.txt");
             FileWriter litwriter = new FileWriter("littab.txt");
             FileWriter poolwriter = new FileWriter("pooltab.txt")) {

            String line;
            while ((line = reader.readLine()) != null) {
                AssemblyLine assemblyline = parseLine(line);
                if (assemblyline == null)
                    continue;

                // --- Handle Label ---
                if (assemblyline.label != null) {
                    if (symtab.containsKey(assemblyline.label) && symtab.get(assemblyline.label) != -1)
                        System.err.println("Duplicate symbol definition: " + assemblyline.label);
                    symtab.put(assemblyline.label, lc);
                }
                if (assemblyline.opcode == null)
                    continue; // Skip lines with only labels

                OpcodeEntry op = mot.get(assemblyline.opcode.toUpperCase());
                if (op == null) {
                    System.err.println("Bad opcode: " + assemblyline.opcode);
                    continue;
                }

                if (assemblyline.opcode.equalsIgnoreCase("START")) {
                    lc = Integer.parseInt(assemblyline.operand1);
                    interwriter.write(String.format("(%s, %s) (C, %s)\n", op.type, op.code, assemblyline.operand1));
                } else if (assemblyline.opcode.equalsIgnoreCase("LTORG")) {
                    interwriter.write(String.format("(%s, %s)\n", op.type, op.code));
                    processLiteralPool(interwriter);
                } else if (assemblyline.opcode.equalsIgnoreCase("END")) {
                    interwriter.write(String.format("(%s, %s)\n", op.type, op.code));
                    processLiteralPool(interwriter);
                    break; 
                } else if (assemblyline.opcode.equalsIgnoreCase("DS")) {
                    int size = Integer.parseInt(assemblyline.operand1);
                    interwriter.write(String.format("(%s, %s) (C, %d)\n", op.type, op.code, size));
                    lc += size; // Allocate storage
                } else if (assemblyline.opcode.equalsIgnoreCase("DC")) {
                    String constValue = assemblyline.operand1.replaceAll("[='\\s]", "");
                    interwriter.write(String.format("(%s, %s) (C, %s)\n", op.type, op.code, constValue));
                    lc += 1; 
                
                } else if (op.type.equals("IS")) {
                    String rg = "", opd = "";
                    
                    if (assemblyline.opcode.equals("MOVER") || assemblyline.opcode.equals("MOVEM") || assemblyline.opcode.equals("ADD") || assemblyline.opcode.equals("SUB") || assemblyline.opcode.equals("DIV")) {
                        if (assemblyline.operand1 != null) {
                            if (assemblyline.operand1.equalsIgnoreCase("AREG")) rg = "(0)";
                            else if (assemblyline.operand1.equalsIgnoreCase("BREG")) rg = "(1)";
                            else if (assemblyline.operand1.equalsIgnoreCase("CREG")) rg = "(2)";
                            else if (assemblyline.operand1.equalsIgnoreCase("DREG")) rg = "(3)";
                            else rg = "(?)"; // Handle unknown register
                        }

                        if (assemblyline.operand2 != null)
                            opd = assemblyline.operand2.startsWith("=") ? String.format("(L, %02d)", getLiteralIndex(assemblyline.operand2)) : String.format("(S, %02d)", getSymbolIndex(assemblyline.operand2));
                    
                    } else if (assemblyline.opcode.equals("READ") || assemblyline.opcode.equals("PRINT")) {
                        // These opcodes have no register operand
                        if (assemblyline.operand1 != null)
                            opd = String.format("(S, %02d)", getSymbolIndex(assemblyline.operand1));
                    }
                    
                    interwriter.write(String.format("(%s, %s) %s %s\n", op.type, op.code, rg, opd).trim().replaceAll(" +", " ") + "\n");
                    lc += op.length;
                }
            }

            symwriter.write("Symb\tAddr\n");
            symwriter.write("----\t----\n");
            for (Map.Entry<String, Integer> e : symtab.entrySet()) {
                if (e.getValue() != -1) // Write only defined symbols
                    symwriter.write(String.format("%s\t%d\n", e.getKey(), e.getValue()));
            }

            litwriter.write("Lit#\tLit\tAddr\n");
            litwriter.write("----\t----\t----\n");
            List<String> litNames = new ArrayList<>(littab.keySet());
            for (int i = 0; i < litNames.size(); i++)
                litwriter.write(String.format("%02d\t%s\t%d\n", i, litNames.get(i), littab.get(litNames.get(i))));

            poolwriter.write("Pool#\tPool Base\n");
            poolwriter.write("-----\t----------\n");
            for (int i = 0; i < pooltab.size() - 1; i++)
                poolwriter.write(String.format("%02d\t%d\n", i, pooltab.get(i)));
        }
    }
}