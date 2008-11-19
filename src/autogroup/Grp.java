package autogroup;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 19 лист 2008
 * Time: 14:09:46
 */
public class Grp {
  String prefix;
  List<String> instances = null;
  int count;

  public Grp(String prefix) {
    this.prefix = prefix;
    count=1;
  }

  void add(String val){
    count++;
    if( instances==null ){
      instances = new ArrayList<String>();
      instances.add(prefix);
    }
    instances.add(val);
  }

  public String toString() {
    return "c="+count;
  }
}
