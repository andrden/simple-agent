package contin.tests;

import contin.World;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Collections;

public class World01 implements World {
  double state=0;

  public List<String> commands() {
    return Arrays.asList("A","B","C");
  }

  public void command(String cmd, double force) {
    if( force<-1 || force>1 ){
      throw new RuntimeException(cmd+" force="+force);
    }
    if( cmd.equals("A") ) state=-1;
    if( cmd.equals("B") ) state=0;
    if( cmd.equals("C") ) state=1;  
  }

  public Map<String, Object> view() {
    return Collections.emptyMap();
  }

  public Map<String, Double> senses() {
    return Collections.singletonMap("s",state);
  }
}
