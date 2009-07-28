package reinforcement.worlds;

import com.pmstation.common.utils.MinMaxFinder;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;
import static java.lang.Math.*;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jul 20, 2009
 * Time: 10:55:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class BallParkingWorld implements RWorld<BallParkingState>{
  public static void main(String[] args){
    JPanel p = new BallParkingWorld().visualizer();

    JFrame f = new JFrame();
    f.add(p);
    f.pack();
    f.setVisible(true);
  }

  BallPanel visPanel;

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
  final int step=5;

  JToggleButton pauseBtn = new JToggleButton("pause");

  // 'static' to survive multiple episodes of basically same setup 
  static Map<BallParkingState,Double> initStateValues =
      new HashMap<BallParkingState,Double>();

  public BallParkingWorld() {
  }

  public boolean isTerminal() {
    return intersects(carSide(), border) ||
        intersects(carSide(), leftWall) ||
        //intersects(carSide(), rightWall) ||
        intersects(carSide(), targetMark);
  }

  public BallParkingState getS() {
    BallParkingState s = new BallParkingState((int)(carX0/5), (int)(carY0/5));

    double ival = 1000 - distToTarget() * 2;
    if( isTerminal() ){
      ival=0;
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

  public double action(String a) {
    if( isTerminal() ){
      return 0;
    }
    for(;;){
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
      if( !pauseBtn.isSelected() ){
        break;
      }
    }

    double steerAngle = Double.parseDouble(a);

    carX0 += step*cos(steerAngle);
    carY0 += step*sin(steerAngle);

    if( visPanel!=null ){
      visPanel.getComponent(0).repaint();
    }
    if( intersects(carSide(), targetMark) ){
      return +1000;
    }
    if( isTerminal() ){
      return -1000;
    }
    return -1;
  }

  public double initStateValue(BallParkingState s) {
    return initStateValues.get(s);
  }


  public List<String> actions() {
    List<String> r = new ArrayList<String>();
//    for( double a = 0; a<2*PI*4; a++ ){
//      r.add(""+a/4);
//    }
//    r.add(""+0);
//    r.add(""+Math.round(PI/2*1000)/1000.);
//    r.add(""+Math.round(PI*1000)/1000.);
//    r.add(""+Math.round(3*PI/2*1000)/1000.);

    for( double a = 0; a<2*PI; a++ ){
      r.add(""+a);
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


  Point[] carSide(){
    ArrayList<Point> l = new ArrayList<Point>();
    l.add(new Point((int)carX0, (int)carY0));
    l.add(new Point((int)carX0+6, (int)carY0));
    l.add(new Point((int)carX0+3, (int)carY0+3));
    return l.toArray(new Point[l.size()]);
  }


  public JPanel visualizer() {
    BallPanel p = new BallPanel();
    p.setLayout(new BorderLayout());
    CarCanvas c = new CarCanvas();
    c.setPreferredSize(new Dimension(300,200));
    p.add(c);
    JPanel pright = new JPanel();
    pright.setLayout(new GridLayout(5,1));
    pright.add(pauseBtn);
    p.add(pright, BorderLayout.EAST);
    visPanel=p;
    return p;
  }

  class BallPanel extends JPanel{

  }

  class CarCanvas extends Canvas{
    CarCanvas() {
      setBackground(Color.LIGHT_GRAY); // to have update() called and flipping suppressed
    }

//    @Override
//    public void update(Graphics g) {
//    }

    @Override
    public void paint(Graphics g) {
      paint(g, border);
      paint(g, leftWall);
      paint(g, rightWall);
      paint(g, carSide());
      paint(g, targetMark);
    }

    void paint(Graphics g, Point[] p){
      g.setColor(Color.YELLOW);
      int[] xx = new int[p.length+1];
      int[] yy = new int[p.length+1];
      for( int i=0; i<p.length+1; i++ ){
        xx[i] = screenX(p[i==p.length ? 0 : i]);
        yy[i] = screenY(p[i==p.length ? 0 : i]);
        //g.drawOval(xx[i],yy[i],3,3);
      }
      g.drawPolyline(xx,yy,p.length+1);
    }

    int screenX(Point p){
      return p.x;
    }
    int screenY(Point p){
      return 200-p.y;
    }
  }

  public void printStateMap(Map<BallParkingState, String> m){
    MinMaxFinder xlimit = new MinMaxFinder();
    MinMaxFinder ylimit = new MinMaxFinder();
    for( BallParkingState s : m.keySet() ){
      xlimit.add(s.x, "");
      ylimit.add(s.y, "");
    }
    StringBuilder sb = new StringBuilder();
    sb.append("<table border>");
    for( int y=(int)ylimit.getMaxVal(); y>=ylimit.getMinVal(); y-- ){
      sb.append("\n<tr>");
      for( int x=(int)xlimit.getMinVal(); x<=xlimit.getMaxVal(); x++ ){
        BallParkingState s = new BallParkingState(x, y);
        sb.append("\n  <td title='"+s+"'>");
        String v = m.get(s);
        if( v==null ){
          //v = initStateValue(s)+"*";
        }
        sb.append(v);
        sb.append("</td>");
      }
      sb.append("</tr>");
    }
    sb.append("</table");

    try {
      Writer w = new FileWriter("d:/tmp/state.html");
      w.write(sb.toString());
      w.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}