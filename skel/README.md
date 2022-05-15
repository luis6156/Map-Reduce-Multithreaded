# Copyright Micu Florian-Luis 2021 - Second Assignment APD

## Multithreading Algorithm
For this assignment I chose to use the "Replicated Workers" design pattern as
it allows me to use threads asynchronously. Moreover, I used the "ExecutorService"
class as the class "ForkJoinPool" is better used for recursive algorithms. 
Even more, since each thread needs to return values, I used the "Future" 
interface as it guarantees that the result will be pulled from the thread 
only after the thread finished its job. To start the threads, I used the 
"invokeAll" method as it starts all tasks assigned to the ExecutorService 
while it also guarantees that each thread finished its job before the "shutdown" 
method is called.

Furthermore, I will discuss the implementation of the Map and Reduce operations
as they are the most complex algorithms in this assignment.

## Map
### Class Attributes
Input:
- File name 
- Fragment size
- Offset

Output (MapResult format):
- File name
- HashMap (length-to-frequency)
- List of words with maximum length 

A map operation consists of a thread reading a segment of text from a file, 
counting the number of words in that segment that have the same length. Due to
the fact that the segment might start or end inside of a word, the algorithm 
first reads a letter backwards (only if the segment is not the start of the 
file) and checks if it is a valid letter. If it is, then the algorithm skips 
all valid letters until an invalid letter is reached. Moreover, after the 
required number of letters is read, the algorithm checks if the next letter 
is valid, and if it is, continues to read until an invalid letter is reached.
This respects the assignment rules that a split word found at the end of the 
sequence must be read completely while a split word found at the beginning of 
the sequence must be skipped (however the characters read counter must be 
decreased accordingly). Furthermore, the maximum length is computed since we 
need to keep track of the words that have said maximum length. To do this, I 
implement a HashMap that associates length-to-frequency and I use another 
HashMap that associates length-to-words. After the required HashMaps are 
completed, I use another class "MapResult" to store the output and return it.

### Map Result processing
It is important after each thread completes its map operation that the main 
thread merges all the results so that each input file will only have one 
MapResult.

## Reduce
Input (MapResult format):
- File name
- HashMap (length-to-frequency)
- List of words with maximum length

Output (ReduceResult format):
- File name
- Rank
- Maximum word length
- Frequency of said maximum length

A reduce operation will use the assignment's formula to compute the rank of 
the file using the sequence of Fibonacci numbers. To compute the Nth 
Fibonacci number, I chose the math algorithm that computes the solution in O(1)
time and space complexity: ((1 + sqrt(5)) / 2)^N / sqrt(5). To return the 
required output, I use the class "ReduceResult" to store the required attributes
and I return the newly created instance.

### Reduce Result processing
It is important after each thread completes its reduce operation that the main
thread sorts the results by rank first and then by the order in which the 
file names have been read (for this I use a HashMap to associate each file 
name to a number that corresponds to its order, thus I have O(1) time 
complexity for searching).

### Notes
Used BufferedReader and StringBuilder to increase time performance, and I 
used Java8 streams to make the code more modular and possibly faster. 

The bonus was not implemented as it was obscure to me how a generic 
MapReduce should look. I would recommend in the future that you would give 
some examples, other than that the assignment was enjoyable to complete.
