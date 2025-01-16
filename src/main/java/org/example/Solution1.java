package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Solution1 {

  //117 sec
  public static void processFile(String inputFile, String outputFile, Set<Integer> winningNumbers) {
    try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
      String line;
      Set<String> winners = new HashSet<>();

      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(";");
        // we assume data is clean, no need to check names+numbers split
        String name = parts[0];
        String[] numbersStr = Arrays.copyOfRange(parts, 1, parts.length);
        // we assume data is clean, no need to check numbers count
        try {
          Set<Integer> numbers = Arrays.stream(numbersStr)
              .map(Integer::parseInt)
              .collect(Collectors.toSet());
          if (numbers.equals(winningNumbers)) {
            winners.add(name);
          }
        } catch (NumberFormatException e) {
          // we assume data is clean
        }
      }
      if (winners.isEmpty()) {
        System.out.println("No winners found.");
      } else {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        for (String winner : winners) {
          writer.write(winner);
          writer.newLine();
        }
        writer.close();
      }
    } catch (FileNotFoundException e) {
      System.out.println("The file " + inputFile + " was not found.");
    } catch (IOException e) {
      System.out.println("An error occurred while reading the file.");
    }
  }

}
