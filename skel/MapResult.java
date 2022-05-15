import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that contains the name of the file analysed, a HashMap that maps
 * word's length to their frequency and a list of maximum length words
 */
public class MapResult {
    private final String filename;
    private final HashMap<Integer, Integer> frequency;
    private final ArrayList<String> words;

    public MapResult(String filename, HashMap<Integer, Integer> frequency, ArrayList<String> words) {
        this.filename = filename;
        this.frequency = frequency;
        this.words = words;
    }

    public String getFilename() {
        return filename;
    }

    public HashMap<Integer, Integer> getFrequency() {
        return frequency;
    }

    public ArrayList<String> getWords() {
        return words;
    }

    @Override
    public String toString() {
        return "MapResult{" +
                "filename='" + filename + '\'' +
                ", frequency=" + frequency +
                ", words=" + words +
                '}';
    }
}
