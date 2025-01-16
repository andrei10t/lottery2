package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//60sec -> playing with regex didn't reduce it
public class Solution2 {
  public static void processFile(String inputFile, String outputFile, Set<Integer> winningNumbers) {
    try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile))) {
      Set<String> winners = new HashSet<>();
      reader.lines()
          .parallel()
          .map(line -> line.split(";"))
          .filter(parts -> parts.length > 1)
          .forEach(parts -> {
            String name = parts[0];
            Set<Integer> numbers = Arrays.stream(Arrays.copyOfRange(parts, 1, 7))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
            if (numbers.equals(winningNumbers)) {
              winners.add(name);
            }
          });
      if (winners.isEmpty()) {
        System.out.println("No winners found.");
      } else {
        Files.write(Paths.get(outputFile), winners);
      }
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
