package logic;

import mem.Cause;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 24/3/2008
 * Time: 17:38:47
 */
public class CauseMaxStruct {
  List<Cause> active = new ArrayList<Cause>();

  int promisedMax() {
    int max = Integer.MIN_VALUE;
    for (Cause c : active) {
      if (c.hasResult()) {
        max = Math.max(max, c.getResult());
      }
    }
    return max;
  }


  public String toString() {
    return active.toString();
  }

  boolean noop() {
    if (active.isEmpty()) {
      return false;
    }
    for (Cause c : active) {
      if (!c.noop()) {
        return false;
      }
    }
    return true;
  }

  boolean hasResult() {
    for (Cause c : active) {
      if (c.hasResult()) {
        return true;
      }
    }
    return false;
  }

}
