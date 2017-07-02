import org.junit.Test;

import java.time.Instant;
import java.util.Optional;

/**
 * Simple tests around the Customer class.
 */
public class CustomerTest {
    @Test
    public void testCustomerDeserialization() {
        Optional<Customer> maybeCustomer = Customer.fromCSVEntry
                ("E33A8D2A-A1E7-445C-ABE6-F2387F0FD489,\"Lowe, Peggy\",2017-05-24T21:09:18Z,34,0.47");

        assert(maybeCustomer.isPresent());

        Customer customer = maybeCustomer.get();
        assert(customer.getID().toString().equalsIgnoreCase("E33A8D2A-A1E7-445C-ABE6-F2387F0FD489"));
        assert(customer.getName().equals("Lowe, Peggy"));
        assert(customer.getDate().equals(Instant.parse("2017-05-24T21:09:18Z")));
        assert(customer.getScore().equals(34));
        assert(customer.getWeighting().equals(0.47));
    }
}
