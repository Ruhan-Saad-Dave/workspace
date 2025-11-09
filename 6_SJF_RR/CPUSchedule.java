import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

class Process {
    // Priority (prio) field has been removed
    int pid, at, bt, rbt, ft, wt, tat; //processId, ArrivalTime, BurstTime, RemainingBurstTime, FinishTime, WaitingTime, TurnAroundTime
    
    // Constructor no longer takes priority
    public Process(int pid, int at, int bt) {
        this.pid = pid; 
        this.at = at; 
        this.bt = bt; 
        this.rbt = bt;
    }
    
    // toString updated to remove priority column
    public String toString() {
        return String.format("    %d\t\t%d\t\t%d\t\t%d\t\t%d\t\t%d", pid, at, bt, ft, wt, tat);
    }
}

public class CPUSchedule {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Process> pList = new ArrayList<>();
        
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        
        for (int i = 0; i < n; i++) {
            System.out.println("\n--- Process " + (i + 1) + " ---");
            System.out.print("Enter Arrival Time: ");    
            int at = sc.nextInt();
            System.out.print("Enter Burst Time: ");    
            int bt = sc.nextInt();
            // Priority prompt has been removed
            pList.add(new Process(i + 1, at, bt));
        }

        // Ask for Time Quantum for Round Robin
        System.out.print("\nEnter Time Quantum for Round Robin: ");
        int quantum = sc.nextInt();
        
        // Run only the algorithms specified in the problem statement
        runSJFPreemptive(new ArrayList<>(pList));
        runRoundRobin(new ArrayList<>(pList), quantum);
        
        sc.close();
    }

    public static void runSJFPreemptive(List<Process> pList) {
        int ct = 0, comp = 0, n = pList.size();
        
        while (comp < n) {
            Process sp = null; // Shortest Process
            int minRem = Integer.MAX_VALUE;
            
            // Find the process with the minimum remaining time among arrived processes
            for (Process p : pList) {
                if (p.at <= ct && p.rbt > 0 && p.rbt < minRem) {
                    minRem = p.rbt;
                    sp = p;
                }
            }
            
            if (sp == null) {
                // No process is ready, CPU is idle
                ct++;
            } else {
                // Run the shortest process for one time unit
                sp.rbt--;
                ct++;
                
                // Check if the process has completed
                if (sp.rbt == 0) {
                    sp.ft = ct;
                    sp.tat = sp.ft - sp.at;
                    sp.wt = sp.tat - sp.bt;
                    comp++;
                }
            }
        }
        printResults(pList, "Shortest-Job-First (SJF) - Preemptive");
    }

    public static void runRoundRobin(List<Process> pList, int quantum) {
        Queue<Process> readyQueue = new LinkedList<>();
        // Sort a copy of the list by arrival time
        List<Process> sortedList = new ArrayList<>(pList);
        Collections.sort(sortedList, Comparator.comparingInt(p -> p.at));

        int ct = 0, comp = 0, n = pList.size();
        int nextArrivalIndex = 0;
        Process currentProcess = null;
        int currentQuantumTime = 0;

        while (comp < n) {
            // Add all processes that have arrived by the current time (ct)
            while (nextArrivalIndex < n && sortedList.get(nextArrivalIndex).at <= ct) {
                readyQueue.add(sortedList.get(nextArrivalIndex));
                nextArrivalIndex++;
            }
            
            // Check if CPU is free
            if (currentProcess == null) {
                if (readyQueue.isEmpty()) {
                    // No process has arrived or is ready, CPU is idle
                    ct++;
                    continue;
                } else {
                    // CPU is free, pick next process from queue
                    currentProcess = readyQueue.poll();
                    currentQuantumTime = 0;
                }
            }

            // Execute one time tick
            ct++;
            currentProcess.rbt--;
            currentQuantumTime++;
            
            // Check if the current process is finished
            if (currentProcess.rbt == 0) {
                currentProcess.ft = ct;
                currentProcess.tat = currentProcess.ft - currentProcess.at;
                currentProcess.wt = currentProcess.tat - currentProcess.bt;
                comp++;
                currentProcess = null; // CPU is now free
            }
            // Check if the time quantum has expired
            else if (currentQuantumTime == quantum) {
                // Add new arrivals *before* re-adding the current process
                while (nextArrivalIndex < n && sortedList.get(nextArrivalIndex).at <= ct) {
                    readyQueue.add(sortedList.get(nextArrivalIndex));
                    nextArrivalIndex++;
                }
                readyQueue.add(currentProcess); // Add current process to back of queue
                currentProcess = null; // CPU is now free
            }
        }
        printResults(pList, "Round Robin (RR)");
    }


    public static void printResults(List<Process> pList, String name) {
        double totWT = 0, totTAT = 0;    
        int n = pList.size();

        System.out.println("\n\n--- Results for: " + name + " ---");
        // Updated header to remove Priority
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("  PID\t\tAT\t\tBT\t\tFT\t\tWT\t\tTAT");
        System.out.println("---------------------------------------------------------------------------------");
        
        // Sort by PID for a clean final output
        Collections.sort(pList, Comparator.comparingInt(p -> p.pid));
        
        for (Process p : pList) {
            System.out.println(p.toString());
            totWT += p.wt;    
            totTAT += p.tat;
        }
        System.out.println("---------------------------------------------------------------------------------");
        System.out.printf("Average Waiting Time: %.2f\n", (totWT / n));
        System.out.printf("Average Turnaround Time: %.2f\n", (totTAT / n));
    }
}