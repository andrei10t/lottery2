package org.example;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;


//17sec
public class Solution3C {

  public static void processFile(String inputFile, String outputFile, Set<String> winningNumbers) {
    final File file = new File(inputFile);
    final long length = file.length();
    final int chunkCount = Runtime.getRuntime().availableProcessors();
    Set<String> winners = new HashSet<>();
    final var chunkStartOffsets = new long[chunkCount];
    try (var raf = new RandomAccessFile(file, "r")) {
      for (int i = 1; i < chunkStartOffsets.length; i++) {
        var start = length * i / chunkStartOffsets.length;
        raf.seek(start);
        while (raf.read() != (byte) '\n') {
        }
        start = raf.getFilePointer();
        chunkStartOffsets[i] = start;
      }
      final var mappedFile = raf.getChannel().map(MapMode.READ_ONLY, 0, length, Arena.global());
      var threads = new Thread[chunkCount];
      for (int i = 0; i < chunkCount; i++) {
        final long chunkStart = chunkStartOffsets[i];
        final long chunkLimit = (i + 1 < chunkCount) ? chunkStartOffsets[i + 1] : length;
        threads[i] = new Thread(new ChunkProcessor(
                mappedFile.asSlice(chunkStart, chunkLimit - chunkStart), winners, winningNumbers));
      }
      for (var thread : threads) {
        thread.start();
      }
      for (var thread : threads) {
        thread.join();
      }
    } catch (IOException | InterruptedException e) {
      System.out.println(e.getMessage());
    }
    if (winners.isEmpty()) {
      System.out.println("No winners found.");
    } else {
      try {
        Files.write(Paths.get(outputFile), winners);
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  private static class ChunkProcessor implements Runnable {
    private final MemorySegment chunk;
    private final Set<String> winners;
    private final Set<String> winningNumbers;

    ChunkProcessor(MemorySegment chunk, Set<String> winners, Set<String> winningNumbers) {
      this.chunk = chunk;
      this.winners = winners;
      this.winningNumbers = winningNumbers;
    }

    private long findByte(long cursor, int b) {
      //not needing any check, for sure there is a ';'
      for (var i = cursor; ; i++) {
        if (chunk.get(JAVA_BYTE, i) == b) {
          return i;
        }
      }
    }

    //    Tried some other solutions for stringAt()
    private String stringAt(long start, long limit) {
      long size = limit - start;
      byte[] byteArray = new byte[(int) size];
      for (int i = 0; i < size; i++) {
        byteArray[i] = chunk.get(JAVA_BYTE, start + i);
      }
      return new String(byteArray, StandardCharsets.UTF_8);
    }

    @Override
    public void run() {
      for (var cursor = 0L; cursor < chunk.byteSize(); ) {
//        Ekkaldır Buse;4;18;48;43;36;19
        // We read a number(between last known ';' and the next one) and check if it's part of the winning combination
        // If not we go to the next line (without saving as strings next numbers and name)
        // If all 6 numbers are winning we read the name and save it.
        // This reduced StringAt() from 70% of total time to 22%. Compared with 3A

        // I feel some optimization can be made with finding ';', we know numbers are one or two digits
        // and no one digit number will start with >4
        var firstSemicolonPos = findByte(cursor, ';');
        long newlinePos;
        var secondSemicolonPos = findByte(firstSemicolonPos + 1, ';');
        var num1 = stringAt(firstSemicolonPos + 1, secondSemicolonPos);
        if (!winningNumbers.contains(num1)) {
          newlinePos = findByte(secondSemicolonPos + 1, '\n');
          cursor = newlinePos + 1;
          continue;
        }
        var thirdSemicolonPos = findByte(secondSemicolonPos + 1, ';');
        var num2 = stringAt(secondSemicolonPos + 1, thirdSemicolonPos);
        if (!winningNumbers.contains(num2)) {
          newlinePos = findByte(thirdSemicolonPos + 1, '\n');
          cursor = newlinePos + 1;
          continue;
        }
        var fourthSemicolonPos = findByte(thirdSemicolonPos + 1, ';');
        var num3 = stringAt(thirdSemicolonPos + 1, fourthSemicolonPos);
        if (!winningNumbers.contains(num3)) {
          newlinePos = findByte(fourthSemicolonPos + 1, '\n');
          cursor = newlinePos + 1;
          continue;
        }
        var fifthSemicolonPos = findByte(fourthSemicolonPos + 1, ';');
        var num4 = stringAt(fourthSemicolonPos + 1, fifthSemicolonPos);
        if (!winningNumbers.contains(num4)) {
          newlinePos = findByte(fifthSemicolonPos + 1, '\n');
          cursor = newlinePos + 1;
          continue;
        }

        var sixthSemicolonPos = findByte(fifthSemicolonPos + 1, ';');
        var num5 = stringAt(fifthSemicolonPos + 1, sixthSemicolonPos);
        newlinePos = findByte(sixthSemicolonPos + 1, '\n');
        if (!winningNumbers.contains(num5)) {
          cursor = newlinePos + 1;
          continue;
        }
        var num6 = stringAt(sixthSemicolonPos + 1, newlinePos);
        if (!winningNumbers.contains(num6)) {
          cursor = newlinePos + 1;
          continue;
        }
        winners.add(stringAt(cursor, firstSemicolonPos));
        cursor = newlinePos + 1;
      }
    }
  }
}