import java.util.Arrays;
import java.util.Scanner;

public class Fit {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of memory blocks: ");
        int nb = sc.nextInt();
        int[] blocks = new int[nb];
        for (int i = 0; i < nb; i++) {
            System.out.print("Enter size for block " + (i + 1) + ": ");
            blocks[i] = sc.nextInt();
        }
        
        System.out.print("\nEnter number of processes: ");
        int np = sc.nextInt();
        int[] proc = new int[np];
        for (int i = 0; i < np; i++) {
            System.out.print("Enter size for process " + (i + 1) + ": ");
            proc[i] = sc.nextInt();
        }

        // We pass 'blocks' (the original) so printResults can show original block sizes
        firstFit(Arrays.copyOf(blocks, nb), proc, blocks);
        nextFit(Arrays.copyOf(blocks, nb), proc, blocks);
        // Removed bestFit as per PS
        worstFit(Arrays.copyOf(blocks, nb), proc, blocks);
        
        sc.close();
    }

    /**
     * Allocates memory using First Fit strategy.
     * @param b The working copy of block sizes (this array will be modified).
     * @param p The array of process sizes.
     * @param originalBlocks The unchanged, original block sizes for reporting.
     */
    public static void firstFit(int[] b, int[] p, int[] originalBlocks) {
        int[] alloc = new int[p.length];    
        Arrays.fill(alloc, -1);
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < b.length; j++) {
                if (b[j] >= p[i]) {
                    alloc[i] = j;    
                    b[j] -= p[i];    
                    break;
                }
            }
        }
        printResults("First Fit", p, alloc, originalBlocks);
    }

    /**
     * Allocates memory using Next Fit strategy.
     * @param b The working copy of block sizes.
     * @param p The array of process sizes.
     * @param originalBlocks The unchanged, original block sizes for reporting.
     */
    public static void nextFit(int[] b, int[] p, int[] originalBlocks) {
        int[] alloc = new int[p.length];    
        Arrays.fill(alloc, -1);
        int lastIdx = 0;
        for (int i = 0; i < p.length; i++) {
            for (int k = 0; k < b.length; k++) {
                int j = (lastIdx + k) % b.length;
                if (b[j] >= p[i]) {
                    alloc[i] = j;    
                    b[j] -= p[i];    
                    lastIdx = (j + 1) % b.length; // Start next search from the *next* block
                    break;
                }
            }
        }
        printResults("Next Fit", p, alloc, originalBlocks);
    }

    // bestFit method removed as it's not in the Chit 8 Problem Statement

    /**
     * Allocates memory using Worst Fit strategy.
     * @param b The working copy of block sizes.
     * @param p The array of process sizes.
     * @param originalBlocks The unchanged, original block sizes for reporting.
     */
    public static void worstFit(int[] b, int[] p, int[] originalBlocks) {
        int[] alloc = new int[p.length];    
        Arrays.fill(alloc, -1);
        for (int i = 0; i < p.length; i++) {
            int worstIdx = -1;    
            int maxFrag = -1;
            for (int j = 0; j < b.length; j++) {
                if (b[j] >= p[i]) {
                    int frag = b[j] - p[i];
                    if (frag > maxFrag) {    
                        maxFrag = frag;    
                        worstIdx = j;    
                    }
                }
            }
            if (worstIdx != -1) {    
                alloc[i] = worstIdx;    
                b[worstIdx] -= p[i];    
            } // Typo "~}" was here and has been fixed
        }
        printResults("Worst Fit", p, alloc, originalBlocks);
    }

    /**
     * Prints the allocation results in the format specified by the PS.
     * @param strat The name of the allocation strategy.
     * @param p The array of process sizes.
     * @param alloc The array mapping process index to allocated block index.
     * @param originalBlocks The array of the original, unmodified block sizes.
     */
    public static void printResults(String strat, int[] p, int[] alloc, int[] originalBlocks) {
        System.out.println("\n\n--- " + strat + " Allocation ---");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println(" Process\tProcess Size(KB)\tBlock No\tBlock Size(KB)\tUnused Space(KB)");
        System.out.println("-----------------------------------------------------------------------------------------");
        
        for (int i = 0; i < p.length; i++) {
            System.out.print(" P" + (i + 1) + "\t\t" + p[i] + "\t\t\t");
            if (alloc[i] != -1) {
                int blockNum = alloc[i] + 1;
                // Get original block size from the 'originalBlocks' array
                int blockSize = originalBlocks[alloc[i]]; 
                // Calculate the immediate unused space from this one allocation
                int unused = blockSize - p[i]; 
                System.out.println(blockNum + "\t\t" + blockSize + "\t\t" + unused);
            } else {
                System.out.println("Not Allocated\t-\t\t-");
            }
        }
        System.out.println("-----------------------------------------------------------------------------------------");
    }
}