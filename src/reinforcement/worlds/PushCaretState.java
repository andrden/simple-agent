package reinforcement.worlds;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: Jul 28, 2009
* Time: 6:19:43 PM
* To change this template use File | Settings | File Templates.
*/
class PushCaretState implements RState{
  int rotate;
  int arm;

  PushCaretState(int rotate, int arm) {
    this.rotate = rotate;
    this.arm = arm;
  }

  @Override
  public String toString() {
    return rotate+" "+arm;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PushCaretState that = (PushCaretState) o;

    if (arm != that.arm) return false;
    if (rotate != that.rotate) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = rotate;
    result = 31 * result + arm;
    return result;
  }
}