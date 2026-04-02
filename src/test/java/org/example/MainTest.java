package org.example;

import org.example.argregationservice.AgregationService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MainTest {
    @Test
    void testGetPositiveInt() {
        Scanner scanner = new Scanner("abc -1 5");

        int result = Main.getPositiveInt(scanner);

        assertEquals(5, result);
    }

    @Test
    void parseCorrectArgs() {
        String[] args = {"automatice", "json", "jikan", "github",  "n=3", "t=2"};
        Map<String, String> map = Main.parseArgs(args);
        assertEquals("json", map.get("fileType"));
        assertEquals("jikan github ", map.get("apiNames"));
        assertEquals("3", map.get("n"));
        assertEquals("2", map.get("t"));
    }

    @Test
    void parseInCorrectArgs() {
        String[] args = {"automatice", "json", "jikan", "github", "t=2"};
        assertThrows(IllegalArgumentException.class, () -> Main.parseArgs(args));
    }

    @Test
    void automaticeWork_doesNotCrash() {
        String[] args = {"automatice", "json", "n=1", "t=1", "api1"};

        AgregationService service = mock(AgregationService.class);

        assertDoesNotThrow(() -> Main.automaticeWork(args, service));
    }

    @Test
    void testParseCLIRun_validInput() {
        String input = """
        data.json
        true
        5
        10
        20
        api1 api2 exit
        """;

        Scanner scanner = new Scanner(input);

        Map<String, String> result = Main.parseCLIRun(scanner);

        assertEquals("data.json", result.get("fileName"));
        assertEquals("true", result.get("append"));
        assertEquals("5", result.get("n"));
        assertEquals("10", result.get("t"));
        assertEquals("20", result.get("time"));
        assertEquals("api1 api2 ", result.get("apiNames"));
        assertEquals(6, result.size());
    }

    @Test
    void testParseCLIRun_InvalidInput() {
        String input = """
        meme
        data.json
        pu
        true
        -1
        -1
        5
        -1
        10
        -1
        20
        api1 api2 exit
        """;

        Scanner scanner = new Scanner(input);

        Map<String, String> result = Main.parseCLIRun(scanner);

        assertEquals("data.json", result.get("fileName"));
        assertEquals("true", result.get("append"));
        assertEquals("5", result.get("n"));
        assertEquals("10", result.get("t"));
        assertEquals("20", result.get("time"));
        assertEquals("api1 api2 ", result.get("apiNames"));
        assertEquals(6, result.size());
    }

    @Test
    void testParseCLIRead_AllValidInput() {
        String input = """
        data.json
        all
        """;

        Scanner scanner = new Scanner(input);

        Map<String, String> result = Main.paeseCLIRead(scanner);

        assertEquals("data.json", result.get("fileName"));
        assertEquals("all", result.get("type"));
        assertEquals(2, result.size());
    }

    @Test
    void testParseCLIRead_AllInValidInput() {
        String input = """
        data.json
        source
        all
        sas
        """;

        Scanner scanner = new Scanner(input);

        Map<String, String> result = Main.paeseCLIRead(scanner);

        assertEquals("data.json", result.get("fileName"));
        assertEquals("all", result.get("type"));
        assertEquals(2, result.size());
    }

    @Test
    void testParseCLIRead_OneValidInput() {
        String input = """
        data.json
        one
        source
        """;

        Scanner scanner = new Scanner(input);

        Map<String, String> result = Main.paeseCLIRead(scanner);

        assertEquals("data.json", result.get("fileName"));
        assertEquals("one", result.get("type"));
        assertEquals("source", result.get("apiName"));
        assertEquals(3, result.size());
    }

    @Test
    void testParseCLIRead_OneInValidInput() {
        String input = """
        llala
        data.json
        pupu
        one
        source
        sdf
        """;

        Scanner scanner = new Scanner(input);

        Map<String, String> result = Main.paeseCLIRead(scanner);

        assertEquals("data.json", result.get("fileName"));
        assertEquals("one", result.get("type"));
        assertEquals("source", result.get("apiName"));
        assertEquals(3, result.size());
    }

    @Test
    void run_notEnoughArgsShouldThrow() {
        String[] args = {};
        assertThrows(IllegalArgumentException.class, () -> Main.run(args, new AgregationService()));
    }

    @Test
    void run_notEnoughArgsShouldThrowAutomatice() {
        String[] args = {"automatice"};
        assertThrows(IllegalArgumentException.class, () -> Main.run(args, new AgregationService()));
    }

    @Test
    void run_illefalArgsShouldThrow() {
        String[] args = {"smth"};
        assertThrows(IllegalArgumentException.class, () -> Main.run(args, new AgregationService()));
    }

    @Test
    void testPrintCsv() {
        List<Map<String, String>> data = new ArrayList<>();

        Map<String, String> row = new LinkedHashMap<>();
        row.put("name", "Alice");
        row.put("age", "25");

        data.add(row);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);

        Main.printCsv(data, out);

        String result = baos.toString();

        assertTrue(result.contains("name"));
        assertTrue(result.contains("age"));
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("25"));
    }

}