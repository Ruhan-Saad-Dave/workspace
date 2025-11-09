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

        // Pass the original 'blocks' array to each function
        firstFit(Arrays.copyOf(blocks, nb), proc, blocks);
        nextFit(Arrays.copyOf(blocks, nb), proc, blocks);
        bestFit(Arrays.copyOf(blocks, nb), proc, blocks);
        
        // Removed the call to worstFit as it's not in the PS
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
            // Start searching from the last allocation point
            for (int k = 0; k < b.length; k++) {
                int j = (lastIdx + k) % b.length;
                if (b[j] >= p[i]) {
                    alloc[i] = j;    
                    b[j] -= p[i];    
                    lastIdx = j; // Update the last allocation index
                    break;
                }
            }
        }
        printResults("Next Fit", p, alloc, originalBlocks);
    }

    /**
     * Allocates memory using Best Fit strategy.
     * @param b The working copy of block sizes.
     * @param p The array of process sizes.
     * @param originalBlocks The unchanged, original block sizes for reporting.
     */
    public static void bestFit(int[] b, int[] p, int[] originalBlocks) {
        int[] alloc = new int[p.length];    
        Arrays.fill(alloc, -1);
        for (int i = 0; i < p.length; i++) {
            int bestIdx = -1;    
            int minFrag = Integer.MAX_VALUE;
            for (int j = 0; j < b.length; j++) {
                if (b[j] >= p[i]) {
                    int frag = b[j] - p[i];
                    if (frag < minFrag) {    
                        minFrag = frag;    
                        bestIdx = j;    
                    }
                }
            }
            if (bestIdx != -1) {    
                alloc[i] = bestIdx;    
                b[bestIdx] -= p[i];    
            }
        }
        printResults("Best Fit", p, alloc, originalBlocks);
    }

    // The worstFit method has been removed as it was not in the PS.

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
                int blockSize = originalBlocks[alloc[i]];
                int unused = blockSize - p[i];
                System.out.println(blockNum + "\t\t" + blockSize + "\t\t" + unused);
            } else {
                System.out.println("Not Allocated\t-\t\t-");
            }
        }
        System.out.println("-----------------------------------------------------------------------------------------");
    }
}