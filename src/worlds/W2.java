package worlds;

import intf.World;

import java.util.*;

/**
 * 2 pain sensors - soothe each with a corresponding command
 */
public class W2 implements World {
  int pain1=0;
  int pain2=0;
  public List<String> commands() {
    return Arrays.asList("N","G1","G2");
  }

  public Collection<String> targetSensors() {
    return Arrays.asList("$1","$2");
  }

  public Map<String, Object> view() {
    Map<String, Object> v = new HashMap<String, Object>();
    v.put("$1",pain1);
    v.put("$2",pain2);
    v.put("r", (int)(3*Math.random()) );
    return v;
  }

  public boolean commandSuboptimal(String cmd){
    Set<String> neededCommands = new HashSet<String>();
    if( pain1<0 ){
      neededCommands.add("G1");
    }
    if( pain2<0 ){
      neededCommands.add("G2");
    }
    if( !neededCommands.isEmpty() && !neededCommands.contains(cmd) ){
      return true;
    }
    return false;
  }

  public void command(String cmd) {
    if( cmd.equals("G1") ){
      pain1=0;
    }else if(Math.random()<0.2){
      pain1=-1;
    }

    if( cmd.equals("G2") ){
      pain2=0;
    }else if(Math.random()<0.2){
      pain2=-1;
    }
  }
}