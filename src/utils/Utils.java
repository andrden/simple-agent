package utils;

import mem.OneView;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.Reader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/4/2008
 * Time: 13:31:58
 */
public class Utils {
  public static boolean containsAll(Map<String, Object> where, Map<String, Object> what) {
    if (where == null) {
      throw new NullPointerException("where==null");
    }
    for (String s : what.keySet()) {
      if (!where.containsKey(s)) {
        return false;
      }
      if (!where.get(s).equals(what.get(s))) {
        return false;
      }
    }
    return true;
  }

  public static <T> T rnd(List<T> l) {
    return l.get((int) (Math.random() * l.size()));
  }

  public static String color2name(Color c) {
    if (c.equals(Color.BLACK)) return "BLACK";
    if (c.equals(Color.YELLOW)) return "YELLOW";
    if (c.equals(Color.ORANGE)) return "ORANGE";
    if (c.equals(Color.RED)) return "RED";
    if (c.equals(Color.GREEN)) return "GREEN";
    if (c.equals(Color.WHITE)) return "WHITE";
    if (c.equals(Color.GRAY)) return "GRAY";
    if (c.equals(Color.BLUE)) return "BLUE";
    if (c.equals(Color.CYAN)) return "CYAN";
    throw new IllegalArgumentException("" + c);
  }

  public static boolean intersects(Set<String> s1, Set<String> s2) {
    for (String s : s1) {
      if (s2.contains(s)) {
        return true;
      }
    }
    return false;
  }

  public static String spaces(int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(' ');
    }
    return sb.toString();
  }

  /**
   * Removes from 'from' all mappings already present in 'what'
   */
  public static Map<String, Object> difference(Map<String, Object> from, Map<String, Object> what) {
    Map<String, Object> ret = new HashMap<String, Object>();
    for (String k : from.keySet()) {
      if (!what.containsKey(k) || !from.get(k).equals(what.get(k))) {
        ret.put(k, from.get(k));
      }
    }
    return ret;
  }

  public static Map<String, Object> intersection(Map<String, Object> m1, Map<String, Object> m2) {
    Map<String, Object> ret = new HashMap<String, Object>();
    for (String k : m1.keySet()) {
      if (m2.containsKey(k) && m2.get(k).equals(m1.get(k))) {
        ret.put(k, m1.get(k));
      }
    }
    return ret;
  }

  public static void retainEqualsIn(Map<String, Object> testAgainst, Map<String, Object> retainIn) {
    for (Iterator<String> i = retainIn.keySet().iterator(); i.hasNext();) {
      String k = i.next();
      if (!testAgainst.containsKey(k)) {
        i.remove();
      } else if (!retainIn.get(k).equals(testAgainst.get(k))) {
        i.remove();
      }
    }
  }


  /**
   * Get all data from a Reader as a String.
   * @param r Reader
   * @throws java.io.IOException
   * @return String
   */
  public static String readAll(Reader r) throws IOException {
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[4096];
    int len;
    while( (len=r.read(buf)) != -1 ){
      sb.append(buf,0,len);
    }
    return sb.toString();
  }

  public static Map<String,Object> interstectingVals(List<OneView> exList){
    Map<String, Object> m = null;
    for( OneView v : exList ){
      if( m==null ){
        m = v.getViewAll();
      }else{
        for( Iterator<String> i = m.keySet().iterator(); i.hasNext(); ){
          String k = i.next();
          if( !m.get(k).equals(v.get(k)) ){
            i.remove();
          }
        }
      }
    }
    return m;
  }
  
}
