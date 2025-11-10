// Q7.java
import java.util.*;

public class Q7 {
    static class Allocation {
        int processIdx, blockIdx, processSize, blockSize, unused;
        Allocation(int p, int b, int ps, int bs, int un) {
            processIdx = p; blockIdx = b; processSize = ps; blockSize = bs; unused = un;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // User input for memory blocks
        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        int[] blockSizes = new int[m];
        System.out.print("Enter sizes of " + m + " memory blocks: ");
        for (int i = 0; i < m; i++)
            blockSizes[i] = sc.nextInt();

        // User input for process sizes
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        int[] processSizes = new int[n];
        System.out.print("Enter sizes of " + n + " processes: ");
        for (int i = 0; i < n; i++)
            processSizes[i] = sc.nextInt();

        System.out.println("\nBest Fit Allocation:");
        simulate("Best Fit", blockSizes, processSizes, n, m);

        System.out.println("\nFirst Fit Allocation:");
        simulate("First Fit", blockSizes, processSizes, n, m);

        System.out.println("\nNext Fit Allocation:");
        simulate("Next Fit", blockSizes, processSizes, n, m);
    }

    static void simulate(String strategy, int[] blockSizes, int[] processSizes, int n, int m) {
        int[] blocks = Arrays.copyOf(blockSizes, m);
        int pointer = 0; // for next fit
        List<Allocation> result = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            int chosen = -1;
            if (strategy.equals("Best Fit")) {
                int minDiff = Integer.MAX_VALUE;
                for (int j = 0; j < m; j++) {
                    if (blocks[j] >= processSizes[i] && blocks[j] - processSizes[i] < minDiff) {
                        minDiff = blocks[j] - processSizes[i];
                        chosen = j;
                    }
                }
            } else if (strategy.equals("First Fit")) {
                for (int j = 0; j < m; j++) {
                    if (blocks[j] >= processSizes[i]) {
                        chosen = j;
                        break;
                    }
                }
            } else if (strategy.equals("Next Fit")) {
                int start = pointer;
                do {
                    if (blocks[pointer] >= processSizes[i]) {
                        chosen = pointer;
                        break;
                    }
                    pointer = (pointer + 1) % m;
                } while (pointer != start);
            }
            if (chosen != -1) {
                result.add(new Allocation(i, chosen, processSizes[i], blockSizes[chosen], blocks[chosen] - processSizes[i]));
                blocks[chosen] -= processSizes[i];
                if (strategy.equals("Next Fit"))
                    pointer = (chosen + 1) % m;
            } else {
                result.add(new Allocation(i, -1, processSizes[i], 0, 0));
            }
        }

        // Print output in table format
        System.out.printf("%-8s %-15s %-9s %-13s %-16s\n", "Process", "Process Size(KB)", "Block No", "Block Size(KB)", "Unused Space(KB)");
        for (Allocation alloc : result) {
            String blockNo = alloc.blockIdx == -1 ? "Not Alloc" : String.valueOf(alloc.blockIdx + 1);
            String blockSize = alloc.blockIdx == -1 ? "-" : String.valueOf(alloc.blockSize);
            String unused = alloc.blockIdx == -1 ? "-" : String.valueOf(alloc.unused);
            System.out.printf("P%-7d %-15d %-9s %-13s %-16s\n", alloc.processIdx + 1, alloc.processSize, blockNo, blockSize, unused);
        }
    }
}