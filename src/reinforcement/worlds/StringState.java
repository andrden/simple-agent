package reinforcement.worlds;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jul 28, 2009
 * Time: 6:13:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringState implements RState{
  String s;

  public StringState(String s) {
    this.s = s;
  }

  @Override
  public String toString() {
    return s;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StringState that = (StringState) o;

    if (s != null ? !s.equals(that.s) : that.s != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return s != null ? s.hashCode() : 0;
  }
}
