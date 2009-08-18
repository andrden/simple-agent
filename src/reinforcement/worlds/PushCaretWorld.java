package reinforcement.worlds;

import intf.World;

import java.util.*;
import java.util.List;
import java.awt.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.abs;

import utils.Utils;

import javax.swing.*;

/**
 * Caret pushed by telescopic rotating arm
 */
public class PushCaretWorld implements RWorld<PushCaretState>{
  private JPanel visPanel;

  public boolean isTerminal() {
    return false;
  }

  public PushCaretState getS() {
    return new PushCaretState( (int)(rotate*10), (int)(arm*10) );
  }

  static int sleepPause=50;

  public double action(String a) {
    try {
      Thread.sleep(sleepPause);
    } catch (InterruptedException e) {
    }

    try{
      return actionAct(a);
    }finally{
      if( visPanel!=null ){
        visPanel.getComponent(0).repaint();
      }
    }
  }

  private double actionAct(String a) {
    double force = Double.parseDouble(a.substring(1)); // -1..1
    if( a.charAt(0)=='U' ){
      rotate += force;
      rotate = Math.max(rotate, minRotate());
      rotate = Math.min(rotate, 1);
      return -0.1 - abs(force)/10;
    }else if( a.charAt(0)=='P' ){
      double newArm = arm+force;
      newArm = Math.max(0, newArm);
      newArm = Math.min(1, newArm);
      if( newArm>maxArm() ){
        double touchDist = Math.tan(Math.PI/2*rotate)*height;
        double newDist = Math.sqrt(Math.pow(newArm,2)-Math.pow(height,2));
        arm=newArm;
        rotate=minRotate();
        double shift = newDist - touchDist;
        totalShift += shift;
        return shift - abs(force)/10;
      }else{
        arm=newArm;
        return -0.1 - abs(force)/10;
      }
    }
    throw new UnsupportedOperationException(a);
  }

  public double initStateValue(PushCaretState s) {
    return 0;
  }

  public List<String> actions() {
    List<String> r = new ArrayList<String>();
    for( double a = -10; a<=10; a+=1 ){
      r.add("U"+a/10);
      r.add("P"+a/10);
    }
    return r;
  }

  public void println() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void printStateMap(Map<PushCaretState, String> m) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public JPanel visualizer() {
    JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    CarCanvas c = new CarCanvas();
    p.add(c);
    p.setPreferredSize(new Dimension(300,200));
    visPanel=p;
    return p;
  }

  class CarCanvas extends WCanvas{

    @Override
    public void paint(Graphics g) {
      int left=(50+(int)totalShift)%300;
      int SIZE=30;
      paint(g, new Point[]{
          new Point(left,2),
          new Point(left+SIZE,2),
          new Point(left+SIZE,2+SIZE),
          new Point(left,2+SIZE),
      });
      paint(g, new Point[]{
          new Point(left-2,2+SIZE),
          new Point(left-2 - (int)(Math.sqrt(2)*arm*SIZE*sin(Math.PI/2*rotate)),
              2+SIZE - (int)(Math.sqrt(2)*arm*SIZE*cos(Math.PI/2*rotate))),
      });
    }

  }

  double lastCmdRes=0;
  double rotate=0.9; // 0 is down, 1 is up, horizontal
  double arm=0; // 0..1
  final double height = Math.sqrt(2)/2; // let arm is fixed at this height
  // arm of length 1 touches ground at rotate=0.5

  double totalShift=0;

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



}