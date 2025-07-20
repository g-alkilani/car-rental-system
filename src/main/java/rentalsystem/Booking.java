package rentalsystem;

import java.time.LocalDateTime;

public class Booking {
    private final CarType bookedType;
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final int carId;
    //omitting necessary in practice fields like customerId, orderId

    public Booking(CarType bookedType, LocalDateTime from, LocalDateTime to, int carId) {
        this.bookedType = bookedType;
        this.from = from;
        this.to = to;
        this.carId = carId;
    }

    public CarType getBookedType() {
        return bookedType;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public int getCarId() {
        return carId;
    }
}
