import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;

public class Process extends Element {
    private int queue;
    private int maxqueue;
    private int failure;
    private int successful;
    private double meanQueue;
    private ArrayList<Channel> channels;
    private int numberOfChannels;
    private int pid;
    private ArrayList<Item> visitorsQueue = new ArrayList<>();

    private Map<Integer, Double> transitionProbabilities; // Список імовірностей переходів
    private Map<Integer, Integer> transitionPriorities; // Список пріоритетів переходів
    private int transitionType; // Тип переходу: 0 - за імовірностями, 1 - за пріоритетом
    private int mode = 0;
    private int isSortingEnabled = 0;
    private boolean queueModificationAllowed = false;
    private boolean isBlocked;
    private double blockEndTime = 10;
    private boolean maintenanceModeEnabled;


    public Process(double delay, int numberOfChannels, int pid, int queueLen, String distribution,
                   Map<Integer, Double> transitionProbabilities, Map<Integer, Integer> transitionPriorities, int transitionType) {
        super(delay);
        this.transitionProbabilities = transitionProbabilities;
        this.transitionPriorities = transitionPriorities;
        this.transitionType = transitionType;
        setParams(numberOfChannels, pid, queueLen, distribution);
    }
    public Process(double delay,  int numberOfChannels, int pid, int queueLen, String distribution) {
        super(delay);
        setParams(numberOfChannels, pid, queueLen, distribution);
    }
    public Process( int numberOfChannels, int pid, int queueLen, String distribution) {
        super(0);
        setParams(numberOfChannels, pid, queueLen, distribution);
    }


    private void setParams(int numberOfChannels, int pid, int queueLen, String distribution) {
        queue = 0;
        maxqueue = queueLen;
        setDistribution(distribution);
        meanQueue = 0.0;
        setPid(pid);
        this.numberOfChannels = numberOfChannels;
        channels = new ArrayList<>(numberOfChannels);
        for (int i = 0; i < numberOfChannels; i++) {
            channels.add(new Channel());
        }
    }
    public void sortVisitorsByType() {
        Collections.sort(visitorsQueue, new Comparator<Item>() {
            @Override
            public int compare(Item v1, Item v2) {
                return Integer.compare(v2.getTreeSpecies(), v1.getTreeSpecies());
            }
        });
    }


        public double getLoad(double totalTime) {
        double timeInActiveStateAllChan = channels.stream()
                .mapToDouble(o -> o.timeInActiveState)
                .sum();

        if (Model.ProcessDebugPrints){
            System.out.println("-- Load per Channel:");
            channels.stream()
                    .mapToDouble(o -> o.timeInActiveState).limit(3)
                    .forEach(s -> System.out.println(s/totalTime));
        }

        return timeInActiveStateAllChan / (totalTime * numberOfChannels);
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }


    public Item getFirstT1OrElseT2T3Visitor() {
        Item item = getFirstT1Visitor();
        if(item != null) return item;
        item = visitorsQueue.get(0);
        if (item != null){
            visitorsQueue.remove(0);
            return item;
        }
        return null;
    }

    public Item getFirstT1Visitor() {
        if(!isThereT1Visitor()) return null;
        for(int i = 0; i < visitorsQueue.size(); i++){
            Item item = visitorsQueue.get(i);
            if(item.getTreeSpecies() == 1) {
                visitorsQueue.remove(i);
                return item;
            }
        }
        return null;
    }

    public boolean isThereT1Visitor() {
        for(Item item : visitorsQueue){
            if(item.getTreeSpecies() == 1) return true;
        }
        return false;
    }


    private class Channel {
        private double tnext;
        private int state;
        private double timeInActiveState = 0.0;
        private double lastStateChangeTime = 0.0;
        private Item currentItem = null;

        public Channel() {
            this.tnext = Double.MAX_VALUE;
            setState(0);
        }

        public void setState(int state){ // Override to calculate load here
            double currentModelTime = getTcurr();

            if (this.getState() == 1) {
                timeInActiveState += (currentModelTime - lastStateChangeTime);
            }
            lastStateChangeTime = currentModelTime;

            this.state = state;
        }

        public void inAct(Item item) {
            setState(1);
            currentItem = item;
            if(getPid() == 0 && mode == 1) setDelayMean(item.getDelay());
            if(Model.itemsProcessDebugPrints && getPid()==0){
                System.out.println("Pid[" + getPid() + "]Item[" + item.getTreeSpecies() + "]_(MEANdel=" + getDelayMean() + ")_ENTRYdelay==" + getDelay());
            }

            this.tnext = getTcurr() + getDelay();
        }

        public Item outAct() {
            Item item = currentItem;
            currentItem = null;
            setState(0);
            this.tnext = Double.MAX_VALUE;
            return item;
        }

        public double getTnext() {
            return tnext;
        }

