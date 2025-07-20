package rentalsystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.LocalDateTime.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BasicRentalSystemTest {
    private RentalSystem rentalSystem;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @BeforeEach
    public void prepare() {
        rentalSystem = new BasicRentalSystem();
    }

    @Test
    public void testAddingCar() {
        addCarsToSystem(4, CarType.SEDAN);
        addCarsToSystem(2, CarType.SUV);
        addCarsToSystem(5, CarType.VAN);

        Map<CarType, List<Car>> availableCars = rentalSystem.getAvailableCars();
        assertEquals(4, availableCars.get(CarType.SEDAN).size());
        assertEquals(2, availableCars.get(CarType.SUV).size());
        assertEquals(5, availableCars.get(CarType.VAN).size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3, -4})
    public void testNonPositiveDays(int days) {
        assertThrows(IllegalArgumentException.class, () -> {
            rentalSystem.reserveCar(CarType.SEDAN, parse("10.07.2025 14:00", DATE_TIME_FORMATTER), days);
        });
    }

    @ParameterizedTest
    @MethodSource("casesWhenCanMakeBooking")
    public void testSuccessfulReservation(CarType typeToBook, LocalDateTime from, int days) {
        addCarsToSystem(3, CarType.SEDAN);
        rentalSystem.reserveCar(CarType.SEDAN, parse("10.07.2025 14:00", DATE_TIME_FORMATTER), 3);
        rentalSystem.reserveCar(CarType.SEDAN, parse("11.07.2025 14:00", DATE_TIME_FORMATTER), 3);
        rentalSystem.reserveCar(CarType.SEDAN, parse("12.07.2025 14:00", DATE_TIME_FORMATTER), 3);

        rentalSystem.reserveCar(typeToBook, from, days);
        assertEquals(4, rentalSystem.getAllBookings().size());
    }


    @ParameterizedTest
    @MethodSource("casesWhenBookingNotPossible")
    public void testBookingNotPossible(CarType typeToBook, LocalDateTime from, int days) {
        addCarsToSystem(3, CarType.SEDAN);
        rentalSystem.reserveCar(CarType.SEDAN, parse("10.07.2025 14:00", DATE_TIME_FORMATTER), 3);
        rentalSystem.reserveCar(CarType.SEDAN, parse("11.07.2025 14:00", DATE_TIME_FORMATTER), 3);
        rentalSystem.reserveCar(CarType.SEDAN, parse("12.07.2025 14:00", DATE_TIME_FORMATTER), 3);

        assertThrows(IllegalStateException.class, () -> {
            rentalSystem.reserveCar(typeToBook, from, days);
        });
        assertEquals(3, rentalSystem.getAllBookings().size());
    }

    public static Stream<Arguments> casesWhenCanMakeBooking() {
        return Stream.of(
                Arguments.of(CarType.SEDAN, parse("07.07.2025 10:00", DATE_TIME_FORMATTER), 3),
                Arguments.of(CarType.SEDAN, parse("07.07.2025 14:00", DATE_TIME_FORMATTER), 3),
                Arguments.of(CarType.SEDAN, parse("07.07.2025 14:00", DATE_TIME_FORMATTER), 4),
                Arguments.of(CarType.SEDAN, parse("07.07.2025 13:59", DATE_TIME_FORMATTER), 5),
                Arguments.of(CarType.SEDAN, parse("08.07.2025 13:59", DATE_TIME_FORMATTER), 4),
                Arguments.of(CarType.SEDAN, parse("09.07.2025 13:59", DATE_TIME_FORMATTER), 3),
                Arguments.of(CarType.SEDAN, parse("10.07.2025 13:59", DATE_TIME_FORMATTER), 2),
                Arguments.of(CarType.SEDAN, parse("11.07.2025 13:59", DATE_TIME_FORMATTER), 1),
                Arguments.of(CarType.SEDAN, parse("13.07.2025 14:01", DATE_TIME_FORMATTER), 2),
                Arguments.of(CarType.SEDAN, parse("14.07.2025 14:00", DATE_TIME_FORMATTER), 2),
                Arguments.of(CarType.SEDAN, parse("15.07.2025 14:00", DATE_TIME_FORMATTER), 2),
                Arguments.of(CarType.SEDAN, parse("20.07.2025 14:00", DATE_TIME_FORMATTER), 2)
        );
    }

    public static Stream<Arguments> casesWhenBookingNotPossible() {
        return Stream.of(
                Arguments.of(CarType.SEDAN, parse("09.07.2025 14:00", DATE_TIME_FORMATTER), 3),
                Arguments.of(CarType.SEDAN, parse("10.07.2025 14:00", DATE_TIME_FORMATTER), 2),
                Arguments.of(CarType.SEDAN, parse("11.07.2025 14:00", DATE_TIME_FORMATTER), 1),
                Arguments.of(CarType.SEDAN, parse("11.07.2025 14:00", DATE_TIME_FORMATTER), 2),
                Arguments.of(CarType.SEDAN, parse("11.07.2025 14:00", DATE_TIME_FORMATTER), 10),
                Arguments.of(CarType.SEDAN, parse("12.07.2025 14:00", DATE_TIME_FORMATTER), 2),
                Arguments.of(CarType.SEDAN, parse("13.07.2025 13:00", DATE_TIME_FORMATTER), 1),
                Arguments.of(CarType.SEDAN, parse("13.07.2025 13:00", DATE_TIME_FORMATTER), 2),
                Arguments.of(CarType.SEDAN, parse("13.07.2025 14:00", DATE_TIME_FORMATTER), 1)
        );
    }

    private void addCarsToSystem(int count, CarType carType) {
        for (int i = 0; i < count; i++) {
            rentalSystem.addCar(carType);
        }
    }

}
