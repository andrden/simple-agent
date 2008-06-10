package logic;

import intf.World;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

import mem.Hist;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 24/3/2008
 * Time: 18:03:46
 */
public class CheckedWorld implements World, Serializable {
  World w;
  Set<String> commands = new HashSet<String>();
  Set<String> sensors = new HashSet<String>();


  public CheckedWorld(World w) {
    this.w = w;
    commands.add(Hist.CMD_KEY);
    commands.add(Hist.RES_KEY);
    commands.add(Hist.NOOP_KEY);

    sensors.add(Hist.CMD_KEY);
    sensors.add(Hist.RES_KEY);
    sensors.add(Hist.NOOP_KEY);
  }

  public List<String> commands() {
    List<String> c = w.commands();
    for( String s : c ){
      if( sensors.contains(s) ){
        throw new RuntimeException(s+" is a sensor");
      }
      if( c.contains("@") ){
        throw new RuntimeException();
      }
    }
    commands.addAll(c);
    return c;
  }

  public void command(String cmd) {
    w.command(cmd);
  }

  public Map<String, Object> view() {
    Map<String, Object> v =  w.view();
    for( String s : v.keySet() ){
      if( commands.contains(s) ){
        throw new RuntimeException(s+" is a command");
      }
      if( s.contains("@") ){
        throw new RuntimeException();
      }
    }
    sensors.addAll(v.keySet());
    return v;
  }

  public int result(boolean reset) {
    return w.result(reset);
  }
}
