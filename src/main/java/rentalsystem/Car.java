package rentalsystem;

public class Car {
    public final CarType type;
    public final int id;
    //omitting potentially necessary fields like carLocation, brand

    public Car(CarType type, int id) {
        this.type = type;
        this.id = id;
    }

    public CarType getType() {
        return type;
    }

    public int getId() {
        return id;
    }
}
