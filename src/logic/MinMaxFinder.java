package logic;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 15 жовт 2008
 * Time: 14:01:09
 */
public class MinMaxFinder {
  boolean first = true;

  double minVal;
  List<String> minNames = new ArrayList<String>();

  public void add(double val, String name) {
    if (first || val <= minVal) {
      if( !first && val < minVal ){
        minNames.clear();
      }
      minVal = val;
      minNames.add(name);
    }
    first = false;
  }

  public List<String> getMinNames() {
    return minNames;
  }
}
