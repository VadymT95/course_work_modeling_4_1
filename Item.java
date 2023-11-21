import java.util.Random;

public class Item {
    private int treeSpecies;
    private Random random = new Random();
    private int[] probabilities = new int[]{50, 10, 40};
    private int[] delayTable = new int[]{25, 40, 30};
    private double entryTime, exitTime;
    private int index = 0;
    private static int totalIndexCounter = 0;

    public Item(double entryTime){
        index = totalIndexCounter;
        totalIndexCounter++;
        setEntryTime(entryTime);
        double rnd = random.nextInt(100);
        double sum = 0;

        for (int i = 1; i < 4; i++) {
            sum += probabilities[i-1];
            if (rnd < sum) {
                if(Model.woodenPlankCreatePrints) System.out.println("NewTreeSpecies, T" + i);
                setTreeSpecies(i);
                break;
            }
        }
    }

    public double getTimeInsideCarpentry(double exitTime){
        setExitTime(exitTime);
        double timeInCarpentry = exitTime - entryTime;
        return timeInCarpentry;
    }

    public int getDelay(){
        return delayTable[(getTreeSpecies()-1)];
    }

    public int getTreeSpecies() {
        return treeSpecies;
    }

    public void setTreeSpecies(int treeSpecies) {
        this.treeSpecies = treeSpecies;
    }

    public double getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(double entryTime) {
        this.entryTime = entryTime;
    }

    public double getExitTime() {
        return exitTime;
    }

    public void setExitTime(double exitTime) {
        this.exitTime = exitTime;
    }

    public int getIndex() {
        return index;
    }
}
