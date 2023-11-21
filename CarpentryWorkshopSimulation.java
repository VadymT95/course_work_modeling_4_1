import java.util.ArrayList;

public class CarpentryWorkshopSimulation {

    public Model createCarpentryWorkshopModel() {
        ArrayList<Element> list = new ArrayList<>();
        int queueLengthGeneral = 50;

        addCreatorToList(15, "exp", "WOOD_ARRIVAL", list); // Прибуття дерев'яних заготовок

        // Первинна обробка заготовок
        Process primaryProcessing = new Process(1, 0, 5, "exp");
        primaryProcessing.setName("primaryProcessing");
        list.add(primaryProcessing);

        // Перевезення до місця фінальної обробки
        Process finalProcessingTransport = new Process(3, 1, 50, "unif");
        finalProcessingTransport.setUnifMin(3);
        finalProcessingTransport.setUnifMax(8);
        finalProcessingTransport.setName("finalProcessingTransport");
        list.add(finalProcessingTransport);

        // Тимчасове зберігання заготовок
        Process storageArea = new Process(100, 2, 40, "unif");
        storageArea.setUnifMin(2);
        storageArea.setUnifMax(5);
        storageArea.setName("storageArea");
        list.add(storageArea);

        // Додаткова обробка (фугування, рейсмусування)
        Process additionalProcessing = new Process(1, 3, 5, "erlang");
        additionalProcessing.setDelayMean(4.5);
        additionalProcessing.setErlangK(3);
        additionalProcessing.setName("additionalProcessing");
        list.add(additionalProcessing);

        // Фінальна обробка заготовок
        Process finalProcessing = new Process(2, 4, 5, "erlang");
        finalProcessing.setDelayMean(4);
        finalProcessing.setErlangK(2);
        finalProcessing.setName("finalProcessing");
        list.add(finalProcessing);

        // Перевезення оброблених заготовок
        Process processedWoodTransport = new Process(100, 5, 40, "unif");
        processedWoodTransport.setUnifMin(2);
        processedWoodTransport.setUnifMax(5);
        processedWoodTransport.setName("processedWoodTransport");
        list.add(processedWoodTransport);

        Model model = new Model(list, 1);
        return model;
    }

    private void addCreatorToList(double delay, String distribution, String name, ArrayList<Element> list) {
        Create woodArrival = new Create(delay, distribution, name);
        list.add(woodArrival);
    }
}
