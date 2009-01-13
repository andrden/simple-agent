package com.pmstation.common.utils;

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
  List<String> maxNames = new ArrayList<String>();

  public void add(double val, String name) {
    if (first || val <= minVal) {
      if( val < minVal ){
        minNames.clear();
      }
      minNames.add(name);
      minVal = val;
    }
    if( first || val>= maxVal ){
      if( val > maxVal ){
        maxNames.clear();
      }
      maxNames.add(name);
      maxVal=val;
    }
    first = false;
  }

  public List<String> getMinNames() {
    return minNames;
  }

  public List<String> getMaxNames() {
    return maxNames;
  }

  public double getMinVal() {
    return minVal;
  }

  public double getMaxVal() {
    return maxVal;
  }

  public static void main(String[] args){
    MinMaxFinder m = new MinMaxFinder();
    m.add(5,"bbbbb");
    m.add(3,"abc");
    m.add(5,"ddddd");
    System.out.println(m.getMaxNames());
  }
}
