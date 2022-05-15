import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Map operation for a given file -> return MapResult
 */
public class Map implements Callable<MapResult> {
    private final String name;
    private final int offset;
    private int fragmentSize;

    public Map(String name, int offset, int fragmentSize) {
        this.name = name;
        this.offset = offset;
        this.fragmentSize = fragmentSize;
    }

    /**
     * Adds currently formed word's characteristics to the length-to-frequency
     * map and length-to words map
     *
     * @param lengthToFrequency length-to-frequency map
     * @param lengthToWords length-to-words map
     * @param sb stringbuilder that stores currently formed word
     * @param numberOfChars length of the currently formed word
     */
    private void populateMaps(HashMap<Integer, Integer> lengthToFrequency,
                             HashMap<Integer, ArrayList<String>> lengthToWords,
                             StringBuilder sb, int numberOfChars) {
        ArrayList<String> words;

        // Check if current number of characters is already present in the
        // length-to-frequency map
        if (lengthToFrequency.containsKey(numberOfChars)) {
            // Increment frequency
            int prevFreq = lengthToFrequency.get(numberOfChars);
            lengthToFrequency.put(numberOfChars, prevFreq + 1);
            // Get list of words for the current number of characters
            words = lengthToWords.get(numberOfChars);
        } else {
            // Add entry into the map with frequency "1"
            lengthToFrequency.put(numberOfChars, 1);
            // Create a new list of words
            words = new ArrayList<>();
        }
        // Add word to the list of words associated to the current frequency
        words.add(sb.toString());

        // Add new list of words to the length-to-words map
        lengthToWords.put(numberOfChars, words);
    }

    /**
     * Checks if char is a number or a letter
     *
     * @param c char to be verified
     * @return true -> if char is AlphaNumeric, otherwise false
     */
    private boolean isAlphaNumeric(char c) {
        return Character.isDigit(c) ||
                Character.isLetter(c);
    }

    @Override
    public MapResult call() {
        HashMap<Integer, Integer> lengthToFrequency = new HashMap<>();
        HashMap<Integer, ArrayList<String>> lengthToWords = new HashMap<>();
        int max = -1;

        // Read data from file (try-with-resources)
        try (BufferedReader br = new BufferedReader(new FileReader(name))) {
            StringBuilder sb = new StringBuilder();
            int numberOfChars = 0;
            long r = 0;

            // Check if the current thread reads from the beginning of the file
            if (offset != 0) {
                // Go to offset - 1 to check if the current sequence starts
                // inside a word
                r = br.skip(offset - 1);
                // If the skip resulted in the end of file, return null
                if (r == 0) {
                    return null;
                }

                // Skip the word that is split
                while (true) {
                    if (!isAlphaNumeric((char) br.read())) {
                        break;
                    } else {
                        // Change required number of characters to be read
                        --fragmentSize;
                    }
                }
            }

            // Read "fragmentSize" characters from the file
            for (int i = 0; i < fragmentSize; ++i) {
                // Read character
                r = br.read();
                if (r == -1 && numberOfChars != 0) {
                    // Reached end of file and the thread read at least a
                    // character -> populate the maps and stop reading
                    // characters
                    populateMaps(lengthToFrequency, lengthToWords, sb,
                            numberOfChars);
                    // Update max length
                    if (numberOfChars > max) {
                        max = numberOfChars;
                    }
                    break;
                } else if (isAlphaNumeric((char)r)) {
                    // Character read is allowed -> add it to the string and
                    // increase counter for length
                    sb.append((char)r);
                    ++numberOfChars;
                } else if (numberOfChars != 0) {
                    // Character read is not allowed and the thread read at
                    // least a character -> add it to the string and increase
                    // counter for length
                    populateMaps(lengthToFrequency, lengthToWords, sb,
                            numberOfChars);
                    // Update max length
                    if (numberOfChars > max) {
                        max = numberOfChars;
                    }

                    // Reset counter and string
                    numberOfChars = 0;
                    sb.setLength(0);
                }
            }

            // Check if sequence ends inside a word
            if (r != -1 && numberOfChars != 0) {
                while (true) {
                    // Read character
                    r = br.read();
                    if (r == -1) {
                        // Reached end of file and the thread read at least a
                        // character -> populate the maps and stop reading
                        // characters
                        populateMaps(lengthToFrequency, lengthToWords, sb,
                                numberOfChars);
                        // Update max length
                        if (numberOfChars > max) {
                            max = numberOfChars;
                        }
                        break;
                    } else if (isAlphaNumeric((char)r)) {
                        // Character read is allowed -> add it to the string
                        // and increase counter for length
                        sb.append((char)r);
                        ++numberOfChars;
                    } else {
                        // Character read is not allowed and the thread read at
                        // least a character -> add it to the string and increase
                        // counter for length
                        populateMaps(lengthToFrequency, lengthToWords, sb,
                                numberOfChars);
                        // Update max length and stop reading characters
                        if (numberOfChars > max) {
                            max = numberOfChars;
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new MapResult(name, lengthToFrequency, lengthToWords.get(max));
    }
}
