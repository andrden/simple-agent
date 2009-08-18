package reinforcement.worlds;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Aug 18, 2009
 * Time: 6:02:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class WCanvas extends Canvas {
  WCanvas() {
    setBackground(Color.LIGHT_GRAY); // to have update() called and flipping suppressed
  }

//    @Override
//    public void update(Graphics g) {
//    }

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
