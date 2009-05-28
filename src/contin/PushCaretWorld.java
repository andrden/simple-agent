package contin;

import intf.World;

import java.util.*;

import utils.Utils;

/**
 * Caret pushed by telescopic rotating arm
 */
public class PushCaretWorld implements World {
  double lastCmdRes=0;
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
    return Arrays.asList("U","P");  // up, push
  }

  public Collection<String> targetSensors() {
    return Collections.singleton("$");
  }

  public Map<String, Object> view() {
    Map<String,Object> m = new HashMap<String,Object>();
    m.put("r", rotate);
    m.put("a", arm);
    m.put("$", lastCmdRes);
    return m;
  }

  public void command(String cmd) {
    lastCmdRes = (int)(10*command(cmd, 1));
  }

  /**
   *
   * @param cmd
   * @param force -1..+1
   * @return
   */
  public double command(String cmd, double force) {
    if( cmd.equals("U") ){
      rotate += force;
      rotate = Math.max(rotate, minRotate());
      rotate = Math.min(rotate, 1);
      return 0;
    }else if( cmd.equals("P") ){
      double newArm = arm+force;
      newArm = Math.max(0, newArm);
      newArm = Math.min(1, newArm);
      if( newArm>maxArm() ){
        double touchDist = Math.tan(Math.PI/2*rotate)*height;
        double newDist = Math.sqrt(Math.pow(newArm,2)-Math.pow(height,2));
        arm=newArm;
        rotate=minRotate();
        return newDist-touchDist;
      }else{
        arm=newArm;
        return 0;
      }
    }
    return 0;
  }

  public static void main(String[] args){
    //printViewSpace();

    List<Hist> history = new ArrayList<Hist>();
    PushCaretWorld w = new PushCaretWorld();
    Strategy[] strs = {new RptGood(), new Rnd()};
    for( int i=0; i<50; i++ ){
      String cmd = null;
      double force = 0;
      for( Strategy s : strs ){
        if( s.activity>1 ){
          s.exec();
          cmd = s.cmd;
          force = s.force;
          break;
        }
      }

      double res = w.command(cmd, force);
      Hist h = new Hist(cmd,force,res,w.view());
      history.add(h);
      for( Strategy s : strs ){
        s.update(h);
      }
      System.out.printf("%s %5.2f %.2f %s",cmd,force,res,""+w.view());
      System.out.println();
    }
  }

  abstract static class Strategy{
    String cmd;
    double force;
    double activity=0;
    abstract void exec();
    abstract void update(Hist last);
  }

  static class Rnd extends Strategy{
    Rnd(){
      activity=2;
    }
    void exec() {
      cmd = Utils.rnd(Arrays.asList("U", "P"));
      force = Math.random() * 2 - 1;
    }
    void update(Hist last) {
    }
  }

  static class RptGood extends Strategy{
    void exec() {
    }
    void update(Hist last) {
      if( last.res>0 ){
        cmd=last.cmd;
        force=last.force;
        activity=3;
      }else{
        activity/=2;
      }
    }
  }


  static class Hist{
    String cmd;
    double force;
    double res;
    Map<String,Object> view;

    Hist(String cmd, double force, double res, Map<String, Object> view) {
      this.cmd = cmd;
      this.force = force;
      this.res = res;
      this.view = view;
    }
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
