package worlds;

import intf.World;

import java.util.*;

/**
 * The simplest possible World - always press G and be happy!
 */
public class W1 implements World {
  int res=0;
  public List<String> commands() {
    return Arrays.asList("N","G");
  }

  public Collection<String> targetSensors() {
    return Collections.singleton("$");
  }

  public Map<String, Object> view() {
    return Collections.singletonMap("$",(Object)res);
  }

  public void command(String cmd) {
    if( cmd.equals("G") ){
      res=1;
    }else{
      res=0;
    }
  }
}
