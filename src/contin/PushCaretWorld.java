package contin;

import intf.World;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Caret pushed by telescopic rotating arm
 */
public class PushCaretWorld implements World {
  double rotate=0.9; // 0 is down, 1 is up, horizontal
  double arm=0; // 0..1
  final double height = Math.sqrt(2)/2; // let arm is fixed at this height
  // arm of length 1 touches ground at rotate=0.5

  double maxArm(){
    if( rotate>0.99 ){
      return 1;
    }
    double max = height / Math.cos(Math.PI/2*rotate);
    if( max>1 ){
      max=1;
    }
    return max;
  }

  double minRotate(){
    if( arm <= height ){
      return 0;
    }
    return Math.acos(height/arm)/(Math.PI/2);
  }

  public List<String> commands() {
    return Arrays.asList("U","P");
  }

  public Map<String, Object> view() {
    Map<String,Object> m = new HashMap<String,Object>();
    m.put("r", rotate);
    m.put("a", arm);
    return m;
  }

  public int command(String cmd) {
    return command(cmd, 1);
  }

  /**
   *
   * @param cmd
   * @param force -1..+1
   * @return
   */
  public int command(String cmd, double force) {
    todo
  }

  public static void main(String[] args){
    printViewSpace();
  }

  private static void printViewSpace() {
    for( double a=0; a<=1; a+=0.1 ){
      for( double r=0; r<=1; r+=0.1 ){
        PushCaretWorld w = new PushCaretWorld();
        w.arm=a;
        w.rotate=r;
        //if( w.maxArm()<a ){
        if( w.minRotate()>r ){
          System.out.print("-");
        }else{
          System.out.print(".");
        }
      }
      System.out.println();
    }
    /*

...........
...........
...........
...........
...........
...........
...........
...........
----.......
-----......
-----......

     */
  }
}
