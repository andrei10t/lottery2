### Options
```
--enable-preview -XX:ActiveProcessorCount=8
```
--enable-preview needs to be set also in settings>java compiler. 

### Arguments
```
<relative_input_file> <relative_output_file> num1 num2 num3 num4 num5 num6
```
```
src/main/resources/pool.csv src/main/resources/output.txt 18 25 48 12 2 16
```
3C is the best solution.  Calling it here in Main
```
Solution3C.processFile(inputFile, outputFile, winningNumbers);
```
### What I do
I'm splitting the input in chunks equal to my threads(could be lower-sized chunks in case one thread finishes earlier but my bottleneck is not there yet).
Right now the bottleneck is at memorysegment.get() -> 79% of total according to the profiler, which using it is a new thing for me. 


In run(), biggest improvement:

    We read a number(between last known ';' and the next one) and check if it's part of the winning combination.
    
    If not we go to the next line (without saving as strings next numbers and name).
    
    If all 6 numbers are winning we read the name and save it.
    This reduced StringAt() from 70% of total time to 22%( Compared with 3A)
    
    I feel some optimization can be made with finding ';', we know numbers are one or two digits
    and no one-digit number will start with >4

I tried switching to 23.0 graalvm from oracle but it was twice as slow(34sec) as java-21-temurin(17sec)

I found other ideas for small improvments like:
1. Use sun.misc.Unsafe instead of MemorySegment to avoid bounds checks
2. Process the data 8 bytes at a time, using a SWAR technique to find the semicolon

