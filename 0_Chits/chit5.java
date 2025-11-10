import java.util.*;

public class chit5 {

    static class Process {
        int pid;
        int arrival;
        int burst;
        int priority;

        // computed
        int start;
        int completion;
        int turnaround;
        int waiting;

        Process(int pid, int arrival, int burst, int priority) {
            this.pid = pid;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
        }

        Process(Process other) {
            this.pid = other.pid;
            this.arrival = other.arrival;
            this.burst = other.burst;
            this.priority = other.priority;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n=== CPU Scheduling Simulation - FCFS and Priority (Non-Preemptive) ===\n");
        System.out.print("Enter number of processes: ");
        int n = Integer.parseInt(sc.nextLine().trim());

        ArrayList<Process> processes = new ArrayList<>();
        System.out.println("\nEnter process details (arrival_time burst_time priority):");
        System.out.println("Note: Lower priority number means higher priority (e.g., 1 > 2)\n");
        
        for (int i = 0; i < n; i++) {
            System.out.printf("Process P%d: ", i + 1);
            String[] parts = sc.nextLine().trim().split("\\s+");
            if (parts.length < 3) {
                System.out.println("Invalid input. Please enter three integers.");
                i--; continue;
            }
            int at = Integer.parseInt(parts[0]);
            int bt = Integer.parseInt(parts[1]);
            int pr = Integer.parseInt(parts[2]);
            processes.add(new Process(i + 1, at, bt, pr));
        }

        System.out.println("\nChoose scheduling algorithm:");
        System.out.println("1) First Come First Serve (FCFS)");
        System.out.println("2) Priority (Non-Preemptive)");
        System.out.println("3) Both algorithms");
        System.out.print("\nYour choice (1-3): ");
        int choice = Integer.parseInt(sc.nextLine().trim());

        if (choice == 1 || choice == 3) {
            System.out.println("\n====== FCFS (First Come First Serve) Scheduling ======");
            System.out.println("Strategy: Execute processes in order of arrival time");
            simulateFCFS(cloneProcessList(processes));
        }

        if (choice == 2 || choice == 3) {
            System.out.println("\n====== Priority (Non-Preemptive) Scheduling ======");
            System.out.println("Strategy: Execute highest priority process (lowest number) that has arrived");
            System.out.println("Note: Once started, a process runs to completion (non-preemptive)");
            simulatePriority(cloneProcessList(processes));
        }

        sc.close();
    }

    static ArrayList<Process> cloneProcessList(ArrayList<Process> src) {
        ArrayList<Process> copy = new ArrayList<>();
        for (Process p : src) copy.add(new Process(p));
        return copy;
    }

    static void printGanttChart(ArrayList<Process> plist, String title) {
        System.out.println("\nGantt Chart:");
        System.out.print("Time: ");
        int lastEnd = 0;
        
        // Print timeline
        for (Process p : plist) {
            if (p.start > lastEnd) {
                System.out.printf("%-" + ((p.start - lastEnd) * 3) + "s", "idle");
            }
            System.out.printf("%" + (p.burst * 3) + "d", p.start);
            lastEnd = p.completion;
        }
        System.out.println(lastEnd);

        // Print process bars
        System.out.print("     ");
        lastEnd = 0;
        for (Process p : plist) {
            if (p.start > lastEnd) {
                System.out.printf("%-" + (p.start - lastEnd) * 3 + "s", "---");
            }
            String bar = String.format("P%d-", p.pid);
            System.out.print(bar.repeat(p.burst));
            lastEnd = p.completion;
        }
        System.out.println();
    }

