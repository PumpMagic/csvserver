import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

/**
 * Customer captures a customer as defined in our sample CSV file.
 *
 * This class captures serialization and deserialization methods to convert to and from CSV, JSON, and debug-friendly
 * formats.
 */
public class Customer {
    private UUID id;
    private String name;
    private Instant date;
    private Integer score;
    private Double weighting;

    private static transient final Logger log = LoggerFactory.getLogger(Customer.class);
    private static transient final Gson gson = CustomGson.INSTANCE;


    /**
     * All-argument constructor.
     */
    private Customer(UUID id, String name, Instant date, Integer score, Double weighting) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.score = score;
        this.weighting = weighting;
    }

    // Getters.
    public UUID getID() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public Instant getDate() {
        return this.date;
    }
    public Integer getScore() {
        return this.score;
    }
    public Double getWeighting() {
        return this.weighting;
    }


    /**
     * Serialize this customer into a debug-friendly string.
     */
    public String toString() {
        return "Customer" +
                "\n\tID: " + this.id.toString() +
                "\n\tname: " + this.name +
                "\n\tdate: " + this.date.toString() +
                "\n\tscore: " + this.score.toString() +
                "\n\tweighting: " + this.weighting.toString();
    }

    /**
     * Serialize this customer into a JSON string.
     */
    public String toJSONString() {
        return gson.toJson(this);
    }

    /**
     * Attempt to deserialize a Customer from string representations of Customer attributes.
     */
    private static Optional<Customer> fromStrings(String uuidString, String name, String dateString, String scoreString,
                                           String weightingString)
    {
        try {
            UUID uuid = UUID.fromString(uuidString);
            Instant date = Instant.parse(dateString);
            Integer score = Integer.valueOf(scoreString);
            Double weighting = Double.valueOf(weightingString);

            return Optional.of(new Customer(uuid, name, date, score, weighting));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            log.warn("Unable to create Customer: malformed input");
        }

        return Optional.empty();
    }

    /**
     * Attempt to deserialize a Customer from a row in a CSV file.
     *
     * This implementation banks on the presence of a comma in the customer name string, and the absence of
     * non-delimiting commas elsewhere. That is to say, it is fragile.
     *
     * A robust implementation might leverage a CSV parsing library (like that in Apache Commons) to parse the row.
     */
    public static Optional<Customer> fromCSVEntry(String row) {
        final int numExpectedCommas = 5;

        String[] tokens = row.split(",");
        if (tokens.length != numExpectedCommas+1) {
            log.warn("Parsing customer failed: expected {} tokens, but got {}", numExpectedCommas+1, tokens.length);
            return Optional.empty();
        }

        log.debug("Creating customer from tokens: {} {} {} {} {} {}",
                tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);

        // Shady logic for handling the comma in the name string.
        String name = (tokens[1]+","+tokens[2]).replace("\"", "");

        return fromStrings(tokens[0], name, tokens[3], tokens[4], tokens[5]);
    }

    /**
     * Utility method that serializes an array of customers into a JSON array.
     */
    public static String serializeCustomerList(Customer[] customers) {
        return gson.toJson(customers);
    }
}
