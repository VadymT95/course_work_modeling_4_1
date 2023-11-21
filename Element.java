public class Element {
    private String name;
    private double tnext;
    private double delayMean, delayDev, unifMin, unifMax;
    private int erlangK;
    private String distribution;
    private int quantity;
    private double tcurr;
    private int state;
    public static int nextId=0;
    private int id;

    public Element(double delay){
        tnext = 0.0;
        delayMean = delay;
        distribution = "";
        tcurr = tnext;
        state=0;
        id = nextId;
        nextId++;
        name = "element"+id;
    }

    public double getDelay() {
        double delay = getDelayMean();
        if ("exp".equalsIgnoreCase(getDistribution())) {
            delay = FunRand.Exp(getDelayMean());
        } else if ("norm".equalsIgnoreCase(getDistribution())) {
            delay = FunRand.Norm(getDelayMean(), getDelayDev());
        } else if ("unif".equalsIgnoreCase(getDistribution())) {
            delay = FunRand.Unif(unifMin, unifMax);
        } else if("static".equalsIgnoreCase(getDistribution())) {
            delay = getDelayMean();
        } else if("erlang".equalsIgnoreCase(getDistribution())) {
            delay = FunRand.Erlang(getDelayMean(), erlangK);
        } else if("".equalsIgnoreCase(getDistribution())) {
            delay = getDelayMean();
        }

        if (Model.delayDebugPrints) {
            System.out.println("[delayDEBUG] (" + delay + "), distribution='" + getDistribution() + "', mean=" + getDelayMean() + ", erlK=" + erlangK);
            if("unif".equalsIgnoreCase(getDistribution())) System.out.println("[delayDEBUG] (" + unifMin + ", " + unifMax + ")");
        }

        return delay;
    }



    public double getDelayDev() {
        return delayDev;
    }
    public void setDelayDev(double delayDev) {
        this.delayDev = delayDev;
    }
    public String getDistribution() {
        return distribution;
    }
    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public int getQuantity() {
        return quantity;
    }
    public double getTcurr() {
        return tcurr;
    }
    public void setTcurr(double tcurr) {
        this.tcurr = tcurr;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public void inAct() {

    }
    public void outAct(){
        quantity++;
    }

    public double getTnext() {
        return tnext;
    }
    public void setTnext(double tnext) {
        this.tnext = tnext;
    }
    public double getDelayMean() {
        return delayMean;
    }
    public void setDelayMean(double delayMean) {
        this.delayMean = delayMean;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void printName(){
        System.out.println(getName());
    }

    public void printInfo(){
        System.out.println(getName()+ " state= " +state+
                " quantity = "+ quantity+
                " tnext= "+tnext);
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void doStatistics(double delta){

    }

    public void setErlangK(int erlangK) {
        this.erlangK = erlangK;
    }

    public void setUnifMin(double unifMin) {
        this.unifMin = unifMin;
    }

    public void setUnifMax(double unifMax) {
        this.unifMax = unifMax;
    }
    public static int getNextId() {
        return nextId;
    }


}


