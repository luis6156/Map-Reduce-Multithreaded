import java.util.*;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Reduce operation for a given file -> return ReduceResult
 */
public class Reduce implements Callable<ReduceResult> {
    private final String filename;
    private final List<MapResult> mapResults;

    public Reduce(String filename, List<MapResult> mapResults) {
        this.filename = filename;
        this.mapResults = mapResults;
    }

    /**
     * Computes Nth Fibonacci number using a math formula
     *
     * @param N element from the Fibonacci sequence
     * @return corresponding number
     */
    private double getNthFib(int N) {
        return Math.round(Math.pow(((Math.sqrt(5) + 1) / 2), N) / Math.sqrt(5));
    }

    @Override
    public ReduceResult call() throws Exception {
        HashMap<Integer, Integer> lengthToFrequency = new HashMap<>();
        ArrayList<String> words = new ArrayList<>();
        double rank = 0.;
        int totalWords = 0;
        int maxLength = -1;
        int maxLengthFreq = 0;

        // Adds all HashMaps into one HashMap (lengthToFrequency)
        mapResults.forEach(mapResult -> mapResult.getFrequency()
                .forEach((key, value) -> lengthToFrequency
                        .merge(key, value, Integer::sum)));
        // Adds all lists of maximum length words into one list (words)
        mapResults.forEach(mapResult ->
                Optional.ofNullable(mapResult.getWords())
                        .ifPresent(words::addAll));

        // Computes rank, maximum word length and its frequency
        for (Map.Entry<Integer, Integer> entry : lengthToFrequency.entrySet()) {
            rank += (getNthFib(entry.getKey() + 1) * entry.getValue());
            totalWords += entry.getValue();
            if (entry.getKey() > maxLength) {
                maxLength = entry.getKey();
                maxLengthFreq = entry.getValue();
            }
        }
        rank /= totalWords;

        return new ReduceResult(filename, rank, maxLength, maxLengthFreq);
    }
}
