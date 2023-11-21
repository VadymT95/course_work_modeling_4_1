import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BasicSimulation {


    public Model startModelWithPriorities(int numberOfProcesses){
        ArrayList<Element> list = new ArrayList<>();
        Create creator1 = new Create(1.0,  "exp", "CREATOR");
        list.add(creator1);

        for (int i = 0; i < numberOfProcesses; i++) {
            Map<Integer, Integer> transitionPriorities = new HashMap<>();

            // Встановлення пріоритетів для кожного процесу
            switch (i) {
                case 0:
                    transitionPriorities.put(1, 1); // Пріоритет 1 на СМО 1
                    break;
                case 1:
                    transitionPriorities.put(3, 2); // Пріоритет 2 на СМО 3
                    transitionPriorities.put(2, 1); // Пріоритет 1 на СМО 2
                    break;
                case 2:
                    transitionPriorities.put(4, 1); // Пріоритет 1 на СМО 4
                    break;
                case 4:
                    transitionPriorities.put(5, 2); // Пріоритет 2 на СМО 5
                    transitionPriorities.put(6, 3); // Пріоритет 1 на СМО 6
                    break;
                case 5:
                    transitionPriorities.put(6, 1); // Пріоритет 1 на СМО 6
                    break;
            }

            Process proc1 = new Process(3.0, 2, i, 3,"exp", null,  transitionPriorities, 1); // 1 - тип переходу за пріоритетами
            proc1.setIsSortingEnabled(1);
            proc1.setQueueModificationAllowed(true);
            proc1.setMaintenanceModeEnabled(false);
            proc1.setBlockEndTime(25);
            list.add(proc1);
        }

        Model model = new Model(list, 0);
        return model;
    }
    public Model startModel(int numberOfProcesses){
        ArrayList<Element> list = new ArrayList<>();
        Create creator1 = new Create(1.0,  "exp", "CREATOR");
        list.add(creator1);

        for (int i = 0; i < numberOfProcesses; i++) {
            Map<Integer, Double> transitionProbabilities = new HashMap<>();
            if (i + 1 < numberOfProcesses) {

                transitionProbabilities.put(i + 1, 1.0);
            }

            Process proc1 = new Process(10.0, 15, i, 30,"exp", transitionProbabilities,  null, 0); // 1 - тип переходу за пріоритетами
            proc1.setDelayDev(0.3);
            proc1.setIsSortingEnabled(1);
            proc1.setQueueModificationAllowed(true);
            proc1.setMaintenanceModeEnabled(true);
            list.add(proc1);
        }


        Model model = new Model(list, 0);
        return model;
    }




    public Model createBasicModel(int numberOfProcesses) {
        return startModel(numberOfProcesses);
    }


}
