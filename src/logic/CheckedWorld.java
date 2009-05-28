package logic;

import intf.World;
import mem.Hist;

import java.io.Serializable;
import java.util.*;

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

  public Collection<String> targetSensors() {
    Collection<String> t = w.targetSensors();
    for( String s : t ){
      if( !s.startsWith("$") ){
        throw new RuntimeException(s + " must start with $");
      }
    }
    return t;
  }

  public List<String> commands() {
    List<String> c = w.commands();
    for (String s : c) {
      if( s.contains(" ") ){
        throw new RuntimeException(s + " has spaces");
      }
      if (sensors.contains(s)) {
        throw new RuntimeException(s + " is a sensor");
      }
      if (c.contains("@")) {
        throw new RuntimeException();
      }
    }
    commands.addAll(c);
    return c;
  }

  public Map<String, Object> view() {
    Map<String, Object> v = w.view();
    for (String s : v.keySet()) {
      if (commands.contains(s)) {
        throw new RuntimeException(s + " is a command");
      }
      if (s.contains("@")) {
        throw new RuntimeException();
      }
    }
    sensors.addAll(v.keySet());
    return v;
  }

  public void command(String cmd) {
    w.command(cmd);
  }

}
