import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Model {
    public static boolean statusPrints = false; //false //true
    public static boolean resultPrints = true;
    public static boolean legacyResultPrints = true;
    public static boolean routingPrints = false;
    public static boolean itemsProcessDebugPrints = false;
    public static boolean ProcessDebugPrints = false;
    public static boolean woodenPlankCreatePrints = false;
    public static boolean delayDebugPrints = false;
    public static boolean failureWoodenPlankTypeEnabled = false;

    private int mode = 0;
    private Random newRand = new Random();

    private int cyclesQuantity = 0;
    private int [] successfulWoodenPlankByTypes = new int[3];

    private ArrayList<Element> list = new ArrayList<>();
    private ArrayList<Process> processes = new ArrayList<>();
    private Random random = new Random();
    double tnext;
    double tcurr;
    double simtime;
    int event;

    public Model(ArrayList<Element> elements, int mode) {
        event = 0;
        list.clear();
        list = elements;
        tnext = 0.0;
        event = 0;
        tcurr = tnext;
        this.mode = mode;

        for (Element e : list) {
            if (e instanceof Process) processes.add((Process) e);
        }
    }

    public void simulate(double time) {
        simtime = time;
        while (tcurr < time) {
            tnext = Double.MAX_VALUE;
            for (Element e : list) {
                if (e.getTnext() < tnext) {
                    tnext = e.getTnext();
                    event = e.getId();
                }
            }
            if (statusPrints) System.out.println("\n- Event in " +
                    list.get(event).getName() +
                    ", time = " + tnext);
            for (Element e : list) {
                e.doStatistics(tnext - tcurr);
            }
            tcurr = tnext;
            for (Element e : list) {
                e.setTcurr(tcurr);
            }

            cyclesQuantity++;
            list.get(event).outAct();
            if (list.get(event) instanceof Process) {
                if(mode == 1){ routeProcessExample((Process) list.get(event));}
                if(mode == 0){ routeProcess((Process) list.get(event));}
            }
            else {routeCreated();}
            for (Element e : list) {
                if (e.getTnext() == tcurr) {
                    e.outAct();
                    if (list.get(event) instanceof Process) {
                        if(mode == 1) routeProcessExample((Process) e);
                        if(mode == 0) routeProcess((Process) e);
                    }
                    else {routeCreated();}
                }
            }
            printInfo();
        }
        printResultOfSuccessful();
        printResult();
    }


    private void routeProcess(Process currentProcess) {


        Item item = currentProcess.outActProc();
        int pid = currentProcess.getPid(); // Отримуємо ідентифікатор поточного процесу

        boolean hasValidTransitions = false;

        if (currentProcess.getTransitionType() == 0) { // Імовірнісний перехід
            Map<Integer, Double> probabilities = currentProcess.getTransitionProbabilities();
            double temp = newRand.nextDouble();
            double result = 0;

            for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
                if (entry.getValue() > 0) {
                    hasValidTransitions = true;
                    result += entry.getValue();
                    if (temp < result) {
                        Process nextProcess = findProcess(entry.getKey());
                        if (nextProcess != null) {
                            nextProcess.inAct(item);
                            return;
                        }
                    }
                }
            }
        } else if (currentProcess.getTransitionType() == 1) { // Пріоритетний перехід
            Map<Integer, Integer> priorities = currentProcess.getTransitionPriorities();
            List<Map.Entry<Integer, Integer>> sortedPriorities = new ArrayList<>(priorities.entrySet());

            // Сортування переходів за пріоритетом у порядку спадання
            sortedPriorities.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

            for (Map.Entry<Integer, Integer> entry : sortedPriorities) {
                if (entry.getValue() > 0) {
                    hasValidTransitions = true;
                    Process nextProcess = findProcess(entry.getKey());
                    if (nextProcess != null && !nextProcess.isQueueFull()) {
                        nextProcess.inAct(item);
                        return;
                    }
                }
            }
        }


        // Якщо не було дійсних переходів або не вдалося перейти, обробити відповідно
        if (hasValidTransitions) {
            currentProcess.incrementLostStatistics(); // Втрата елемента
            if(failureWoodenPlankTypeEnabled == true)System.out.println("failure item type: " + item.getTreeSpecies());
        } else {
            currentProcess.incrementSuccessfulStatistics();
            //System.out.println("Successful item type: " + item.getTreeSpecies());
            successfulWoodenPlankByTypes[item.getTreeSpecies()-1]++;
        }
    }


    private Process findProcess(int id) {
        Process pNew = null;
        for(Process p : processes){
            if(p.getPid() == id)pNew = p;
        }
        if(pNew == null){
            try {throw new Exception("id error = " + id);
            } catch (Exception e) {e.printStackTrace();}
        }
        return pNew;
    }
    private void routeProcessExample(Process process) {
        Item woodItem = process.outActProc();
        int pid = process.getPid();
        Process nextProcess = null;

        if(pid == 0) {
            if (woodItem.getTreeSpecies() == 1) { // Type 1 wood goes to final processing (process 1)
                nextProcess = getProcByPid(1);
            } else { // Wood of types 2 and 3 goes to additional processing (process 2)
                nextProcess = getProcByPid(2);
            }
        } else if (pid == 1) { // Final processing, endpoint for type 1 wood
            if(routingPrints) System.out.println("P" + pid + "[S" + woodItem.getTreeSpecies() + "]idx[" + woodItem.getIndex() + "]->|exit");

            if(routingPrints) System.out.println("TIME in workshop = " + woodItem.getTimeInsideCarpentry(process.getTcurr()));
            return;
        } else if (pid == 2) { // Transport to additional processing
            nextProcess = getProcByPid(3);
        } else if (pid == 3) { // Additional processing
            nextProcess = getProcByPid(4);

        } else if (pid == 4) { // Additional processed wood
            if (woodItem.getTreeSpecies() == 2) { // Type 2 wood is returned for final processing (process 5)
                nextProcess = getProcByPid(5);
                woodItem.setTreeSpecies(1);
            } else if (woodItem.getTreeSpecies() == 3){ // Type 3 wood goes to the warehouse or to the customer
                if(routingPrints) System.out.println("P" + pid + "[S" + woodItem.getTreeSpecies() + "]idx[" + woodItem.getIndex() + "]->|exit");

                if(routingPrints) System.out.println("TIME in workshop = " + woodItem.getTimeInsideCarpentry(process.getTcurr()));
                return;
            }
        } else if (pid == 5) { // Transportation to primary processing, route for all types of wood
            nextProcess = getProcByPid(0);
        }
        if(routingPrints) System.out.println("(+" + process.getDelay() + ") P" + pid + "[S" + woodItem.getTreeSpecies() + "]idx[" + woodItem.getIndex() + "]->P" + nextProcess.getPid());
        nextProcess.inAct(woodItem);
    }


    private void routeCreated() {
        Process firstProcess = getProcByPid(0);
        Item item = new Item(tcurr);
        //if(routingPrints) System.out.println("CREATE" + "->P" + firstProcess.getPid() + "_Vidx[" + item.getIndex() + "]");
        firstProcess.inAct(item);
    }



    public void printInfo() {
        if(!statusPrints) return;
        System.out.println("----- Status info -----");
        for (Element e : list) {
            e.printInfo();
        }
    }
    public void printResult() {
        if(!resultPrints && !legacyResultPrints) return;
        System.out.println("\n-------------RESULTS-------------");
        if(legacyResultPrints) {
            for (Element e : list) {
                e.printName();
                if (e instanceof Process) {
                    Process p = (Process) e;
                    System.out.println("total = " + (p.getQuantity() ));
                    System.out.println("mean length of queue = " +
                            p.getMeanQueue() / tcurr);
                    //System.out.println("Failure/Total = " + p.getFailure() + "/" + (p.getQuantity() + p.getFailure()));
                    System.out.println("Load = " + p.getLoad(simtime));
                }
                System.out.println("----------");
            }
        }
        if (resultPrints) {
            for(int i = 0; i < 3; i++){
                System.out.println("successful_ItemsByTypes == " + successfulWoodenPlankByTypes[i]);
            }
        }
    }
    public void printResultOfSuccessful() {
        System.out.println("\n\n========== RESULTS by processes ==========");
        for (Element process : processes) {
            process.printName();
            if (process instanceof Process) {
                Process p = (Process) process;
                System.out.println("total = " + (p.getQuantity() + p.getFailure() + p.getQueue()));
                System.out.println("total processed = " + p.getQuantity());

                System.out.println("total in queue = " + p.getQueue());
                System.out.println("total failed = " + p.getFailure());
                System.out.println("total successful completed = " + p.getSuccessful());
            }
            System.out.println("=======================================");
        }
    }
    private Process getProcByPid(int pid) {
        Process searchedProc = null;
        for(Process proc : processes){
            if(proc.getPid() == pid){
                searchedProc = proc;
            }
        }
        if(searchedProc == null) try {
            throw new Exception("No process created with pid = " + pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchedProc;
    }

    private Create getCreator() {
        Create creator = null;
        for(Element element : list){
            if (!(element instanceof Process)) creator = (Create) element;
        }
        return creator;
    }
    public ArrayList<Process> getProcesses() {
        return processes;
    }

    public void setProcesses(ArrayList<Process> processes) {
        this.processes = processes;
    }
    public int getCyclesQuantity() {
        return cyclesQuantity;
    }

    public void setCyclesQuantity(int cyclesQuantity) {
        this.cyclesQuantity = cyclesQuantity;
    }

    public int[] getSuccessfulWoodenPlankByTypes() {
        return successfulWoodenPlankByTypes;
    }

    public void setSuccessfulWoodenPlankByTypes(int[] successfulWoodenPlankByTypes) {
        this.successfulWoodenPlankByTypes = successfulWoodenPlankByTypes;
    }
    public double getSimtime() {
        return simtime;
    }

    public void setSimtime(double simtime) {
        this.simtime = simtime;
    }
}