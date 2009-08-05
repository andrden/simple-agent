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

 Variant without requirement for minimum turns solved by rude force:
 episode end t=9530 dt=17 ep=132 totalRew=-1016.0 qvals>100=3997
 episode end t=9557 dt=27 ep=133 totalRew=-1026.0 qvals>100=4001
 episode end t=9595 dt=38 ep=134 totalRew=-1037.0 qvals>100=4016
 episode end t=9634 dt=39 ep=135 totalRew=962.0 qvals>100=4033
 episode end t=9668 dt=34 ep=136 totalRew=-1033.0 qvals>100=4048
 episode end t=9714 dt=46 ep=137 totalRew=-1045.0 qvals>100=4069
 episode end t=9741 dt=27 ep=138 totalRew=-1026.0 qvals>100=4073
 episode end t=9793 dt=52 ep=139 totalRew=949.0 qvals>100=4096
 episode end t=9818 dt=25 ep=140 totalRew=976.0 qvals>100=4096
 episode end t=9847 dt=29 ep=141 totalRew=972.0 qvals>100=4098
 episode end t=9872 dt=25 ep=142 totalRew=976.0 qvals>100=4099
 episode end t=9902 dt=30 ep=143 totalRew=971.0 qvals>100=4105
 episode end t=9927 dt=25 ep=144 totalRew=976.0 qvals>100=4105

 solution:  3232332433454445554454
 24 16 0 a=3.0 >> 23 16 0
 23 16 0 a=2.0 >> 22 17 0
 22 17 0 a=3.0 >> 21 17 0
 21 17 0 a=2.0 >> 21 18 0
 21 18 0 a=3.0 >> 20 18 0
 20 18 0 a=3.0 >> 19 18 0
 19 18 0 a=2.0 >> 18 19 0
 18 19 0 a=4.0 >> 18 18 0
 18 18 0 a=3.0 >> 17 18 0
 17 18 0 a=3.0 >> 16 18 0
 16 18 0 a=4.0 >> 15 18 0
 15 18 0 a=5.0 >> 15 17 0
 15 17 0 a=4.0 >> 15 16 0
 15 16 0 a=4.0 >> 14 15 0
 14 15 0 a=4.0 >> 13 14 0
 13 14 0 a=5.0 >> 14 13 0
 14 13 0 a=5.0 >> 14 12 0
 14 12 0 a=5.0 >> 14 11 0
 14 11 0 a=5.0 >> 14 10 0
 14 10 0 a=4.0 >> 14 10 0
 14 10 0 a=4.0 >> 13 9 0
 13 9 0 a=5.0 >> 13 8 0
 13 8 0 a=4.0 >> -1 -1 0


 Variant with minimum turns brute force:
 episode end t=39016 dt=26 ep=475 totalRew=-1077.0 qvals>100=25317
 episode end t=39036 dt=20 ep=476 totalRew=-1047.0 qvals>100=25320
 episode end t=39066 dt=30 ep=477 totalRew=-1081.0 qvals>100=25331
 episode end t=39135 dt=69 ep=478 totalRew=-1260.0 qvals>100=25377
 episode end t=39176 dt=41 ep=479 totalRew=-1148.0 qvals>100=25399
 episode end t=39216 dt=40 ep=480 totalRew=881.0 qvals>100=25420
 episode end t=39249 dt=33 ep=481 totalRew=-1104.0 qvals>100=25435
 episode end t=39310 dt=61 ep=482 totalRew=-1216.0 qvals>100=25476
 episode end t=39337 dt=27 ep=483 totalRew=-1070.0 qvals>100=25485
 episode end t=39366 dt=29 ep=484 totalRew=-1080.0 qvals>100=25495
 episode end t=39388 dt=22 ep=485 totalRew=-1049.0 qvals>100=25499
 episode end t=39444 dt=56 ep=486 totalRew=-1195.0 qvals>100=25530
 episode end t=39604 dt=160 ep=487 totalRew=357.0 qvals>100=25647
 episode end t=39632 dt=28 ep=488 totalRew=917.0 qvals>100=25656
 episode end t=39659 dt=27 ep=489 totalRew=-1070.0 qvals>100=25663
 episode end t=39680 dt=21 ep=490 totalRew=-1044.0 qvals>100=25663
 episode end t=39702 dt=22 ep=491 totalRew=-1049.0 qvals>100=25666
 episode end t=39725 dt=23 ep=492 totalRew=-1050.0 qvals>100=25669
 episode end t=39748 dt=23 ep=493 totalRew=954.0 qvals>100=25671
 episode end t=39771 dt=23 ep=494 totalRew=-1050.0 qvals>100=25674
 episode end t=39794 dt=23 ep=495 totalRew=-1050.0 qvals>100=25675
 episode end t=39817 dt=23 ep=496 totalRew=-1050.0 qvals>100=25678
 episode end t=39845 dt=28 ep=497 totalRew=925.0 qvals>100=25685
 episode end t=39880 dt=35 ep=498 totalRew=-1098.0 qvals>100=25698
 episode end t=39905 dt=25 ep=499 totalRew=-1048.0 qvals>100=25700
 episode end t=39935 dt=30 ep=500 totalRew=-1073.0 qvals>100=25707
 episode end t=39988 dt=53 ep=501 totalRew=832.0 qvals>100=25738
 episode end t=40024 dt=36 ep=502 totalRew=893.0 qvals>100=25748
 episode end t=40049 dt=25 ep=503 totalRew=-1060.0 qvals>100=25752
 episode end t=40074 dt=25 ep=504 totalRew=944.0 qvals>100=25755
 episode end t=40101 dt=27 ep=505 totalRew=-1066.0 qvals>100=25761
 episode end t=40130 dt=29 ep=506 totalRew=-1076.0 qvals>100=25769
 episode end t=40158 dt=28 ep=507 totalRew=933.0 qvals>100=25775
 episode end t=40199 dt=41 ep=508 totalRew=-1128.0 qvals>100=25792
 episode end t=40238 dt=39 ep=509 totalRew=-1118.0 qvals>100=25808
 episode end t=40261 dt=23 ep=510 totalRew=954.0 qvals>100=25808
 episode end t=40284 dt=23 ep=511 totalRew=954.0 qvals>100=25807
 episode end t=40307 dt=23 ep=512 totalRew=954.0 qvals>100=25807
 episode end t=40330 dt=23 ep=513 totalRew=954.0 qvals>100=25807
 episode end t=40353 dt=23 ep=514 totalRew=954.0 qvals>100=25805
 episode end t=40376 dt=23 ep=515 totalRew=954.0 qvals>100=25804
 episode end t=40399 dt=23 ep=516 totalRew=954.0 qvals>100=25804
 episode end t=40422 dt=23 ep=517 totalRew=954.0 qvals>100=25804

 solution with minimum turns: 33223333334445555555444
 24 16 0 a=3.0 >> 23 16 3.0
 23 16 3.0 a=3.0 >> 22 16 3.0
 22 16 3.0 a=2.0 >> 21 17 2.0
 21 17 2.0 a=2.0 >> 21 18 2.0
 21 18 2.0 a=3.0 >> 20 18 3.0
 20 18 3.0 a=3.0 >> 19 18 3.0
 19 18 3.0 a=3.0 >> 18 18 3.0
 18 18 3.0 a=3.0 >> 17 18 3.0
 17 18 3.0 a=3.0 >> 16 18 3.0
 16 18 3.0 a=3.0 >> 15 18 3.0
 15 18 3.0 a=4.0 >> 14 18 4.0
 14 18 4.0 a=4.0 >> 13 17 4.0
 13 17 4.0 a=4.0 >> 13 16 4.0
 13 16 4.0 a=5.0 >> 13 15 5.0
 13 15 5.0 a=5.0 >> 13 14 5.0
 13 14 5.0 a=5.0 >> 14 13 5.0
 14 13 5.0 a=5.0 >> 14 12 5.0
 14 12 5.0 a=5.0 >> 14 11 5.0
 14 11 5.0 a=5.0 >> 14 10 5.0
 14 10 5.0 a=5.0 >> 15 9 5.0
 15 9 5.0 a=4.0 >> 14 9 4.0
 14 9 4.0 a=4.0 >> 13 8 4.0
 13 8 4.0 a=4.0 >> -1 -1 0

 3232332433454445554454 vs
 33223333334445555555444

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
  String prevAction="0";

  JToggleButton pauseBtn = new JToggleButton("pause");

  // 'static' to survive multiple episodes of basically same setup 
  static Map<BallParkingState,Double> initStateValues =
      new HashMap<BallParkingState,Double>();

  public BallParkingWorld() {
  }

  public boolean isTerminal() {
    return intersects(carSide(), border) ||
        intersects(carSide(), leftWall) ||
        intersects(carSide(), rightWall) ||
        intersects(carSide(), targetMark);
  }

  public BallParkingState getS() {
    BallParkingState s = new BallParkingState((int)(carX0/5), (int)(carY0/5), prevAction);

    double ival = 1000 - distToTarget() * 2;
    if( isTerminal() ){
      ival=0;
      s = new BallParkingState(-1, -1, "0");
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
        Thread.sleep(10);
      } catch (InterruptedException e) {
      }
      if( !pauseBtn.isSelected() ){
        break;
      }
    }

    double steerAngle = Double.parseDouble(a);

    carX0 += step*cos(steerAngle);
    carY0 += step*sin(steerAngle);
    boolean angleChange = !prevAction.equals(a);
    //prevAction=a;

    if( visPanel!=null ){
      visPanel.getComponent(0).repaint();
    }
    if( intersects(carSide(), targetMark) ){
      return +1000;
    }
    if( isTerminal() ){
      return -1000;
    }
    if( angleChange ){
      return -5;
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
        BallParkingState s = new BallParkingState(x, y, "0");
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