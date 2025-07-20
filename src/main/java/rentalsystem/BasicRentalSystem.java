package rentalsystem;

import java.time.LocalDateTime;
import java.util.*;

public class BasicRentalSystem implements RentalSystem {
    private int nextCarId = 1;
    private final List<Booking> bookings = new ArrayList<>();
    private final Map<CarType, Set<Integer>> carTypeToIds = new HashMap<>();
    private final Map<Integer, Car> idToCar = new HashMap<>();

    @Override
    public Booking reserveCar(CarType carType, LocalDateTime bookingFrom, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Number of days has to be positive");
        }

        if (carType == null) {
            throw new IllegalArgumentException("CarType can't be null");
        }

        LocalDateTime bookingTo = bookingFrom.plusDays(days);
        Set<Integer> availableCars = new HashSet<>(carTypeToIds.get(carType));
        for (Booking booking : bookings) {
            if (booking.getBookedType() == carType && hasOverlap(booking, bookingFrom, bookingTo)) {
                availableCars.remove(booking.getCarId());
            }
        }

        if (!availableCars.isEmpty()) {
            Integer bookedCar = availableCars.iterator().next();
            Booking newBooking = new Booking(carType, bookingFrom, bookingTo, idToCar.get(bookedCar).id);
            bookings.add(newBooking);
            return newBooking;
        } else {
            throw new IllegalStateException("No cars left for booking");
        }
    }

    @Override
    public boolean addCar(CarType carType) {
        Car car = new Car(carType, nextCarId);
        idToCar.put(nextCarId, car);
        carTypeToIds.computeIfAbsent(carType, k -> new HashSet<>()).add(nextCarId);
        nextCarId++;
        return true;
    }

    @Override
    public Map<CarType, List<Car>> getAvailableCars() {
        Map<CarType, List<Car>> carTypeToCars = new HashMap<>();
        for (Map.Entry<CarType, Set<Integer>> entry : carTypeToIds.entrySet()) {
            List<Car> cars = new ArrayList<>();
            carTypeToCars.put(entry.getKey(), cars);
            for (Integer carId : entry.getValue()) {
                cars.add(idToCar.get(carId));
            }
        }
        return carTypeToCars;
    }


    @Override
    public List<Booking> getAllBookings() {
        return Collections.unmodifiableList(bookings);
    }
}
