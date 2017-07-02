import com.google.gson.Gson;

/**
 * ServerStatus captures the status of our HTTP server.
 */
public class ServerStatus {
    // Whether or not the server is alive.
    private boolean alive;
    // The number of files the server is storing.
    private int numFiles;

    private static transient final Gson gson = CustomGson.INSTANCE;

    public ServerStatus(boolean alive, int numFiles) {
        this.alive = alive;
        this.numFiles = numFiles;
    }

    /**
     * Serialize this status into a JSON string.
     */
    public String toJSONString() {
        return gson.toJson(this);
    }
}
