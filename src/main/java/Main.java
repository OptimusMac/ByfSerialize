import byfserialize.ByfSerialize;
import byfserialize.Car;


public class Main {
  public static void main(String[] args) {
    Car car = new Car("mazda", 4, 10000);
    ByfSerialize serialize = new ByfSerialize();
    String ser = serialize.serialize(car);

    Car car1 = serialize.deserialize(ser, Car.class);
    System.out.println(car1);
  }
}
