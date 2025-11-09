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

class LRU implements PRA {
    public void run(int cap, int[] rs) { //capicity, referenceString(input numbers)
        int pf = 0; //pageFault
        List<Integer> fr = new ArrayList<>(cap); //frame
        System.out.println("\n--- LRU Algorithm ---");
        System.out.print("Page\tFrames\n");
        
        for (int p : rs) { //page
            System.out.print(p + "\t");
            if (fr.contains(p)) {
                // --- HIT ---
                fr.remove(Integer.valueOf(p)); // Remove from its current position
                fr.add(p); // Add to end (making it Most Recently Used)
                Page.printFrames(fr, cap);
                System.out.print("\tHit");
            } else {
                // --- FAULT ---
                pf++;
                if (fr.size() == cap) {
                    fr.remove(0); // Remove LRU (at the front of the list)
                }
                fr.add(p); // Add new page (becomes MRU)
                Page.printFrames(fr, cap);
                System.out.print("\tFault");
            }
            System.out.println(); // Newline for next step
        }
        System.out.println("\nTotal LRU Page Faults: " + pf);
    }
}

// Optimal class has been removed as it was not in the Problem Statement

public class Page {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of frames: ");
        int cap = sc.nextInt();
        sc.nextLine();  
        System.out.print("Enter the page reference string (e.g., 7 0 1 2): ");
        int[] rs = Arrays.stream(sc.nextLine().split("\\s+"))
                         .mapToInt(Integer::parseInt)
                         .toArray();

        // *** MODIFIED ***
        // Create and run FIFO and LRU as requested
        PRA fifo = new FIFO();
        PRA lru = new LRU();
        
        fifo.run(cap, rs);
        lru.run(cap, rs);
        
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