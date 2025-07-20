package rentalsystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadSafeRentalSystemTest {
    private RentalSystem rentalSystem;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @BeforeEach
    public void prepare() {
        rentalSystem = new ThreadSafeRentalSystem();
    }

    @Test
    public void testAddingCarsByMultipleThreads() throws BrokenBarrierException, InterruptedException {
        final int threadNumber = 5;

        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadNumber + 1);
        ExecutorService executorService = Executors.newFixedThreadPool(threadNumber);
        TestParameters[] testParamsArray = new TestParameters[]{
                new TestParameters(CarType.SUV, 100),
                new TestParameters(CarType.SEDAN, 150),
                new TestParameters(CarType.VAN, 200),
        };

        List<CarType> shuffledList = buildCarTypeList(testParamsArray);
        Callable[] callables = new Callable[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            int startIndex = i;
            callables[i] = () -> {
                for (int j = startIndex; j < shuffledList.size(); j += threadNumber) {
                    rentalSystem.addCar(shuffledList.get(j));
                }
                cyclicBarrier.await();
                return 0;
            };
        }
        for (Callable callable : callables) {
            executorService.submit(callable);
        }
        cyclicBarrier.await();
        executorService.shutdown();

        Map<CarType, List<Car>> actualAvailableCars = rentalSystem.getAvailableCars();
        assertEquals(100, actualAvailableCars.get(CarType.SUV).size());
        assertEquals(150, actualAvailableCars.get(CarType.SEDAN).size());
        assertEquals(200, actualAvailableCars.get(CarType.VAN).size());

        int maxCarId = Arrays.stream(CarType.values())
                .map(actualAvailableCars::get)
                .flatMap(Collection::stream)
                .mapToInt(car -> car.id)
                .max()
                .getAsInt();
        assertEquals(450, maxCarId);
    }

    @Test
    public void testReservationUsingMultipleThreads() throws BrokenBarrierException, InterruptedException {
        final int threadNumber = 10;

        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadNumber + 1);
        ExecutorService executorService = Executors.newFixedThreadPool(threadNumber);
        TestParameters[] testParamsArray = new TestParameters[]{
                new TestParameters(CarType.SUV, 100),
                new TestParameters(CarType.SEDAN, 200),
                new TestParameters(CarType.VAN, 300),
        };

        for (TestParameters testParameters : testParamsArray) {
            addCarsToSystem(testParameters.count, testParameters.carType);
        }

        List<CarType> shuffledList = buildCarTypeList(testParamsArray);
        Callable[] callables = new Callable[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            int startIndex = i;
            callables[i] = () -> {
                for (int j = startIndex; j < shuffledList.size(); j += threadNumber) {
                    CarType carType = shuffledList.get(j);
                    try {
                        for (int k = 0; k < 3; k++) {
                            rentalSystem.reserveCar(carType, LocalDateTime.parse("11.07.2025 14:00", DATE_TIME_FORMATTER), 3);
                        }
                    } catch (IllegalStateException ex) {
                        //this is expected behaviour, moving on
                    }
                }
                cyclicBarrier.await();
                return 0;
            };
        }
        for (Callable callable : callables) {
            executorService.submit(callable);
        }
        cyclicBarrier.await();
        executorService.shutdown();

        List<Booking> allBookings = rentalSystem.getAllBookings();
        assertEquals(600, allBookings.size());
    }

    private List<CarType> buildCarTypeList(TestParameters[] testParamsArray) {
        List<CarType> cars = new ArrayList<>();
        for (TestParameters testParameters : testParamsArray) {
            for (int i = 0; i < testParameters.count; i++) {
                cars.add(testParameters.carType);
            }
        }
        Collections.shuffle(cars);
        return new CopyOnWriteArrayList<>(cars);
    }

    private void addCarsToSystem(int count, CarType carType) {
        for (int i = 0; i < count; i++) {
            rentalSystem.addCar(carType);
        }
    }

    private static class TestParameters {
        CarType carType;
        int count;

        public TestParameters(CarType carType, int count) {
            this.carType = carType;
            this.count = count;
        }
    }

}
