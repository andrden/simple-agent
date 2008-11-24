package autogroup;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 19 лист 2008
 * Time: 14:09:46
 */
public class Grp1 {
  String prefix;
  List<String> instances = null;
  Map<String, Grp1> subGroups = null;

  public Grp1(String prefix) {
    this.prefix = prefix;
  }

  void add(String val){
    if( subGroups!=null ){
      for( String s : subGroups.keySet() ){
        if( val.startsWith(s) ){
          subGroups.get(s).add(val);
          return;
        }
      }
    }
    if( instances==null ){
      instances = new ArrayList<String>();
      instances.add(prefix);
    }
    instances.add(val);
    prefix = maxCommonPrefix(prefix, val);
    if( prefix.length() < val.length() ){
      List<String> ls = selectByPrefix(val.substring(0, prefix.length()+1));
      if( ls.size()>1 ){
        String comm = maxCommonPrefix(ls);
        if( subGroups==null ){
          subGroups = new HashMap<String, Grp1>();
        }
        Grp1 sub = new Grp1(comm);
        subGroups.put(comm, sub);
        sub.instances = ls;
        removeWithPrefix(comm);
        System.nanoTime();
      }
    }
  }

  void removeWithPrefix(String p){
    for( int i=instances.size()-1; i>=0; i-- ){
      if( instances.get(i).startsWith(p) ){
        instances.remove(i);
      }
    }
  }

  public String toString() {
    return (instances==null ? "" : "ins="+instances.size())
            + (subGroups==null ? "" : " "+subGroups.keySet());
  }

  public List<String> selectByPrefix(String prefix){
    List<String> ret = new ArrayList<String>();
    for( String s : instances ){
      if( s.startsWith(prefix) ){
        ret.add(s);
      }
    }
    return ret;
  }

  public String maxCommonPrefix(String s1, String s2){
    for( int i=Math.min(s1.length(), s2.length()); i>=0; i-- ){
      String p1 = s1.substring(0,i);
      String p2 = s2.substring(0,i);
      if( p1.equals(p2) ){
        return p1;
      }
    }
    throw new IllegalStateException();
  }


  public String maxCommonPrefix(List<String> ls){
    String prev = "";
    String s0 = ls.get(0);
    for( int i=1; i<=s0.length(); i++ ){
      String test = s0.substring(0,i);
      for( String s : ls ){
        if( !s.startsWith(test) ){
          return prev;
        }
      }
      prev=test;
    }
    return prev;
  }
}
