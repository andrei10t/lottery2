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

//29238 ms
public class Solution3 {

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
    //    private final int myIndex;
    private final Set<String> winningNumbers;
//    private final Map<String, StationStats> statsMap = new HashMap<>();

    ChunkProcessor(MemorySegment chunk, Set<String> winners, Set<String> winningNumbers) {
      this.chunk = chunk;
      this.winners = winners;
//      this.myIndex = myIndex;
      this.winningNumbers = winningNumbers;
    }

    private long findByte(long cursor, int b) {
      for (var i = cursor; i < chunk.byteSize(); i++) {
        if (chunk.get(JAVA_BYTE, i) == b) {
          return i;
        }
      }
      throw new RuntimeException(((char) b) + " not found");
    }

    private String stringAt(long start, long limit) {
      return StandardCharsets.UTF_8.decode(chunk.asSlice(start, limit - start).asByteBuffer()).toString();
    }

    @Override
    public void run() {
      for (var cursor = 0L; cursor < chunk.byteSize(); ) {
        //TODO ""
//        Ekkaldır Buse;4;18;48;43;36;19
        var firstSemicolonPos = findByte(cursor, ';');
        var name = stringAt(cursor, firstSemicolonPos);

        var secondSemicolonPos = findByte(firstSemicolonPos + 1, ';');
        var num1 = stringAt(firstSemicolonPos + 1, secondSemicolonPos);

        var thirdSemicolonPos = findByte(secondSemicolonPos + 1, ';');
        var num2 = stringAt(secondSemicolonPos + 1, thirdSemicolonPos);

        var fourthSemicolonPos = findByte(thirdSemicolonPos + 1, ';');
        var num3 = stringAt(thirdSemicolonPos + 1, fourthSemicolonPos);

        var fifthSemicolonPos = findByte(fourthSemicolonPos + 1, ';');
        var num4 = stringAt(fourthSemicolonPos + 1, fifthSemicolonPos);

        var sixthSemicolonPos = findByte(fifthSemicolonPos + 1, ';');
        var num5 = stringAt(fifthSemicolonPos + 1, sixthSemicolonPos);

        var newlinePos = findByte(sixthSemicolonPos + 1, '\n');
        var num6 = stringAt(sixthSemicolonPos + 1, newlinePos);

        cursor = newlinePos + 1;
        if (winningNumbers.contains(num1) && winningNumbers.contains(num2) && winningNumbers.contains(num3) &&
            winningNumbers.contains(num4) && winningNumbers.contains(num5) && winningNumbers.contains(num6)) {
          winners.add(name);
        }
      }
    }
  }
}