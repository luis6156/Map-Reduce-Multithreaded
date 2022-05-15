/**
 * Class that contains the name of the file analysed, its rank, maximum word
 * length and its corresponding frequency
 */
public class ReduceResult {
    private final String filename;
    private final double rank;
    private final int maxLength;
    private final int frequency;

    public ReduceResult(String filename, double rank, int maxLength, int frequency) {
        this.filename = filename;
        this.rank = rank;
        this.maxLength = maxLength;
        this.frequency = frequency;
    }

    public String getFilename() {
        return filename;
    }

    public double getRank() {
        return rank;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getFrequency() {
        return frequency;
    }
}
