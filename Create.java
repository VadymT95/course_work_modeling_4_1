public class Create extends Element {

    public Create(double delay, String distribution, String name) {
        super(delay);
        super.setDistribution(distribution);
        super.setName(name);

        super.setTnext(0.0); // Simulation starts with Create event
    }

    @Override
    public void outAct() {
        super.outAct();
        super.setTnext(super.getTcurr() + super.getDelay());
    }

}
