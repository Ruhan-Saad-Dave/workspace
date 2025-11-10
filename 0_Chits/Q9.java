import java.util.*;

/**
 * CPU Scheduling Simulation: SJF (Preemptive) and Round Robin
 * 
 * This program simulates two CPU scheduling algorithms:
 * 1. Shortest Job First (Preemptive) - also known as SRTF
 * 2. Round Robin with user-defined time quantum
 * 
 * Features:
 * - Detailed process execution timeline
 * - Visual Gantt chart with process execution blocks
 * - Comprehensive performance metrics
 * - Step-by-step explanation of scheduling decisions
 */
public class Q9 {
    static class Process implements Comparable<Process> {
        int pid;           // Process ID
        int arrival;       // Arrival Time
        int burst;        // Original Burst Time
        int priority;     // Priority (unused in this implementation)
        int remaining;    // Remaining Time
        int completion;   // Completion Time
        int turnaround;   // Turnaround Time
        int waiting;      // Waiting Time
        int start = -1;   // First time on CPU
        
        // For tracking RR execution
        ArrayList<TimeSlice> execHistory;
        
        Process(int pid, int arrival, int burst) {
            this.pid = pid;
            this.arrival = arrival;
            this.burst = burst;
            this.remaining = burst;
            this.execHistory = new ArrayList<>();
        }
        
        @Override
        public int compareTo(Process other) {
            return Integer.compare(this.remaining, other.remaining);
        }
    }
    
    static class TimeSlice {
        int start, end;
        TimeSlice(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("\n=== CPU Scheduling Simulation ===");
        System.out.println("1. Shortest Job First (Preemptive/SRTF)");
        System.out.println("2. Round Robin");
        System.out.println("3. Both Algorithms");
        System.out.print("\nChoose algorithm (1-3): ");
        int choice = Integer.parseInt(sc.nextLine());
        
        System.out.print("\nEnter number of processes: ");
        int n = Integer.parseInt(sc.nextLine());
        
        ArrayList<Process> processes = new ArrayList<>();
        System.out.println("\nEnter process details (arrival_time burst_time):");
        for (int i = 0; i < n; i++) {
            System.out.printf("Process P%d: ", i + 1);
            String[] parts = sc.nextLine().trim().split("\\s+");
            int at = Integer.parseInt(parts[0]);
            int bt = Integer.parseInt(parts[1]);
            processes.add(new Process(i + 1, at, bt));
        }

        if (choice == 1 || choice == 3) {
            System.out.println("\n====== Shortest Job First (Preemptive/SRTF) ======");
            System.out.println("Strategy: Execute process with shortest remaining time");
            System.out.println("* Preempts when a shorter job arrives");
            simulateSJF(cloneProcessList(processes));
        }

        if (choice == 2 || choice == 3) {
            System.out.print("\nEnter time quantum for Round Robin: ");
            int quantum = Integer.parseInt(sc.nextLine());
            
            System.out.println("\n====== Round Robin (RR) Scheduling ======");
            System.out.println("Strategy: Give each process a fixed time slice");
            System.out.println("* Preempts after quantum expires");
            System.out.println("* Time Quantum = " + quantum + " units");
            simulateRR(cloneProcessList(processes), quantum);
        }

        sc.close();
    }

    static ArrayList<Process> cloneProcessList(ArrayList<Process> src) {
        ArrayList<Process> copy = new ArrayList<>();
        for (Process p : src) {
            Process newP = new Process(p.pid, p.arrival, p.burst);
            copy.add(newP);
        }
        return copy;
    }

    static void printGanttChart(ArrayList<Process> processes) {
        // Find completion time of last process
        int endTime = 0;
        for (Process p : processes) {
            endTime = Math.max(endTime, p.completion);
        }
        
        // Build timeline
        System.out.println("\nGantt Chart:");
        System.out.print("Time: ");
        for (int i = 0; i <= endTime; i += 5) {
            System.out.printf("%-5d", i);
        }
        System.out.println();
        
        // Print process execution bars
        for (Process p : processes) {
            System.out.printf("P%-2d  ", p.pid);
            int time = 0;
            
            for (TimeSlice ts : p.execHistory) {
                // Print spaces until this slice starts
                while (time < ts.start) {
                    System.out.print(" ");
                    time++;
                }
                // Print execution bar for this slice
                while (time < ts.end) {
                    System.out.print("▄");
                    time++;
                }
            }
            // Fill remaining space with blank
            while (time < endTime) {
                System.out.print(" ");
                time++;
            }
            System.out.println();
        }
        System.out.println();
    }

