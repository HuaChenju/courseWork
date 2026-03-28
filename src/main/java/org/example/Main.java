package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.argregationservice.AgregationService;
import org.example.storageservice.CsvStorageService;
import org.example.storageservice.JsonStorageService;
import org.example.storageservice.StorageService;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("not enough args");
        }
        String typeWork = args[0];
        if (typeWork.equals("automatice")) {
            if (args.length < 3) {
                throw new RuntimeException("not enough args");
            }
            automaticeWork(args);
        } else if (typeWork.equals("interactive")) {
            interactiveWork();
        } else {
            System.err.println("error: unknown work type");
            System.exit(1);
        }
    }

    private static void automaticeWork(String[] args) {
        AgregationService service = new AgregationService();
        List<String> apiNames =  new ArrayList<>();
        String fileType = "json";
        StorageService storageService = new JsonStorageService();
        int n = 1, t = 10;
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "json" -> fileType = "json";
                case "csv" -> fileType = "csv";
                default -> {
                    if (args[i].startsWith("n=")) {
                        n = Integer.parseInt(args[i].substring(2));
                    } else if (args[i].startsWith("t=")) {
                        t = Integer.parseInt(args[i].substring(2));
                    } else {
                        apiNames.add(args[i]);
                    }
                }

            }
        }
        String fileName = "outputAutomatice." + fileType;

        if (fileType.equals("csv")) {
            storageService = new CsvStorageService();
        }

        try {
            service.startPolling(n, t, apiNames, fileName, false, storageService);
            Thread.sleep(t * 2000L);
            service.stopPolling();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
        }


    }

    public static void interactiveWork() {


        Scanner scanner = new Scanner(System.in);
        StorageService storageService = new JsonStorageService();
        int n = 1, t = 10;
        AgregationService service = new AgregationService();

        while (true) {

            System.out.println("enter command: run / read / stop");
            String command = scanner.next();

            if (command.equals("stop")) {
                System.out.println("good bye");
                break;
            }

            if (command.equals("run")) {

                System.out.println("enter a file with an extension");
                String fileName = scanner.next();
                while (!(fileName.endsWith("json") ||( fileName.endsWith("csv"))) ) {
                    System.out.println("incorrect extension. try again");
                    fileName = scanner.next();
                }
                if (fileName.endsWith("csv")) {
                    storageService = new CsvStorageService();
                }

                System.out.println("append? true/false");
                String appendStr = scanner.next();
                while (!(appendStr.equals("true") || appendStr.equals("false"))) {
                    System.out.println("you need to write false or true. try again");
                    appendStr = scanner.next();
                }
                boolean append = Boolean.parseBoolean(appendStr);

                System.out.print("Enter max number of parallel API requests (n): ");
                n = getPositiveInt(scanner);

                System.out.print("Enter polling interval in seconds (t): ");
                t = getPositiveInt(scanner);

                System.out.println("How many time you wanna wait?");
                int time = getPositiveInt(scanner);

                System.out.println("enter list of api. finish with 'exit'");
                List<String> apiNames = new ArrayList<>();

                String apiName = scanner.next();
                while (!apiName.equals("exit")) {
                    apiNames.add(apiName);
                    apiName = scanner.next();
                }
                try {
                    service.startPolling(n, t, apiNames, fileName, append, storageService);
                    Thread.sleep(time * 1000L);
                    service.stopPolling();
                    System.out.println("data saved");
                } catch (Exception e) {
                    System.err.println("Fatal error: " + e.getMessage());
                }

            }

            if (command.equals("read")) {

                System.out.println("enter a file with an extension");
                String fileName = scanner.next();
                while (!(fileName.endsWith("json") ||( fileName.endsWith("csv"))) ) {
                    System.out.println("incorrect extension. try again");
                    fileName = scanner.next();
                }

                System.out.println("read all or one api? all/one");
                String type = scanner.next();
                while (!(type.equals("all") || type.equals("one"))) {
                    System.out.println("you need to write 'all' or 'one'. try again");
                    type = scanner.next();
                }
                try {
                    if (type.equals("all")) {
                        printAllData(fileName);
                    } else {
                        System.out.println("write api name");
                        String api = scanner.next();
                        printDataApi(fileName, api);
                    }
                } catch (IOException e) {
                    System.err.println("Storage error: " + e.getMessage());
                    System.exit(1);
                }

            }
        }

    }


    private static void printAllData(String fileName) throws IOException {
        if (fileName.endsWith("json")) {
            JsonNode info = new JsonStorageService().readAll(fileName);
            try {
                System.out.println(
                        new ObjectMapper()
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(info)
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            printCsv(new CsvStorageService().readAll(fileName));
        }
    }

    private static void printDataApi(String fileName, String apiName) throws IOException  {
        if (fileName.endsWith("json")) {
            JsonNode info = new JsonStorageService().readByApi(apiName, fileName);
            try {
                System.out.println(
                        new ObjectMapper()
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(info)
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            printCsv(new CsvStorageService().readByApi(fileName, apiName));
        }

    }

    private static void printCsv(List<Map<String, String>> info) {
        if (info.isEmpty()) {
            System.out.println("CSV пустой");
            return;
        }

        Set<String> headers = info.get(0).keySet();

        for (String header : headers) {
            System.out.printf("%-20s", header);
        }
        System.out.println();

        for (int i = 0; i < headers.size(); i++) {
            System.out.print("--------------------");
        }
        System.out.println();

        for (Map<String, String> row : info) {
            for (String header : headers) {
                System.out.printf("%-20s", row.getOrDefault(header, ""));
            }
            System.out.println();
        }
    }

    private static int getPositiveInt(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("You need to enter a number. Try again:");
            scanner.next(); // убрать неправильный ввод
        }

        int n = scanner.nextInt();

        while (n <= 0) {
            System.out.println("Number must be greater than 0. Try again:");
            while (!scanner.hasNextInt()) {
                System.out.println("You need to enter a number. Try again:");
                scanner.next();
            }
            n = scanner.nextInt();
        }

        return n;
    }
}