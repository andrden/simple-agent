package autogroup;

import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 24 лист 2008
 * Time: 12:04:06
 */
public class Grper1 {
  TreeMap<String, Grp1> grps = new TreeMap<String, Grp1>();
  void next(final String s){
    for( int i=s.length(); i>0; i-- ){
      String prefix = s.substring(0,i);
      Grp1 g = grps.get(prefix);
      if( g!=null ){
        g.add(s);
        return;
      }
      String h = grps.higherKey(prefix);
      if( h!=null && h.startsWith(prefix) ){
        g = grps.get(h);
        g.add(s);
        return;
      }
    }
    grps.put(s, new Grp1(s));
  }

}
