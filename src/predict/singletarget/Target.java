package predict.singletarget;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 29/7/2008
 * Time: 12:07:37
 */
public class Target {
  String key;
  Object val;


  public Target(String key, Object val) {
    this.key = key;
    this.val = val;
  }


  public String toString() {
    return key + "=" + val;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Target target = (Target) o;

    if (key != null ? !key.equals(target.key) : target.key != null) return false;
    if (val != null ? !val.equals(target.val) : target.val != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (key != null ? key.hashCode() : 0);
    result = 31 * result + (val != null ? val.hashCode() : 0);
    return result;
  }
}
