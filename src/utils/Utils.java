package utils;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/4/2008
 * Time: 13:31:58
 */
public class Utils {
  public static boolean containsAll( Map<String,Object> where, Map<String,Object> what ){
    if( where==null ){
      throw new NullPointerException("where==null");
    }
    for( String s : what.keySet() ){
      if( !where.containsKey(s) ){
        return false;
      }
      if( !where.get(s).equals(what.get(s)) ){
        return false;
      }
    }
    return true;
  }

  public static <T> T rnd(List<T> l){
    return l.get( (int)(Math.random()*l.size()) );
  }

  public static String color2name(Color c){
    if( c.equals(Color.BLACK) ) return "BLACK";
    if( c.equals(Color.YELLOW) ) return "YELLOW";
    if( c.equals(Color.ORANGE) ) return "ORANGE";
    if( c.equals(Color.RED) ) return "RED";
    if( c.equals(Color.GREEN) ) return "GREEN";
    if( c.equals(Color.WHITE) ) return "WHITE";
    throw new IllegalArgumentException(""+c);
  }

  public static boolean intersects(Set<String> s1, Set<String> s2){
    for( String s : s1 ){
      if( s2.contains(s) ){
        return true;
      }
    }
    return false;
  }

  public static String spaces(int count){
    StringBuilder sb = new StringBuilder();
    for( int i=0; i<count; i++ ){
      sb.append(' ');
    }
    return sb.toString();
  }
}
