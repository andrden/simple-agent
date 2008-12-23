package utils;

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
  double maxVal;
  List<String> minNames = new ArrayList<String>();

  public void add(double val, String name) {
    if (first || val <= minVal) {
      if( !first && val < minVal ){
        minNames.clear();
      }
      minNames.add(name);
      minVal = val;
    }
    if( first || val>= maxVal ){
      maxVal=val;
    }
    first = false;
  }

  public List<String> getMinNames() {
    return minNames;
  }

  public double getMinVal() {
    return minVal;
  }

  public double getMaxVal() {
    return maxVal;
  }
}
