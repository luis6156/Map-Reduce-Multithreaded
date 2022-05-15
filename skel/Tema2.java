import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class Tema2 {
    private static int fragmentSize, numberOfFiles, numberOfWorkers;
    private static final HashMap<String, Integer> inputFilesToPosition =
            new HashMap<>();

    /**
     * Read input data from file and arguments
     *
     * @param args arguments to be processed
     */
    private static int readInput(String[] args) {
        numberOfWorkers = Integer.parseInt(args[0]);

        // Read file (try-with-resources)
        try (BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
            // Read line by line
            String line = br.readLine();
            // Read fragment size
            if (line != null) {
                fragmentSize = Integer.parseInt(line);
            } else {
                System.err.println("Input file does not have the allowed format.");
                return -1;
            }

            line = br.readLine();
            // Read number of files
            if (line != null) {
                numberOfFiles = Integer.parseInt(line);
            } else {
                System.err.println("Input file does not have the allowed format.");
                return -1;
            }

            line = br.readLine();
            int position = 0;
            // Read files names
            while (line != null) {
                inputFilesToPosition.put(line, position++);
                line = br.readLine();
            }

            return 0;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return -1;
        }
    }

    /**
     * Creates Map tasks, executes them and merges the list of MapResults
     * before returning them
     *
     * @param tpe ExecutorService instance
     * @return HashMap of filenames to MapResult or null if an error has been
     * thrown
     */
    public static HashMap<String, ArrayList<MapResult>> executeMap(ExecutorService tpe) {
        HashMap<String, ArrayList<MapResult>> fileToMaps = new HashMap<>();
        List<Map> tasks = new ArrayList<>();

        // Create Map tasks
        for (HashMap.Entry<String, Integer> filename : inputFilesToPosition.entrySet()) {
            // Compute number of fragments
            int fileSize = 0;
            try {
                fileSize = (int) Files.size(Paths.get(filename.getKey()));
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error could not get file size");
                return null;
            }
            int numberOfFragments = (int) Math.ceil((float) fileSize / fragmentSize);
            // Create Map tasks with the correct offset and fragment size
            for (int j = 0; j < numberOfFragments; ++j) {
                tasks.add(new Map(filename.getKey(), j * fragmentSize, fragmentSize));
            }
        }

        // Start Map tasks and get their result
        List<Future<MapResult>> futureMapResults;
        try {
            futureMapResults = tpe.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error ExecutorService for Map tasks " +
                    "failed when calling the \"invokeAll\" method");
            return null;
        }

        // Process Map Operation's results
        for (Future<MapResult> futureMapResult : futureMapResults) {
            MapResult mapResult = null;
            try {
                // Get map result
                mapResult = futureMapResult.get();
                // Check if the result is not empty (special case where the
                // thread had nothing to parse)
                if (mapResult != null) {
                    // Merge results for a particular file into one map
                    // (fileToMaps)
                    if (fileToMaps.containsKey(mapResult.getFilename())) {
                        ArrayList<MapResult> prevMapResults = fileToMaps.get(mapResult.getFilename());
                        prevMapResults.add(mapResult);
                        fileToMaps.put(mapResult.getFilename(), prevMapResults);
                    } else {
                        ArrayList<MapResult> mapResults = new ArrayList<>();
                        mapResults.add(mapResult);
                        fileToMaps.put(mapResult.getFilename(), mapResults);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.err.println("Error Future for Map tasks " +
                        "failed when calling the \"get\" method");
                return null;
            }
        }

        return fileToMaps;
    }

    /**
     * Creates Reduce tasks, executes them and orders the list of ReduceResults
     * before returning them
     *
     * @param tpe ExecutorService instance
     * @param fileToMaps result of Map operation
     * @return list of Reduce or null if an error has been thrown
     */
    public static List<ReduceResult> executeReduce(ExecutorService tpe, HashMap<String,
            ArrayList<MapResult>> fileToMaps) {
        List<Reduce> tasksReduce = new ArrayList<>(numberOfFiles);

        // Create Reduce tasks
        for (HashMap.Entry<String, Integer> filename : inputFilesToPosition.entrySet()) {
            tasksReduce.add(new Reduce(filename.getKey(), fileToMaps.get(filename.getKey())));
        }

        List<Future<ReduceResult>> futureReduceResults;
        // Start Reduce tasks and get their result
        try {
            futureReduceResults = tpe.invokeAll(tasksReduce);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error ExecutorService for Reduce tasks " +
                    "failed when calling the \"invokeAll\" method");
            return null;
        }

        List<ReduceResult> reduceResults = new ArrayList<>();
        // Process Reduce Operation's results
        for (Future<ReduceResult> futureReduceResult : futureReduceResults) {
            try {
                reduceResults.add(futureReduceResult.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.err.println("Error Future for Reduce tasks " +
                        "failed when calling the \"get\" method");
                return null;
            }
        }

        // Sort results (rank and then by order of file insertion)
        reduceResults.sort((o1, o2) -> {
            if (o1.getRank() == o2.getRank()) {
                return Integer.compare(inputFilesToPosition.get(o1.getFilename()), inputFilesToPosition.get(o2.getFilename()));
            }
            return -Double.compare(o1.getRank(), o2.getRank());
        });

        return reduceResults;
    }

    /**
     * Writes solution to file
     *
     * @param outputFile name of the output file
     * @param reduceResults list of results ordered
     * @throws IOException in case of IO error
     */
    private static void writeSolution(String outputFile,
                                      List<ReduceResult> reduceResults) throws IOException {
        FileWriter writer = new FileWriter(outputFile);
        for (ReduceResult reduceResult : reduceResults) {
            writer.write(reduceResult.getFilename().substring(reduceResult.getFilename().lastIndexOf("/") + 1) + "," + String.format("%.2f", reduceResult.getRank()) + "," + reduceResult.getMaxLength() + "," + reduceResult.getFrequency() + '\n');
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        HashMap<String, ArrayList<MapResult>> fileToMaps;
        List<ReduceResult> reduceResults;

        // Verify that all arguments are present
        if (args.length < 3) {
            System.err.println("Usage: Tema2 <workers> <in_file> <out_file>");
            return;
        }

        if (readInput(args) == -1) {
            System.err.println("Error reading file");
            return;
        }

        // Executor Service
        ExecutorService tpe = Executors.newFixedThreadPool(numberOfWorkers);

        // Execute Map Operation
        fileToMaps = executeMap(tpe);
        if (fileToMaps == null) {
            tpe.shutdownNow();
            return;
        }

        // Execute Reduce Operation
        reduceResults = executeReduce(tpe, fileToMaps);
        if (reduceResults == null) {
            tpe.shutdownNow();
            return;
        }

        // Stop ExecutorService
        tpe.shutdown();

        // Write solution to file
        try {
            writeSolution(args[2], reduceResults);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing to output file");
        }
    }
}
