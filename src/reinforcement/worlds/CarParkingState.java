package reinforcement.worlds;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: Jul 28, 2009
* Time: 6:19:43 PM
* To change this template use File | Settings | File Templates.
*/
class CarParkingState implements RState{
  int x;
  int y;
  int carAngle; - discretization done by hand is not good

  CarParkingState(int x, int y, String prevCmd) {
    this.x = x;
    this.y = y;
    this.prevCmd = prevCmd;
  }

  @Override
  public String toString() {
    return x+" "+y+" "+prevCmd;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CarParkingState that = (CarParkingState) o;

    if (x != that.x) return false;
    if (y != that.y) return false;
    if (!prevCmd.equals(that.prevCmd)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    result = 31 * result + prevCmd.hashCode();
    return result;
  }
}