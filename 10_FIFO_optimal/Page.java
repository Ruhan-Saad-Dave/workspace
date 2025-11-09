import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

interface PRA { // PageReplacementAlgorithm
    void run(int cap, int[] rs);
}

/**
 * NEW CLASS: Implements the FIFO algorithm
 */
class FIFO implements PRA {
    public void run(int cap, int[] rs) { //capicity, referenceString
        int pf = 0; //pageFault
        List<Integer> fr = new ArrayList<>(cap); //frame
        System.out.println("\n--- FIFO Algorithm ---");
        System.out.print("Page\tFrames\n");
        
        for (int p : rs) { //page
            System.out.print(p + "\t");
            if (fr.contains(p)) {
                // --- HIT ---
                // In FIFO, a hit does not change the order
                Page.printFrames(fr, cap);
                System.out.print("\tHit");
            } else {
                // --- FAULT ---
                pf++;
                if (fr.size() == cap) {
                    fr.remove(0); // Remove the first-in page (at the front)
                }
                fr.add(p); // Add new page to the end of the list
                Page.printFrames(fr, cap);
                System.out.print("\tFault");
            }
            System.out.println(); // Newline for next step
        }
        System.out.println("\nTotal FIFO Page Faults: " + pf);
    }
}

// The LRU class has been removed as it was not requested.

class Optimal implements PRA {
    public void run(int cap, int[] rs) {
        int pf = 0;
        List<Integer> fr = new ArrayList<>(cap);
        System.out.println("\n--- Optimal Algorithm ---");
        System.out.print("Page\tFrames\n");
        
        for (int i = 0; i < rs.length; i++) {
            int p = rs[i];
            System.out.print(p + "\t");
            
            if (fr.contains(p)) {
                // --- HIT ---
                Page.printFrames(fr, cap);
                System.out.print("\tHit");
            } else {
                // --- FAULT ---
                pf++;
                if (fr.size() < cap) {
                    fr.add(p);
                } else {
                    int pte = findVictim(fr, rs, i); // pageToEvict
                    fr.remove(Integer.valueOf(pte));
                    fr.add(p);
                }
                Page.printFrames(fr, cap);
                System.out.print("\tFault");
            }
            System.out.println(); // Newline for next step
        }
        System.out.println("\nTotal Optimal Page Faults: " + pf);
    }

    private int findVictim(List<Integer> fr, int[] rs, int idx) {
        int pte = -1, fnu = -1; // pageToEvict, farthestNextUse
        for (int pif : fr) { // pageInFrame
            int nextUse = -1;
            for (int j = idx + 1; j < rs.length; j++) {
                if (rs[j] == pif) {
                    nextUse = j;
                    break;
                }
            }
            if (nextUse == -1) return pif; // Never used again
            if (nextUse > fnu) {
                fnu = nextUse;
                pte = pif;
            }
        }
        return (pte == -1) ? fr.get(0) : pte;
    }
}

public class Page {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of frames: ");
        int cap = sc.nextInt();
        sc.nextLine();  
        System.out.print("Enter the page reference string (e.g., 7 0 1 2): ");
        
        // **FIXED**: Splits by space or comma to avoid NumberFormatException
        int[] rs = Arrays.stream(sc.nextLine().trim().split("[\\s,]+"))
                         .mapToInt(Integer::parseInt)
                         .toArray();

        // **MODIFIED**: Runs FIFO and Optimal as requested
        PRA fifo = new FIFO();
        PRA opt = new Optimal();
        fifo.run(cap, rs);
        opt.run(cap, rs);
        
        sc.close();
    }

    public static void printFrames(List<Integer> fr, int cap) {
        // Print all pages currently in frames
        for (int i = 0; i < fr.size(); i++) {
            System.out.print(fr.get(i) + "\t");
        }
        // Print placeholders for empty frames
        for (int i = 0; i < cap - fr.size(); i++) {
            System.out.print("-\t");
        }
    }
}