import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

class Process {
    int pid, at, bt, prio, rbt, ft, wt, tat; //processId, ArrivalTime, BurstTime, Priority, RemainingBurstTime, FinishTime, WaitingTime, TurnAroundTime
    
    public Process(int pid, int at, int bt, int pri) {
        this.pid = pid; 
        this.at = at; 
        this.bt = bt; 
        this.prio = pri; 
        this.rbt = bt; // rbt (RemainingBurstTime) is set to full BurstTime initially
    }
    
    public String toString() {
        return String.format("    %d\t\t%d\t\t%d\t\t%d\t\t%d\t\t%d\t\t%d", pid, at, bt, prio, ft, wt, tat);
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
            System.out.print("Enter Priority: ");    
            int pri = sc.nextInt();
            pList.add(new Process(i + 1, at, bt, pri));
        }
        
        // Run only the algorithms specified in the problem statement
        runFCFS(new ArrayList<>(pList));
        runPriorityNonPreemptive(new ArrayList<>(pList));
        
        sc.close();
    }

    public static void runFCFS(List<Process> pList) {
        // Sort processes by Arrival Time
        Collections.sort(pList, Comparator.comparingInt(p -> p.at));
        
        int ct = 0; // Current Time
        for (Process p : pList) {
            // Handle CPU idle time
            if (ct < p.at)    
                ct = p.at;
                
            p.ft = ct + p.bt; // Finish Time = Current Time + Burst Time
            p.tat = p.ft - p.at; // Turnaround Time = Finish Time - Arrival Time
            p.wt = p.tat - p.bt; // Waiting Time = Turnaround Time - Burst Time
            ct = p.ft; // Update Current Time
        }
        printResults(pList, "First-Come, First-Served (FCFS)");
    }

    public static void runPriorityNonPreemptive(List<Process> pList) {
        int ct = 0, comp = 0, n = pList.size(); // Current Time, Completed Processes
        
        while (comp < n) {
            Process hp = null; // Highest Priority Process
            int maxPri = Integer.MAX_VALUE; // Lower number means higher priority
            
            // Find the highest priority process that has arrived and is not finished
            for (Process p : pList) {
                if (p.at <= ct && p.rbt > 0 && p.prio < maxPri) {
                    maxPri = p.prio;
                    hp = p;
                }
            }
            
            if (hp == null) {
                // No process is ready, CPU is idle
                ct++; 
            } else {
                // Run the selected process to completion (non-preemptive)
                ct += hp.bt;
                hp.ft = ct;
                hp.tat = hp.ft - hp.at;
                hp.wt = hp.tat - hp.bt;
                hp.rbt = 0; // Mark process as completed
                comp++;
            }
        }
        printResults(pList, "Priority (Non-Preemptive)");
    }

    public static void printResults(List<Process> pList, String name) {
        double totWT = 0, totTAT = 0;    
        int n = pList.size();

        System.out.println("\n\n--- Results for: " + name + " ---");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("  PID\t\tAT\t\tBT\t\tPriority\tFT\t\tWT\t\tTAT");
        System.out.println("-----------------------------------------------------------------------------------------");
        
        // Sort by PID for a clean final output
        Collections.sort(pList, Comparator.comparingInt(p -> p.pid));
        
        for (Process p : pList) {
            System.out.println(p.toString());
            totWT += p.wt;    
            totTAT += p.tat;
        }
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("Average Waiting Time: %.2f\n", (totWT / n));
        System.out.printf("Average Turnaround Time: %.2f\n", (totTAT / n));
    }
}