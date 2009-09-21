package visual;

import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 14, 2009
 * Time: 2:14:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class A {
  public static void main(String[] args) throws Exception{
    new A().doit();
  }

  int irnd(int max){
    return (int)(Math.random()*max);
  }

  private void doit() throws Exception{
    BufferedImage bi = ImageIO.read(new File("C:\\proj\\cr6\\photo1.jpg"));
    LineIter li = new LineIter(irnd(bi.getWidth()), irnd(bi.getWidth()),
      irnd(bi.getHeight()), irnd(bi.getHeight()));
    for( int i=0; i<=li.size(); i++ ){
      int v = new Color(bi.getRGB(li.xAt(i), li.yAt(i))).getGreen();
      //System.out.println(v);
      printV(v);
    }

    BufferedImage bres = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
    //bres.getGraphics().drawLine(li.x1, li.y1, li.x2, li.y2);
    for( int i=0; i<300; i++ ){
      int x = irnd(bi.getWidth());
      int y = irnd(bi.getWidth());
      Color ca = colorAvg(bi, x, y, 300);
      if( ca!=null ){
        Graphics g = bres.getGraphics();
        g.setColor(ca);
        g.fillOval(x,y,100,100);
      }
    }
    ImageIO.write(bres, "jpeg", new File("C:\\proj\\cr6\\photo1-out.jpg"));
  }

  Color colorAvg(BufferedImage bi, int x, int y, int radius){
    ColorAvg ca = new ColorAvg();
    for( int i=0; i<200; i++ ){
      int xi = x + irnd(radius)*2 - radius;
      int yi = y + irnd(radius)*2 - radius;
      if( xi<0 || xi>=bi.getWidth() || yi<0 || yi>=bi.getHeight() ){
        continue;
      }
      ca.add(new Color(bi.getRGB(xi, yi)));
    }
    return ca.get();
  }

  class ColorAvg{
    int count;
    long r;
    long g;
    long b;
    void add(Color c){
      count++;
      r+=c.getRed();
      g+=c.getGreen();
      b+=c.getBlue();
    }
    Color get(){
      if( count<1 ){
        return null;
      }
      return new Color((int)(r/count), (int)(g/count), (int)(b/count));
    }
  }

  void printV(int v){
    for( int i=0; i<v/16; i++ ){
      System.out.print('.');
    }
    System.out.println();
  }

  class LineIter{
    int x1,x2,y1,y2;
    boolean xmain;

    LineIter(int x1, int x2, int y1, int y2) {
      if( x1>x2 ){
        int x=x2;
        x2=x1;
        x1=x;
      }
      if( y1>y2 ){
        int y=y2;
        y2=y1;
        y1=y;
      }
      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      xmain = Math.abs(x1-x2) >= Math.abs(y1-y2);
    }

    int xAt(int i){
      if( xmain ){
        return x1+i;
      }
      return x1 + i*(x2-x1)/(y2-y1);
    }
    int yAt(int i){
      if( !xmain ){
        return y1+i;
      }
      return y1 + i*(y2-y1)/(x2-x1);
    }

    int size(){
      return Math.max(Math.abs(x1-x2), Math.abs(y1-y2));
    }
  }
}
