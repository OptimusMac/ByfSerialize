package byfserialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
public class Car {

  private String mark;
  private int wheels;
  private int price;

}
