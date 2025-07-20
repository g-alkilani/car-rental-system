package rentalsystem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RentalSystem {

    Booking reserveCar(CarType carType, LocalDateTime from, int days);

    boolean addCar(CarType carType);

    Map<CarType, List<Car>> getAvailableCars();

    List<Booking> getAllBookings();

    default boolean hasOverlap(Booking booking, LocalDateTime bookingFrom, LocalDateTime bookingTo) {
        return !booking.getTo().isBefore(bookingFrom) && !booking.getFrom().isAfter(bookingTo);
    }
}
