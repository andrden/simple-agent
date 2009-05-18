package contin.tests;

import contin.World;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

public class World1 implements World {
  double state=-10;

  public List<String> commands() {
    return Arrays.asList("C");
  }

  public void command(String cmd, double force) {
    if( force<-1 || force>1 ){
      throw new RuntimeException(cmd+" force="+force);
    }
    if( state+force>0 ){
      state-=force;
    }else{
      state+=force;
    }
  }

  public Map<String, Object> view() {
    return Collections.emptyMap();
  }

  public Map<String, Double> senses() {
    return Collections.singletonMap("s",state);
  }
}
