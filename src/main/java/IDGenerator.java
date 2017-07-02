/**
 * Monotonically increasing ID generator. Thread-safe.
 */
public class IDGenerator {
    // Unique ID generation. Use this for file IDs
    private static long idCounter = 0;
    public static synchronized long createID() {
        long id = idCounter;
        idCounter += 1;

        return id;
    }
}
