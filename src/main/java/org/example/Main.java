package org.example;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class Main {

  // I Tried using graalvm jdk 23.0.1 from oracle
  public static void main(String[] args) {
    String inputFile = args[0];
    String outputFile = args[1];
    Set<String> winningNumbers = validateInput(Arrays.copyOfRange(args, 2, args.length));

    long startTime = System.currentTimeMillis();
//    Solution3C.processFile(inputFile, outputFile, winningNumbers);
    Solution4A.processFile(inputFile, outputFile, stringToBitSet(winningNumbers));
    long endTime = System.currentTimeMillis();
    long durationInMillis = endTime - startTime;
    System.out.println("Elapsed time: " + durationInMillis + " ms");
  }

  private static Set<String> validateInput(String[] inputNumbers) {
    Set<String> validNumbers = new HashSet<>();

    try {
      for (String inputNumber : inputNumbers) {
        if (!inputNumber.matches("\\d+")) {
          throw new IllegalArgumentException("Only numbers are allowed.");
        }
        int number = Integer.parseInt(inputNumber);

        if (number < 1 || number > 49) {
          throw new IllegalArgumentException("Numbers must be between 1 and 49.");
        }

        if (!validNumbers.add(inputNumber)) {
          throw new IllegalArgumentException("Duplicate numbers are not allowed.");
        }
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Only numbers are allowed.");
    }
    return validNumbers;
  }

  private static BitSet stringToBitSet(Set<String> numbers) {
    BitSet bitSet = new BitSet(64);
    for (String numberStr : numbers) {
      int number = Integer.parseInt(numberStr);
      bitSet.set(number);
    }
    return bitSet;
  }
}