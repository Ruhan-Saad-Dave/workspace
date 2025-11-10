// Q6.java
import java.util.*;

class Process {
    String id;
    int arrival, burst, rem, waiting = 0, turnaround = 0, completion = 0;
}

public class Q6 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        Process[] processes = new Process[n];

        for (int i = 0; i < n; i++) {
            processes[i] = new Process();
            System.out.print("Enter Process ID: ");
            processes[i].id = sc.next();
            System.out.print("Enter Arrival Time: ");
            processes[i].arrival = sc.nextInt();
            System.out.print("Enter Burst Time: ");
            processes[i].burst = sc.nextInt();
            processes[i].rem = processes[i].burst; // Remaining time for preemptive scheduling
        }
                sjfPreemptive(processes, n);
                System.out.print("Enter Time Quantum: ");
                int tq = sc.nextInt();
                roundRobin(processes, n, tq);
    }

    // --------------------- SJF Preemptive (Shortest Remaining Time First) ---------------------
    static void sjfPreemptive(Process[] processes, int n) {
        int completed = 0, time = 0;
        boolean[] isComplete = new boolean[n];

        System.out.println("\n--- SJF Preemptive (SRTF) ---");

        while (completed < n) {
            int minIdx = -1, minRem = Integer.MAX_VALUE;

            // Find process with shortest remaining time at current time
            for (int i = 0; i < n; i++) {
                if (processes[i].arrival <= time && !isComplete[i] && processes[i].rem < minRem && processes[i].rem > 0) {
                    minRem = processes[i].rem;
                    minIdx = i;
                }
            }

            if (minIdx == -1) {
                time++; // No process is ready; increment time
                continue;
            }

            // Execute one unit of the selected process
            processes[minIdx].rem--;
            time++;

            // If process completed
            if (processes[minIdx].rem == 0) {
                completed++;
                processes[minIdx].completion = time;
                processes[minIdx].turnaround = processes[minIdx].completion - processes[minIdx].arrival;
                processes[minIdx].waiting = processes[minIdx].turnaround - processes[minIdx].burst;
                isComplete[minIdx] = true;
            }
        }

        // Display results
        int totalWaiting = 0, totalTurnaround = 0;
        System.out.println("Process\tWaiting\tTurnaround");
        for (Process p : processes) {
            System.out.println(p.id + "\t" + p.waiting + "\t" + p.turnaround);
            totalWaiting += p.waiting;
            totalTurnaround += p.turnaround;
        }

        System.out.println("Average Waiting Time: " + (float) totalWaiting / n);
        System.out.println("Average Turnaround Time: " + (float) totalTurnaround / n);
    }

    // --------------------- Round Robin (Preemptive) ---------------------
    static void roundRobin(Process[] processes, int n, int tq) {
        int[] rem = new int[n];
        int[] waiting = new int[n], turnaround = new int[n];
        boolean[] inQueue = new boolean[n];
        Queue<Integer> q = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            rem[i] = processes[i].burst;
        }

        int complete = 0, time = 0;

        // Add first arriving processes to queue at t=0
        for (int i = 0; i < n; i++) {
            if (processes[i].arrival == 0) {
                q.add(i);
                inQueue[i] = true;
            }
        }

        while (complete < n) {
            if (q.isEmpty()) {
                // If queue empty, find next arriving process
                int minArrival = Integer.MAX_VALUE, idx = -1;
                for (int i = 0; i < n; i++) {
                    if (!inQueue[i] && rem[i] > 0 && processes[i].arrival < minArrival) {
                        minArrival = processes[i].arrival;
                        idx = i;
                    }
                }
                time = minArrival;
                q.add(idx);
                inQueue[idx] = true;
            }

            int idx = q.poll();
            int execTime = Math.min(tq, rem[idx]);
            time = Math.max(time, processes[idx].arrival) + execTime;
            rem[idx] -= execTime;

            // Add newly arrived processes
            for (int i = 0; i < n; i++) {
                if (!inQueue[i] && rem[i] > 0 && processes[i].arrival <= time) {
                    q.add(i);
                    inQueue[i] = true;
                }
            }

            if (rem[idx] > 0) {
                q.add(idx);
            } else {
                complete++;
                turnaround[idx] = time - processes[idx].arrival;
                waiting[idx] = turnaround[idx] - processes[idx].burst;
            }
        }

        // Display results
        int totalWaiting = 0, totalTurnaround = 0;
        System.out.println("\n--- Round Robin (Preemptive) ---");
        System.out.println("Process\tWaiting\tTurnaround");

        for (int i = 0; i < n; i++) {
            System.out.println(processes[i].id + "\t" + waiting[i] + "\t" + turnaround[i]);
            totalWaiting += waiting[i];
            totalTurnaround += turnaround[i];
        }

        System.out.println("Average Waiting Time: " + (float) totalWaiting / n);
        System.out.println("Average Turnaround Time: " + (float) totalTurnaround / n);
    }
}