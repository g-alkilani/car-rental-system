package rentalsystem;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeRentalSystem implements RentalSystem {
    private final AtomicInteger nextCarId = new AtomicInteger(0);
    private final Map<Integer, Car> idToCar = new ConcurrentHashMap<>();
    private final Map<CarType, List<Booking>> carTypeToBookings = new ConcurrentHashMap<>();
    private final Map<CarType, List<Integer>> carTypeToIds = new ConcurrentHashMap<>();


    public ThreadSafeRentalSystem() {
        for (CarType carType : CarType.values()) {
            carTypeToBookings.put(carType, new CopyOnWriteArrayList<>());
            carTypeToIds.put(carType, new CopyOnWriteArrayList<>());
        }
    }

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
        synchronized (carType) {
            List<Booking> existingBookings = carTypeToBookings.get(carType);
            for (Booking booking : existingBookings) {
                if (hasOverlap(booking, bookingFrom, bookingTo)) {
                    availableCars.remove(booking.getCarId());
                }
            }

            if (!availableCars.isEmpty()) {
                Integer bookedCar = availableCars.iterator().next();
                Booking newBooking = new Booking(carType, bookingFrom, bookingTo, idToCar.get(bookedCar).getId());
                existingBookings.add(newBooking);
                return newBooking;
            } else {
                throw new IllegalStateException("No cars left for booking");
            }
        }
    }

    @Override
    public Map<CarType, List<Car>> getAvailableCars() {
        Map<CarType, List<Car>> carTypeToCars = new HashMap<>();
        for (Map.Entry<CarType, List<Integer>> entry : carTypeToIds.entrySet()) {
            List<Car> cars = new ArrayList<>(entry.getValue().size());
            carTypeToCars.put(entry.getKey(), cars);
            for (Integer carId : entry.getValue()) {
                cars.add(idToCar.get(carId));
            }
        }
        return carTypeToCars;
    }

    @Override
    public boolean addCar(CarType carType) {
        int carId = nextCarId.incrementAndGet();
        Car car = new Car(carType, carId);
        idToCar.put(carId, car);
        carTypeToIds.get(carType).add(carId);
        return true;
    }

    @Override
    public List<Booking> getAllBookings() {
        List<Booking> allBookings = new ArrayList<>();
        for (List<Booking> bookingsPerType : carTypeToBookings.values()) {
            allBookings.addAll(bookingsPerType);
        }
        return allBookings;
    }

}
