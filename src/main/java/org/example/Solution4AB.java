package org.example;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public class Solution4AB {

    private static final long BROADCAST_0x01 = 0x0101010101010101L;
    private static final long BROADCAST_0x80 = 0x8080808080808080L;
    private static final long BROADCAST_NEWLINE = 0x0A0A0A0A0A0A0A0AL;
    private static final long BROADCAST_SEMICOLON = 0x3B3B3B3B3B3B3B3BL;

    public static void processFile(String inputFile, String outputFile, BitSet winningNumbers) {
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
            final var mappedFile = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, length, Arena.global());
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
        private final BitSet winningNumbers;

        ChunkProcessor(MemorySegment chunk, Set<String> winners, BitSet winningNumbers) {
            this.chunk = chunk;
            this.winners = winners;
            this.winningNumbers = winningNumbers;
        }

        private static long newlineMatchBits(long word) {
            long diff = word ^ BROADCAST_NEWLINE;
            return (diff - BROADCAST_0x01) & (~diff & BROADCAST_0x80);
        }

        private static int nameLen(long separator) {
            return (Long.numberOfTrailingZeros(separator) >>> 3) + 1;
        }

        private static long semicolonMatchBits(long word) {
            long diff = word ^ BROADCAST_SEMICOLON;
            return (diff - BROADCAST_0x01) & (~diff & BROADCAST_0x80);
        }

        private long findByte(long cursor, int b) {
            for (var i = cursor; i < chunk.byteSize(); i++) {
                if (chunk.get(JAVA_BYTE, i) == b) {
                    return i;
                }
            }
            throw new RuntimeException(((char) b) + " not found");
        }


        //        read as number, no longer read as strings
        private Integer numberAt(long start, long limit) {
            var size = limit - start;
            int result = 0;
            for (int i = 0; i < size; i++) {
                result = ((result << 8) | (chunk.get(JAVA_BYTE, start + i)));
            }
            return result;
        }

        private long findNewLine(long cursor) {
            int nameLen = 0;
            while (true) {
                long wordByte = chunk.get(JAVA_LONG, cursor + nameLen);
                long matchBits = newlineMatchBits(wordByte);
                if (matchBits != 0) {
                    return cursor + nameLen + nameLen(matchBits);
                }
                nameLen += Long.BYTES;
            }
        }

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
                int nameLen = 0;
                //reading name of player
                while (true) {
                    //read 8 rather than 1 byte
                    long wordByte = chunk.get(JAVA_LONG, cursor + nameLen);
                    //get position of all found semicolons in that word
                    long matchBits = semicolonMatchBits(wordByte);
                    if (matchBits != 0) {
                        int firstSemicolonPos = Long.numberOfTrailingZeros(matchBits);
                        long newlinePos;
                        var secondSemicolonPos = findByte(firstSemicolonPos + 1, ';');
                        var num1 = numberAt(firstSemicolonPos + 1, secondSemicolonPos);
                        // instead of checking with contains.
                        if (!winningNumbers.get(num1)) {
                            newlinePos = findByte(secondSemicolonPos + 1, '\n');
                            cursor = newlinePos + 1;
                            break;
                        }
                        var thirdSemicolonPos = findByte(secondSemicolonPos + 1, ';');
                        var num2 = numberAt(secondSemicolonPos + 1, thirdSemicolonPos);
                        if (!winningNumbers.get(num2)) {
                            newlinePos = findByte(thirdSemicolonPos + 1, '\n');
                            cursor = newlinePos + 1;
                            break;
                        }
                        var fourthSemicolonPos = findByte(thirdSemicolonPos + 1, ';');
                        var num3 = numberAt(thirdSemicolonPos + 1, fourthSemicolonPos);
                        if (!winningNumbers.get(num3)) {
                            newlinePos = findByte(fourthSemicolonPos + 1, '\n');
                            cursor = newlinePos + 1;
                            break;
                        }
                        var fifthSemicolonPos = findByte(fourthSemicolonPos + 1, ';');
                        var num4 = numberAt(fourthSemicolonPos + 1, fifthSemicolonPos);
                        if (!winningNumbers.get(num4)) {
                            newlinePos = findByte(fifthSemicolonPos + 1, '\n');
                            cursor = newlinePos + 1;
                            break;
                        }

                        var sixthSemicolonPos = findByte(fifthSemicolonPos + 1, ';');
                        var num5 = numberAt(fifthSemicolonPos + 1, sixthSemicolonPos);
                        newlinePos = findByte(sixthSemicolonPos + 1, '\n');
                        if (!winningNumbers.get(num5)) {
                            cursor = newlinePos + 1;
                            break;
                        }
                        var num6 = numberAt(sixthSemicolonPos + 1, newlinePos);
                        if (!winningNumbers.get(num6)) {
                            cursor = newlinePos + 1;
                            break;
                        }
                        winners.add(stringAt(cursor, firstSemicolonPos));
                        cursor = newlinePos + 1;
                        break;
                    }
                    nameLen += Long.BYTES;
                }
            }
        }
    }

}
