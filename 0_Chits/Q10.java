// Q10.java
import java.util.*;

public class Q10 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n=== Page Replacement Simulator ===");
        System.out.print("Enter number of frames: ");
        int frames = sc.nextInt();
        System.out.print("Enter length of reference string: ");
        int n = sc.nextInt();
        int[] ref = new int[n];
        System.out.print("Enter reference string (space separated): ");
        for (int i = 0; i < n; i++)
            ref[i] = sc.nextInt();

        System.out.println("\n--- FIFO Page Replacement (step-by-step) ---");
        pageReplacementFIFO(ref, n, frames);

        System.out.println("\n--- Optimal Page Replacement (step-by-step) ---");
        pageReplacementOptimal(ref, n, frames);

        sc.close();
    }

    static void pageReplacementFIFO(int[] ref, int n, int frames) {
        // Fixed frame slots: when a page is replaced, the new page occupies the same slot index
        int[] slots = new int[frames];
        Arrays.fill(slots, -1);
        // FIFO queue will store frame indices (not page values)
        Queue<Integer> fifoIndexQueue = new LinkedList<>();
        int faults = 0;

        System.out.printf("%5s | %-15s | %-8s | %-12s\n", "Step", "Reference", "Frames", "Result");
        System.out.println("----------------------------------------------------------------");

        for (int i = 0; i < n; i++) {
            int page = ref[i];
            String result;

            // check if page already in a slot
            int foundIdx = -1;
            for (int s = 0; s < frames; s++) if (slots[s] == page) { foundIdx = s; break; }

            if (foundIdx != -1) {
                result = "Hit (in slot " + foundIdx + ")";
            } else {
                faults++;
                // find first free slot if any
                int freeIdx = -1;
                for (int s = 0; s < frames; s++) if (slots[s] == -1) { freeIdx = s; break; }

                if (freeIdx != -1) {
                    // place page into free slot and record its frame index in FIFO order
                    slots[freeIdx] = page;
                    fifoIndexQueue.add(freeIdx);
                    result = "Fault (loaded into free slot " + freeIdx + ")";
                } else {
                    // replace the oldest frame index
                    int idxToReplace = fifoIndexQueue.poll();
                    int removed = slots[idxToReplace];
                    slots[idxToReplace] = page;
                    fifoIndexQueue.add(idxToReplace);
                    result = "Fault (replaced page " + removed + " at slot " + idxToReplace + ")";
                }
            }

            System.out.printf("%5d | %-15d | %-8s | %-12s\n", i + 1, page, framesToStringFromArray(slots), result);
        }

        System.out.println("----------------------------------------------------------------");
        System.out.println("Total Page Faults: " + faults + " out of " + n + " references");
        System.out.printf("Hit Rate: %.2f%%, Fault Rate: %.2f%%\n", (n - faults) * 100.0 / n, faults * 100.0 / n);
    }

    static void pageReplacementOptimal(int[] ref, int n, int frames) {
        int[] slots = new int[frames];
        Arrays.fill(slots, -1);
        int faults = 0;

        System.out.printf("%5s | %-15s | %-8s | %-20s\n", "Step", "Reference", "Frames", "Result (explain)");
        System.out.println("-------------------------------------------------------------------------------");

        for (int i = 0; i < n; i++) {
            int page = ref[i];
            String result;

            // check hit
            int foundIdx = -1;
            for (int s = 0; s < frames; s++) if (slots[s] == page) { foundIdx = s; break; }

            if (foundIdx != -1) {
                result = "Hit (in slot " + foundIdx + ")";
            } else {
                faults++;
                // free slot?
                int freeIdx = -1;
                for (int s = 0; s < frames; s++) if (slots[s] == -1) { freeIdx = s; break; }

                if (freeIdx != -1) {
                    slots[freeIdx] = page;
                    result = "Fault (loaded into free slot " + freeIdx + ")";
                } else {
                    int idxToReplace = -1;
                    int farthestUse = -1;
                    for (int j = 0; j < frames; j++) {
                        int fpage = slots[j];
                        int nextUse = Integer.MAX_VALUE;
                        for (int k = i + 1; k < n; k++) {
                            if (ref[k] == fpage) { nextUse = k; break; }
                        }
                        if (nextUse > farthestUse) { farthestUse = nextUse; idxToReplace = j; }
                    }
                    int removed = slots[idxToReplace];
                    slots[idxToReplace] = page;
                    result = "Fault (replaced " + removed + ", next use at " + (farthestUse == Integer.MAX_VALUE ? "never" : farthestUse) + ")";
                }
            }

            System.out.printf("%5d | %-15d | %-8s | %-20s\n", i + 1, page, framesToStringFromArray(slots), result);
        }

        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("Total Page Faults: " + faults + " out of " + n + " references");
        System.out.printf("Hit Rate: %.2f%%, Fault Rate: %.2f%%\n", (n - faults) * 100.0 / n, faults * 100.0 / n);
    }

    static String framesToString(List<Integer> snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < snapshot.size(); i++) {
            if (i > 0) sb.append(",");
            if (snapshot.get(i) == -1) sb.append("-");
            else sb.append(snapshot.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    static String framesToStringFromArray(int[] slots) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < slots.length; i++) {
            if (i > 0) sb.append(",");
            if (slots[i] == -1) sb.append("-");
            else sb.append(slots[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}