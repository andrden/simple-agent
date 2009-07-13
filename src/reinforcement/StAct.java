package reinforcement;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jun 22, 2009
 * Time: 7:51:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class StAct {
  String s;
  String a;

  public StAct(String s, String a) {
    this.s = s;
    this.a = a;
  }

  public String getS() {
    return s;
  }

  public void setS(String s) {
    this.s = s;
  }

  public String getA() {
    return a;
  }

  public void setA(String a) {
    this.a = a;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StAct stAct = (StAct) o;

    if (a != null ? !a.equals(stAct.a) : stAct.a != null) return false;
    if (s != null ? !s.equals(stAct.s) : stAct.s != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = s != null ? s.hashCode() : 0;
    result = 31 * result + (a != null ? a.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return s + " "+a;
  }
}
