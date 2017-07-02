import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.servlet.MultipartConfigElement;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * CSVServer is a small service for parsing and exposing CSV files as JSON, exposed over HTTP.
 */
public class CSVServer {
    // Configuration variables.
    private static final int PORT = 65530;

    // The files that have been uploaded to our server. Stored in-memory as a map of file IDs to customers.
    private static Map<Long, Customer[]> files = new HashMap<>();

    // Logger.
    private static final Logger log = LoggerFactory.getLogger(CSVServer.class);

    // The entry point to our server.
    public static void main(String[] args) {
        Spark.port(PORT);

        // Define the status endpoint.
        // This is useful for a simple health check.
        Spark.get("/status", (req, res) -> {
            ServerStatus status = new ServerStatus(true, files.size());
            return status.toJSONString();
        });

        // Define the file upload endpoint.
        // This endpoint expects multipart/form-data with a customer CSV file at key "csvFile".
        // It attempts to parse this file into an array of customers, store them in our in-memory, and return the
        // file ID for future queries.
        Spark.post("/uploadFile", (request, response) -> {
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

            try (InputStream inputStream = request.raw().getPart("csvFile").getInputStream()) {
                Long fileID = IDGenerator.createID();
                log.debug("Capturing file at ID {}", fileID);

                // Parse the file into an array of Customers.
                // Ignore any row that results in a parsing failure.
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                Customer[] customers = bufferedReader.lines()
                        .map(Customer::fromCSVEntry)
                        .flatMap(optional -> optional.isPresent() ? Stream.of(optional.get()) : Stream.empty())
                        .toArray(Customer[]::new);

                // Store the array of Customers in our map for future use.
                files.put(fileID, customers);

                return "{ \"fileID\": " + fileID.toString() + " }";
            } catch (Exception e) {
                response.status(400);
                return "{ \"errorMessage\": \"File not found in request\" }";
            }
        });

        // Define the get file endpoint.
        // This endpoint searches our in-memory data store for the specified file.
        // If the file is found, it will dump all customers contained in that file.
        Spark.get("/file/:fileID/customers", (request, response) -> {
            String fileIDString = request.params("fileID");
            if (fileIDString == null) {
                response.status(400);
                return "{ \"errorMessage\": \"File ID unspecified\" }";
            }

            Long fileID = Long.valueOf(fileIDString);
            if (!files.containsKey(fileID)) {
                response.status(404);
                return "{ \"errorMessage\": \"File with ID " + fileIDString + " not found\" }";
            }

            Customer[] customers = files.get(fileID);

            log.debug("Got " + customers.length + " rows");

            StringBuffer output = new StringBuffer();
            output.append("{ \"customers\": ");
            output.append(Customer.serializeCustomerList(customers));
            output.append(" }");

            return output.toString();
        });

        // Define the get customer endpoint.
        // This endpoint searches the specified file for a specified customer.
        // If the customer is found, it will dump it out.
        Spark.get("/file/:fileID/customer/:customerID", (request, response) -> {
            String fileIDString = request.params("fileID");
            if (fileIDString == null) {
                response.status(400);
                return "{ \"errorMessage\": \"File ID unspecified\" }";
            }
            String customerIDString = request.params("customerID");
            if (customerIDString == null) {
                response.status(400);
                return "{ \"errorMessage\": \"Customer ID unspecified\" }";
            }

            Long fileID = Long.valueOf(fileIDString);
            if (!files.containsKey(fileID)) {
                response.status(404);
                return "{ \"errorMessage\": \"File with ID " + fileIDString + " not found\" }";
            }

            log.debug("Searching for customer with ID " + customerIDString);

            Customer[] customers = files.get(fileID);
            Optional<Customer> outputCustomer = Optional.empty();
            for (Customer customer : customers) {
                if (customer.getID().toString().equalsIgnoreCase(customerIDString)) {
                    outputCustomer = Optional.of(customer);
                    break;
                }
            }

            if (!outputCustomer.isPresent()) {
                response.status(404);
                return "{ \"errorMessage\": \"Customer with ID " + customerIDString + " not found\" }";
            }

            return outputCustomer.get().toJSONString();
        });


        // Initialize HTTP server.
        Spark.init();
    }
}