    static void simulateFCFS(ArrayList<Process> plist) {
        // sort by arrival then pid
        Collections.sort(plist, Comparator.comparingInt((Process p) -> p.arrival)
                                       .thenComparingInt(p -> p.pid));

        System.out.println("\nProcess Execution Order:");
        int time = 0;
        double totalWT = 0, totalTAT = 0;

        for (Process p : plist) {
            if (p.arrival > time) {
                System.out.printf("Time %d-%-2d: CPU idle (waiting for next process)\n", 
                    time, p.arrival);
                time = p.arrival;
            }
            p.start = time;
            p.completion = p.start + p.burst;
            p.turnaround = p.completion - p.arrival;
            p.waiting = p.start - p.arrival;

            System.out.printf("Time %d-%-2d: Executing P%d (Arrival=%d, Burst=%d)\n",
                time, p.completion, p.pid, p.arrival, p.burst);
            
            time = p.completion;
            totalWT += p.waiting;
            totalTAT += p.turnaround;
        }

        // Print summary table
        System.out.println("\nProcess Summary:");
        System.out.printf("%-6s %-7s %-7s %-9s %-7s %-11s %-12s %-7s\n", 
            "PID", "Arrival", "Burst", "Priority", "Start", "Completion", "Turnaround", "Waiting");
        System.out.println("-".repeat(70));

        for (Process p : plist) {
            System.out.printf("P%-5d %-7d %-7d %-9d %-7d %-11d %-12d %-7d\n",
                p.pid, p.arrival, p.burst, p.priority, 
                p.start, p.completion, p.turnaround, p.waiting);
        }

        int n = plist.size();
        System.out.println("\nPerformance Metrics:");
        System.out.printf("Average Turnaround Time = %.2f units\n", totalTAT / n);
        System.out.printf("Average Waiting Time    = %.2f units\n", totalWT / n);

        printGanttChart(plist, "FCFS");
    }

    static void simulatePriority(ArrayList<Process> plist) {
        int n = plist.size();
        boolean[] done = new boolean[n];
        int completed = 0;
        int time = 0;
        double totalWT = 0, totalTAT = 0;
        ArrayList<Process> executionOrder = new ArrayList<>();

        System.out.println("\nProcess Execution Order:");
        while (completed < n) {
            int idx = -1;
            int bestPriority = Integer.MAX_VALUE;
            
            // Find highest priority ready process
            for (int i = 0; i < n; i++) {
                Process p = plist.get(i);
                if (!done[i] && p.arrival <= time) {
                    if (p.priority < bestPriority || 
                       (p.priority == bestPriority && p.arrival < plist.get(idx).arrival)) {
                        bestPriority = p.priority;
                        idx = i;
                    }
                }
            }

            if (idx == -1) {
                // Find next arrival
                int nextArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!done[i]) nextArrival = Math.min(nextArrival, plist.get(i).arrival);
                }
                System.out.printf("Time %d-%-2d: CPU idle (waiting for next process)\n",
                    time, nextArrival);
                time = nextArrival;
                continue;
            }

            Process p = plist.get(idx);
            p.start = time;
            p.completion = p.start + p.burst;
            p.turnaround = p.completion - p.arrival;
            p.waiting = p.start - p.arrival;

            System.out.printf("Time %d-%-2d: Executing P%d (Priority=%d, Arrival=%d, Burst=%d)\n",
                time, p.completion, p.pid, p.priority, p.arrival, p.burst);

            time = p.completion;
            done[idx] = true;
            completed++;
            executionOrder.add(p);

            totalWT += p.waiting;
            totalTAT += p.turnaround;
        }

        // Print summary table
        System.out.println("\nProcess Summary:");
        System.out.printf("%-6s %-7s %-7s %-9s %-7s %-11s %-12s %-7s\n",
            "PID", "Arrival", "Burst", "Priority", "Start", "Completion", "Turnaround", "Waiting");
        System.out.println("-".repeat(70));

        Collections.sort(plist, Comparator.comparingInt(p -> p.pid));
        for (Process p : plist) {
            System.out.printf("P%-5d %-7d %-7d %-9d %-7d %-11d %-12d %-7d\n",
                p.pid, p.arrival, p.burst, p.priority,
                p.start, p.completion, p.turnaround, p.waiting);
        }

        System.out.println("\nPerformance Metrics:");
        System.out.printf("Average Turnaround Time = %.2f units\n", totalTAT / n);
        System.out.printf("Average Waiting Time    = %.2f units\n", totalWT / n);

        printGanttChart(executionOrder, "Priority");
    }
}


