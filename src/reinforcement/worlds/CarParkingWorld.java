package reinforcement.worlds;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;
import static java.lang.Math.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jul 20, 2009
 * Time: 10:55:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class CarParkingWorld implements RWorld<CarParkingState>{
  public static void main(String[] args){
    JPanel p = new CarParkingWorld().visualizer();

    JFrame f = new JFrame();
    f.add(p);
    f.pack();
    f.setVisible(true);
  }

  public void printStateMap(Map<CarParkingState, String> m){
  }

  // 'static' to survive multiple episodes of basically same setup
  static Map<CarParkingState,Double> initStateValues =
      new HashMap<CarParkingState,Double>();


  public double initStateValue(CarParkingState s) {
    return initStateValues.get(s);
  }


  public boolean isTerminal() {
    return intersects(carSide(), border) ||
        intersects(carSide(), leftWall) ||
        intersects(carSide(), rightWall) ||
        intersects(carSide(), targetMark);
  }

  public CarParkingState getS() {
    CarParkingState s =
        new CarParkingState((int)(carX0/5), (int)(carY0/5), (int)(carAngle/PI*100), prevAction);

    double ival = 500 - distToTarget() * 10;
    // must be only a scent, much below 1000, so real objective is bright when found
    if( isTerminal() ){
      ival=0;
      s = new CarParkingState(-1, -1, 0, "");
    }
    initStateValues.put(s, ival);
    // - must have steep enough gradient to overcome every step reward of (-1)

    return s;

  }

  double distToTarget(){
    double d=-1;
    for( Point p : carSide() ){
      for( Point pt : targetMark ){
        double dst = p.distance(pt);
        if( d==-1 ){
          d=dst;
        }else{
          d = Math.min(d,dst);
        }
      }
    }
    return d;
  }


  static int sleepPause=5;

  public double action(String a) {
    if( isTerminal() ){
      return 0;
    }
    try {
      Thread.sleep(sleepPause);
    } catch (InterruptedException e) {
    }

    double dstTarg0 = distToTarget();

    double steerAngle = Double.parseDouble(a.substring(1));
    if( a.startsWith("b") ){
      steerAngle += PI; // reverse
    }

    double targX = carX0 + carLen*cos(carAngle)+step*cos(carAngle+steerAngle);
    double targY = carY0 + carLen*sin(carAngle)+step*sin(carAngle+steerAngle);
    carAngle = atan2(targY - carY0, targX - carX0);
    carX0 += step*cos(steerAngle)*cos(carAngle);
    carY0 += step*cos(steerAngle)*sin(carAngle);
    boolean steerChange = !a.equals(prevAction);
    prevAction=a;

    if( visPanel!=null ){
      visPanel.getComponent(0).repaint();
    }
    if( intersects(carSide(), targetMark) ){
      return +1000;
    }
    if( isTerminal() ){
      return -1000;
    }
    //double dstTarg1 = distToTarget();
    //double targetApproachBonus = (dstTarg0 - dstTarg1)/step;
    if( steerChange ){
      return -5;
    }
    return -1;// + targetApproachBonus;
  }

  public List<String> actions() {
    List<String> r = new ArrayList<String>();
    for( double a = -10; a<=10; a+=1 ){
      r.add("f"+a/10);
      r.add("b"+a/10);
    }
    return r;
  }

  public void println() {
  }

  boolean intersects(Point[] pcar, Point[] p2){
    List<Line2D> p2Lines = toLines(p2);
    for( Line2D l1 : toLines(pcar) ){
      for( Line2D l2 : p2Lines ){
        if( l1.intersectsLine(l2) ){
          return true;
        }
      }
    }
    return false;
  }

  List<Line2D> toLines(Point[] p){
    List<Line2D> ret = new ArrayList<Line2D>();
    for( int i=0; i<p.length; i++ ){
      ret.add(new Line2D.Double(p[i],p[(i+1)%p.length]));
    }
    return ret;
  }

  JPanel visPanel;

  Point[] border = new Point[]{
      new Point(5,5),new Point(295,5),new Point(295,195),new Point(5,195)};
  Point[] leftWall = new Point[]{
      new Point(40,5),new Point(45,5),new Point(45,90),new Point(40,90)};
  Point[] rightWall = new Point[]{
      new Point(90,5),new Point(95,5),new Point(95,90),new Point(90,90)};
  Point[] targetMark = new Point[]{
      new Point(60,30),new Point(70,30),new Point(65,40)};


  double carX0=120;
  double carY0=80;
  double carAngle=-PI/6;
  String prevAction="";
  //double steerAngle=PI/14;
  double carLen=70;
  double carWidth=30;
  final int step=5;

  Point[] carSide(){
    ArrayList<Point> l = new ArrayList<Point>();
    l.add(new Point((int)carX0, (int)carY0));
    l.add(new Point(
        (int)(carX0 + carLen*cos(carAngle)),
        (int)(carY0 + carLen*sin(carAngle))));
    l.add(new Point(
        (int)(carX0 + carLen*cos(carAngle)+carWidth*cos(carAngle+PI/2)),
        (int)(carY0 + carLen*sin(carAngle)+carWidth*sin(carAngle+PI/2))));
    l.add(new Point(
        (int)(carX0 + carWidth*cos(carAngle+PI/2)),
        (int)(carY0 + carWidth*sin(carAngle+PI/2))));
    return l.toArray(new Point[l.size()]);
  }

  Point[] carDriver(){
    ArrayList<Point> l = new ArrayList<Point>();
    l.add(new Point(
        (int)(carX0 + (carLen-15)*cos(carAngle)+(carWidth-10)*cos(carAngle+PI/2)),
        (int)(carY0 + (carLen-15)*sin(carAngle)+(carWidth-10)*sin(carAngle+PI/2))));
    l.add(new Point(
        (int)(carX0 + (carLen-15)*cos(carAngle)+(carWidth-15)*cos(carAngle+PI/2)),
        (int)(carY0 + (carLen-15)*sin(carAngle)+(carWidth-15)*sin(carAngle+PI/2))));
    return l.toArray(new Point[l.size()]);
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
      paint(g, border);
      paint(g, leftWall);
      paint(g, rightWall);
      paint(g, carSide());
      paint(g, carDriver());
      paint(g, targetMark);
    }

  }
}