        public int getState() {
            return state;
        }
    }
    public void incrementLostStatistics() {
        failure++;
    }
    public void incrementSuccessfulStatistics() {
        successful++;
    }
    public boolean isQueueFull() {

        for (Channel channel : channels) {
            if (channel.getState() == 0) {
                return false;
            }
        }
        return true;
    }
    private int getLastVisitorType() {
        if (!visitorsQueue.isEmpty()) {
            return visitorsQueue.get(visitorsQueue.size() - 1).getTreeSpecies();
        }
        return -1;
    }
    public void inAct(Item item) {
        manageBlock();
        if (isBlocked) {
            System.out.println("Process is under maintenance. Item rejected.");
            failure++;
            return;
        }

        Channel freeChannel = channels.stream()
                .filter(c -> c.getState() == 0)
                .findFirst()
                .orElse(null);

        if (freeChannel != null) {
            if (pid == 0 && mode == 1) super.setDelayMean(item.getDelay());
            freeChannel.inAct(item);
        } else {
            if (getQueue() < getMaxqueue()) {
                visitorsQueue.add(item);
                if (isSortingEnabled == 1) sortVisitorsByType();
                setQueue(getQueue() + 1);
            } else {
                // Режим, що дозволяє заміну в черзі
                if (queueModificationAllowed == true && getLastVisitorType() < item.getTreeSpecies()) {
                    int rejectedType = getLastVisitorType(); // Тип відхиленого відвідувача
                    if(Model.failureWoodenPlankTypeEnabled) {
                        System.out.println("Failure item type: " + rejectedType);
                    }
                    visitorsQueue.remove(visitorsQueue.size() - 1);
                    visitorsQueue.add(item);
                    sortVisitorsByType();
                } else {
                    if(Model.failureWoodenPlankTypeEnabled) {
                        System.out.println("Failure item type: " + item.getTreeSpecies());
                    }
                }
                failure++;
            }
        }
    }

    public Item outActProc() {
        Item item = null;

        Channel channelToRelease = channels.stream()
                .filter(c -> c.getTnext() == getTcurr())
                .findFirst()
                .orElse(null);

        if (channelToRelease != null) {
            item = channelToRelease.outAct();
        }

        // Execute orders from queue:
        if (getQueue() > 0) {
            Channel nextFreeChannel = channels.stream()
                    .filter(c -> c.getState() == 0)
                    .findFirst()
                    .orElse(null);

            if (nextFreeChannel != null) {
                setQueue(getQueue() - 1);
                Item firstQueueAppropriateItem = getFirstT1OrElseT2T3Visitor();
                if (pid == 0 && mode == 1) super.setDelayMean(item.getDelay());
                nextFreeChannel.inAct(firstQueueAppropriateItem);
            }
        }

        return item;
    }
    public void manageBlock() {
        checkIfBlocked(() ->
                maintenanceModeEnabled && getQuantity() % 100 == 1 && getQuantity() >= 100 && !isBlocked
        );
    }
    private boolean checkIfBlocked(Supplier<Boolean> blockCondition) {
        if (maintenanceModeEnabled && isBlocked && getTcurr() < blockEndTime) {
            return true;
        }

        if (blockCondition.get()) {
            isBlocked = true;
            blockEndTime = getTcurr() + 10;
            return true;
        }

        isBlocked = false; // Зняття блокування
        return false;
    }
    @Override
    public double getTnext() { // Get minimal Time of end of any channel
        return channels.stream()
                .mapToDouble(Channel::getTnext)
                .min()
                .orElse(Double.MAX_VALUE);
    }
    public int getSuccessful() {
        return successful;
    }

    public void setSuccessful(int successful) {
        this.successful = successful;
    }

    public int getFailure() {
        return failure;
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public int getMaxqueue() {
        return maxqueue;
    }

    public void setMaxqueue(int maxqueue) {
        this.maxqueue = maxqueue;
    }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("queue = " + this.getQueue());
        System.out.println("failure = " + this.getFailure());
    }

    @Override
    public void doStatistics(double delta) {
        meanQueue = getMeanQueue() + queue * delta;
    }

    @Override
    public String getName() {
        return (super.getName() + " id: " + Integer.toString(pid));
    }

    public double getMeanQueue() {
        return meanQueue;
    }

    public Map<Integer, Double> getTransitionProbabilities() {
        return transitionProbabilities;
    }

    public void setTransitionProbabilities(Map<Integer, Double> transitionProbabilities) {
        this.transitionProbabilities = transitionProbabilities;
    }

    public Map<Integer, Integer> getTransitionPriorities() {
        return transitionPriorities;
    }

    public void setTransitionPriorities(Map<Integer, Integer> transitionPriorities) {
        this.transitionPriorities = transitionPriorities;
    }

    public int getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(int transitionType) {
        this.transitionType = transitionType;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
    public int getIsSortingEnabled() {
        return isSortingEnabled;
    }

    public void setIsSortingEnabled(int isSortingEnabled) {
        this.isSortingEnabled = isSortingEnabled;
    }

    public boolean isQueueModificationAllowed() {
        return queueModificationAllowed;
    }

    public void setQueueModificationAllowed(boolean queueModificationAllowed) {
        this.queueModificationAllowed = queueModificationAllowed;
    }
    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public double getBlockEndTime() {
        return blockEndTime;
    }

    public void setBlockEndTime(double blockEndTime) {
        this.blockEndTime = blockEndTime;
    }

    public boolean isMaintenanceModeEnabled() {
        return maintenanceModeEnabled;
    }

    public void setMaintenanceModeEnabled(boolean maintenanceModeEnabled) {
        this.maintenanceModeEnabled = maintenanceModeEnabled;
    }
}
