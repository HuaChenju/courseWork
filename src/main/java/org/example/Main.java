package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.argregationservice.AgregationService;
import org.example.storageservice.CsvStorageService;
import org.example.storageservice.JsonStorageService;
import org.example.storageservice.StorageService;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        run(args, new AgregationService());
    }

    static void run(String[] args, AgregationService service) {
        if (args.length < 1) {
            throw new IllegalArgumentException("not enough args");
        }
        String typeWork = args[0];
        if (typeWork.equals("automatice")) {
            if (args.length < 3) {
                throw new IllegalArgumentException("not enough args");
            }
            automaticeWork(args, service);
        } else if (typeWork.equals("interactive")) {
            interactiveWork(service);
        } else {
            throw new IllegalArgumentException("error: unknown work type");
        }
    }

    static void automaticeWork(String[] args, AgregationService service) {
        StorageService storageService = new JsonStorageService();

        try {
            Map<String, String> params = parseArgs(args);

            String fileName = "outputAutomatice." + params.get("fileType");
            int n = Integer.parseInt(params.get("n"));
            int t = Integer.parseInt(params.get("t"));
            List<String> apiNames = List.of(params.get("apiNames").split(" "));

            if (Objects.equals(params.get("fileType"), "csv")) {
                storageService = new CsvStorageService();
            }

            try {

                service.startPolling(n, t, apiNames, fileName, false, storageService);
                Thread.sleep(t * 2000L);
                service.stopPolling();

            } catch (Exception e) {
                System.err.println("Fatal error: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }

    }


    static Map<String, String> parseArgs(String[] args) throws IllegalArgumentException {
        Map<String, String> result = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "json" -> result.put("fileType", "json");
                case "csv" -> result.put("fileType", "csv");
                default -> {
                    if (args[i].startsWith("n=")) {
                        result.put("n", args[i].substring(2));
                    } else if (args[i].startsWith("t=")) {
                        result.put("t", args[i].substring(2));
                    } else {
                        StringBuilder sb = new StringBuilder(result.getOrDefault("apiNames", ""));
                        sb.append(args[i]).append(" ");
                        result.put("apiNames", sb.toString());
                    }
                }
            }
        }
        if (result.containsKey("apiNames") && result.containsKey("n") && result.containsKey("t") && result.containsKey("fileType")) {
            return result;
        }
        throw new IllegalArgumentException("not enough args");
    }

    public static void interactiveWork(AgregationService service) {


        Scanner scanner = new Scanner(System.in);
        StorageService storageService = new JsonStorageService();
        int n = 1, t = 10;

        while (true) {

            System.out.println("enter command: run / read / stop");
            String command = scanner.next();

            if (command.equals("stop")) {
                System.out.println("good bye");
                break;
            }

            if (command.equals("run")) {
                Map<String, String> tools = parseCLIRun(scanner);
                String fileName = tools.get("fileName");
                if (fileName.endsWith("csv")) {
                    storageService = new CsvStorageService();
                }
                boolean append = Boolean.parseBoolean(tools.get("append"));
                n = Integer.parseInt(tools.get("n"));
                t = Integer.parseInt(tools.get("t"));
                int time = Integer.parseInt(tools.get("time"));
                List<String> apiNames = List.of(tools.get("apiNames").split(" "));

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
                Map<String, String> tools = paeseCLIRead(scanner);

                String fileName = tools.get("fileName");
                String type = tools.get("type");

                try {
                    if (type.equals("all")) {
                        printAllData(fileName, System.out);
                    } else {
                        String api = tools.get("apiName");
                        printDataApi(fileName, api, System.out);
                    }
                } catch (IOException e) {
                    System.err.println("Storage error: " + e.getMessage());
                    System.exit(1);
                }
            }
        }
    }

    static Map<String, String> parseCLIRun(Scanner scanner) {
        System.out.println("enter a file with an extension");
        Map<String, String> result = new HashMap<>();
        String fileName = scanner.next();
        while (!(fileName.endsWith("json") ||( fileName.endsWith("csv"))) ) {
            System.out.println("incorrect extension. try again");
            fileName = scanner.next();
        }

        result.put("fileName", fileName);

        System.out.println("append? true/false");
        String appendStr = scanner.next();
        while (!(appendStr.equals("true") || appendStr.equals("false"))) {
            System.out.println("you need to write false or true. try again");
            appendStr = scanner.next();
        }
        result.put("append", appendStr);

        System.out.print("Enter max number of parallel API requests (n): ");
        result.put("n", String.valueOf(getPositiveInt(scanner)));

        System.out.print("Enter polling interval in seconds (t): ");
        result.put("t", String.valueOf(getPositiveInt(scanner)));

        System.out.println("How many time you wanna wait?");
        result.put("time", String.valueOf(getPositiveInt(scanner)));

        System.out.println("enter list of api. finish with 'exit'");
        StringBuilder apiNames = new StringBuilder();

        String apiName = scanner.next();
        while (!apiName.equals("exit")) {
            apiNames.append(apiName).append(" ");
            apiName = scanner.next();
        }

        result.put("apiNames", apiNames.toString());
        return result;
    }

    static Map<String, String> paeseCLIRead(Scanner scanner) {
        Map<String, String> result = new HashMap<>();
        System.out.println("enter a file with an extension");
        String fileName = scanner.next();
        while (!(fileName.endsWith("json") ||( fileName.endsWith("csv"))) ) {
            System.out.println("incorrect extension. try again");
            fileName = scanner.next();
        }
        result.put("fileName", fileName);
        System.out.println("read all or one api? all/one");
        String type = scanner.next();
        while (!(type.equals("all") || type.equals("one"))) {
            System.out.println("you need to write 'all' or 'one'. try again");
            type = scanner.next();
        }
        result.put("type", type);

        if (type.equals("one")) {
            System.out.println("write api name");
            result.put("apiName", scanner.next());
        }

        return result;
    }

    static void printAllData(String fileName, PrintStream out) throws IOException {
        if (fileName.endsWith("json")) {
            JsonNode info = new JsonStorageService().readAll(fileName);
            try {
                out.println(
                        new ObjectMapper()
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(info)
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            printCsv(new CsvStorageService().readAll(fileName), out);
        }
    }

    static void printDataApi(String fileName, String apiName, PrintStream out) throws IOException  {
        if (fileName.endsWith("json")) {
            JsonNode info = new JsonStorageService().readByApi(apiName, fileName);
            try {
                out.println(
                        new ObjectMapper()
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(info)
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            printCsv(new CsvStorageService().readByApi(fileName, apiName), out);
        }

    }

    static void printCsv(List<Map<String, String>> info, PrintStream out) {
        if (info.isEmpty()) {
            out.println("CSV пустой");
            return;
        }

        Set<String> headers = info.getFirst().keySet();

        for (String header : headers) {
            out.printf("%-20s", header);
        }
        System.out.println();

        for (int i = 0; i < headers.size(); i++) {
            out.print("--------------------");
        }
        out.println();

        for (Map<String, String> row : info) {
            for (String header : headers) {
                out.printf("%-20s", row.getOrDefault(header, ""));
            }
            out.println();
        }
    }

    static int getPositiveInt(Scanner scanner) {
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