package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Runs the official JSON Schema Test Suite (draft2020-12) against fastjson3's JSONSchema implementation.
 * Not a JUnit test — run as a main() to get a full compliance report.
 */
public class JSONSchemaTestSuiteRunner {
    static final Path SUITE_DIR = Paths.get("/root/git/JSON-Schema-Test-Suite/tests/draft2020-12");

    // Files that test features we explicitly don't implement
    static final Set<String> SKIP_FILES = Set.of(
            "vocabulary.json",      // $vocabulary not implemented
            "dynamicRef.json",      // $dynamicRef not implemented
            "refRemote.json",       // remote $ref not implemented
            "infinite-loop-detection.json", // requires remote ref infrastructure
            "format.json",          // format-as-annotation (not assertion) — different semantics
            "content.json",         // contentEncoding/contentMediaType not implemented
            "default.json"          // default keyword — not a validation keyword
    );

    public static void main(String[] args) throws IOException {
        int totalTests = 0;
        int passed = 0;
        int failed = 0;
        int skippedFiles = 0;
        int errors = 0;

        Map<String, int[]> fileResults = new LinkedHashMap<>();
        List<String> failures = new ArrayList<>();

        try (Stream<Path> files = Files.list(SUITE_DIR)) {
            List<Path> testFiles = files
                    .filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .toList();

            for (Path file : testFiles) {
                String fileName = file.getFileName().toString();
                if (SKIP_FILES.contains(fileName)) {
                    skippedFiles++;
                    continue;
                }

                String content = Files.readString(file);
                JSONArray groups = JSON.parseArray(content);

                int filePassed = 0;
                int fileFailed = 0;
                int fileErrors = 0;

                for (int g = 0; g < groups.size(); g++) {
                    JSONObject group = groups.getJSONObject(g);
                    String groupDesc = group.getString("description");
                    Object schemaObj = group.get("schema");
                    JSONArray tests = group.getJSONArray("tests");

                    JSONSchema schema;
                    try {
                        if (schemaObj instanceof Boolean b) {
                            schema = b ? Any.INSTANCE : Any.NOT_ANY;
                        } else {
                            schema = JSONSchema.of((JSONObject) schemaObj);
                        }
                    } catch (Exception e) {
                        // Schema parsing error — count all tests in this group as errors
                        for (int t = 0; t < tests.size(); t++) {
                            fileErrors++;
                            totalTests++;
                            String testDesc = tests.getJSONObject(t).getString("description");
                            failures.add(String.format("  ERROR [%s] %s / %s: %s",
                                    fileName, groupDesc, testDesc, e.getClass().getSimpleName() + ": " + e.getMessage()));
                        }
                        continue;
                    }

                    for (int t = 0; t < tests.size(); t++) {
                        JSONObject test = tests.getJSONObject(t);
                        String testDesc = test.getString("description");
                        Object data = test.get("data");
                        boolean expectedValid = test.getBooleanValue("valid");

                        totalTests++;

                        try {
                            boolean actualValid = schema.isValid(data);
                            if (actualValid == expectedValid) {
                                filePassed++;
                                passed++;
                            } else {
                                fileFailed++;
                                failed++;
                                failures.add(String.format("  FAIL [%s] %s / %s: expected %s, got %s",
                                        fileName, groupDesc, testDesc, expectedValid, actualValid));
                            }
                        } catch (Exception e) {
                            fileErrors++;
                            errors++;
                            failures.add(String.format("  ERROR [%s] %s / %s: %s",
                                    fileName, groupDesc, testDesc, e.getClass().getSimpleName() + ": " + e.getMessage()));
                        }
                    }
                }

                fileResults.put(fileName, new int[]{filePassed, fileFailed, fileErrors});
            }
        }

        // Print report
        System.out.println("=" .repeat(80));
        System.out.println("JSON Schema Test Suite — Draft 2020-12 — fastjson3 Results");
        System.out.println("=" .repeat(80));
        System.out.println();

        System.out.println("Per-file results:");
        System.out.printf("  %-35s %6s %6s %6s %6s%n", "File", "Pass", "Fail", "Error", "Total");
        System.out.println("  " + "-".repeat(71));
        for (Map.Entry<String, int[]> entry : fileResults.entrySet()) {
            int[] r = entry.getValue();
            int total = r[0] + r[1] + r[2];
            String status = (r[1] == 0 && r[2] == 0) ? "✓" : "✗";
            System.out.printf("  %s %-33s %6d %6d %6d %6d%n", status, entry.getKey(), r[0], r[1], r[2], total);
        }

        System.out.println();
        System.out.println("Skipped files (" + skippedFiles + "): " + SKIP_FILES);
        System.out.println();

        if (!failures.isEmpty()) {
            System.out.println("Failures and errors (" + (failed + errors) + "):");
            // Group and limit output
            int shown = 0;
            for (String f : failures) {
                System.out.println(f);
                shown++;
                if (shown >= 100) {
                    System.out.println("  ... and " + (failures.size() - shown) + " more");
                    break;
                }
            }
            System.out.println();
        }

        System.out.println("=" .repeat(80));
        System.out.printf("TOTAL: %d tests, %d passed, %d failed, %d errors (%.1f%% pass rate)%n",
                totalTests, passed, failed, errors,
                totalTests > 0 ? (passed * 100.0 / totalTests) : 0);
        System.out.printf("Skipped %d files (features not implemented)%n", skippedFiles);
        System.out.println("=" .repeat(80));
    }
}
