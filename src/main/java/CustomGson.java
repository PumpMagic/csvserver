import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

/**
 * CustomGson exposes a shared Gson instance for serializing and deserializing objects to and from JSON.
 *
 * It modifies default Gson behavior to handle conversion of types relevant to our application.
 */
public class CustomGson {
    public static Gson INSTANCE = createInstance();

    /**
     * Custom Instant serializer. The default exposes epoch time information rather than an ISO 8601 timestamp string.
     */
    private static class InstantSerializer implements JsonSerializer<Instant> {
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    /**
     * Create a custom Gson instance.
     */
    private static Gson createInstance() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Instant.class, new InstantSerializer());

        return builder.create();
    }
}
