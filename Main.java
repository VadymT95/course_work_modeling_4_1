public class Main {
    public static void main(String[] args) {
        final int simulationRuns = 2;

        double[] totalElements = new double[simulationRuns];
        double[] finalProcessed = new double[simulationRuns];
        double[] loadOnSMO1 = new double[simulationRuns];
        double[] loadOnSMO7 = new double[simulationRuns];
        double[] processedType1 = new double[simulationRuns];
        double[] processedType2 = new double[simulationRuns];
        double[] processedType3 = new double[simulationRuns];
        double[] simulationTimes = new double[simulationRuns];

        for (int i = 0; i < simulationRuns; i++) {
            long startTime = System.currentTimeMillis();

            BasicSimulation main = new BasicSimulation();
            Model model = main.createBasicModel(20);

            Element.nextId = 0;

            model.simulate(1000.0);

            long endTime = System.currentTimeMillis();
            double executionTime = (endTime - startTime) / 1000.0;


            totalElements[i] = model.getProcesses().get(0).getQuantity();
            finalProcessed[i] = model.getProcesses().get(19).getSuccessful();
            loadOnSMO1[i] = model.getProcesses().get(0).getLoad(model.getSimtime());
            loadOnSMO7[i] = model.getProcesses().get(19).getLoad(model.getSimtime());
            processedType1[i] = model.getSuccessfulWoodenPlankByTypes()[0];
            processedType2[i] = model.getSuccessfulWoodenPlankByTypes()[1];
            processedType3[i] = model.getSuccessfulWoodenPlankByTypes()[2];
            simulationTimes[i] = executionTime;

            System.out.println("Execution time for run " + (i+1) + ": " + executionTime + " seconds");
        }


        printStatistics("Total Elements", totalElements);
        printStatistics("Final Processed", finalProcessed);
        printStatistics("Load on SMO 1", loadOnSMO1);
        printStatistics("Load on SMO 7", loadOnSMO7);
        printStatistics("Processed Type 1", processedType1);
        printStatistics("Processed Type 2", processedType2);
        printStatistics("Processed Type 3", processedType3);
        printStatistics("Simulation Time", simulationTimes);
    }

    private static void printStatistics(String name, double[] values) {
        System.out.println("=== " + name + " ===");
        System.out.println("Average: " + calculateAverage(values));
        System.out.println("Min: " + findMin(values));
        System.out.println("Max: " + findMax(values));
        System.out.println();
    }

    private static double calculateAverage(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private static double findMin(double[] values) {
        double min = values[0];
        for (double value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    private static double findMax(double[] values) {
        double max = values[0];
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