    static void simulateSJF(ArrayList<Process> processes) {
        int n = processes.size();
        int currentTime = 0;
        int completed = 0;
        
        // Sort processes by arrival time initially
        PriorityQueue<Process> ready = new PriorityQueue<>();
        ArrayList<Process> notArrived = new ArrayList<>(processes);
        notArrived.sort((a, b) -> Integer.compare(a.arrival, b.arrival));
        
        System.out.println("\nProcess Execution Order:");
        Process current = null;
        
        while (completed < n) {
            // Check for new arrivals
            while (!notArrived.isEmpty() && notArrived.get(0).arrival <= currentTime) {
                Process p = notArrived.remove(0);
                ready.offer(p);
                System.out.printf("Time %d: P%d arrived (burst=%d)\n", 
                    currentTime, p.pid, p.remaining);
            }
            
            // Check if we need to preempt current process
            if (current != null && !ready.isEmpty() && 
                ready.peek().remaining < current.remaining) {
                ready.offer(current);
                System.out.printf("Time %d: P%d preempted (remaining=%d)\n",
                    currentTime, current.pid, current.remaining);
                current.execHistory.add(new TimeSlice(
                    currentTime - (current.remaining - ready.peek().remaining),
                    currentTime));
                current = null;
            }
            
            // Get next process to run
            if (current == null && !ready.isEmpty()) {
                current = ready.poll();
                if (current.start == -1) current.start = currentTime;
                System.out.printf("Time %d: P%d started/resumed (remaining=%d)\n",
                    currentTime, current.pid, current.remaining);
            }
            
            // Run current process
            if (current != null) {
                current.remaining--;
                
                // Process completed
                if (current.remaining == 0) {
                    current.completion = currentTime + 1;
                    current.turnaround = current.completion - current.arrival;
                    current.waiting = current.turnaround - current.burst;
                    current.execHistory.add(new TimeSlice(
                        currentTime + 1 - current.burst,
                        currentTime + 1));
                    
                    System.out.printf("Time %d: P%d completed (TAT=%d, WT=%d)\n",
                        currentTime + 1, current.pid, 
                        current.turnaround, current.waiting);
                    
                    completed++;
                    current = null;
                }
            }
            
            currentTime++;
            
            // If no process is ready, advance time to next arrival
            if (current == null && ready.isEmpty() && !notArrived.isEmpty()) {
                currentTime = notArrived.get(0).arrival;
            }
        }
        
        // Print summary
        printSummary(processes, "SJF (Preemptive)");
        printGanttChart(processes);
    }
    
    static void simulateRR(ArrayList<Process> processes, int quantum) {
        int n = processes.size();
        int currentTime = 0;
        int completed = 0;
        
        Queue<Process> ready = new LinkedList<>();
        ArrayList<Process> notArrived = new ArrayList<>(processes);
        notArrived.sort((a, b) -> Integer.compare(a.arrival, b.arrival));
        
        System.out.println("\nProcess Execution Order:");
        Process current = null;
        int timeInQuantum = 0;
        
        while (completed < n) {
            // Check for new arrivals
            while (!notArrived.isEmpty() && notArrived.get(0).arrival <= currentTime) {
                Process p = notArrived.remove(0);
                ready.offer(p);
                System.out.printf("Time %d: P%d arrived (burst=%d)\n",
                    currentTime, p.pid, p.remaining);
            }
            
            // Check if quantum expired
            if (current != null && timeInQuantum == quantum) {
                if (current.remaining > 0) {
                    ready.offer(current);
                    System.out.printf("Time %d: P%d quantum expired (remaining=%d)\n",
                        currentTime, current.pid, current.remaining);
                    current.execHistory.add(new TimeSlice(
                        currentTime - quantum, currentTime));
                }
                current = null;
            }
            
            // Get next process
            if (current == null && !ready.isEmpty()) {
                current = ready.poll();
                timeInQuantum = 0;
                if (current.start == -1) current.start = currentTime;
                System.out.printf("Time %d: P%d started/resumed (remaining=%d)\n",
                    currentTime, current.pid, current.remaining);
            }
            
            // Run current process
            if (current != null) {
                current.remaining--;
                timeInQuantum++;
                
                // Process completed
                if (current.remaining == 0) {
                    current.completion = currentTime + 1;
                    current.turnaround = current.completion - current.arrival;
                    current.waiting = current.turnaround - current.burst;
                    current.execHistory.add(new TimeSlice(
                        currentTime + 1 - timeInQuantum,
                        currentTime + 1));
                    
                    System.out.printf("Time %d: P%d completed (TAT=%d, WT=%d)\n",
                        currentTime + 1, current.pid, 
                        current.turnaround, current.waiting);
                    
                    completed++;
                    current = null;
                }
            }
            
            currentTime++;
            
            // If no process is ready, advance time to next arrival
            if (current == null && ready.isEmpty() && !notArrived.isEmpty()) {
                currentTime = notArrived.get(0).arrival;
            }
        }
        
        // Print summary
        printSummary(processes, "Round Robin (Q=" + quantum + ")");
        printGanttChart(processes);
    }
    
    static void printSummary(ArrayList<Process> processes, String algorithm) {
        // Sort by PID for consistent output
        processes.sort((a, b) -> Integer.compare(a.pid, b.pid));
        
        System.out.println("\nProcess Summary for " + algorithm + ":");
        System.out.println("-".repeat(75));
        System.out.printf("%-6s %-8s %-8s %-10s %-12s %-12s %-8s\n",
            "PID", "Arrival", "Burst", "Start", "Completion", "Turnaround", "Waiting");
        System.out.println("-".repeat(75));
        
        double avgTAT = 0, avgWT = 0;
        for (Process p : processes) {
            System.out.printf("P%-5d %-8d %-8d %-10d %-12d %-12d %-8d\n",
                p.pid, p.arrival, p.burst, p.start,
                p.completion, p.turnaround, p.waiting);
            avgTAT += p.turnaround;
            avgWT += p.waiting;
        }
        System.out.println("-".repeat(75));
        
        System.out.printf("\nPerformance Metrics:\n");
        System.out.printf("Average Turnaround Time = %.2f units\n", avgTAT / processes.size());
        System.out.printf("Average Waiting Time    = %.2f units\n", avgWT / processes.size());
    }
}