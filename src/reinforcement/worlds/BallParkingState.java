package reinforcement.worlds;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: Jul 28, 2009
* Time: 6:19:43 PM
* To change this template use File | Settings | File Templates.
*/
class BallParkingState implements RState{
  int x;
  int y;

  BallParkingState(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return x+" "+y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BallParkingState state = (BallParkingState) o;

    if (x != state.x) return false;
    if (y != state.y) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }
}
