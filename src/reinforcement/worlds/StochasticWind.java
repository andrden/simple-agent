package reinforcement.worlds;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jun 22, 2009
 * Time: 7:47:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class StochasticWind implements RWorld{
  private String s="1_4";
  public boolean isTerminal(){
    return s.equals("8_4");
  }

  public List<String> actions(){
    return Arrays.asList("n","ne","e","se","s","sw","w","nw");
  }

  public JPanel visualizer() {
    return null;
  }

  public String getS() {
    return s;
  }

  int wind(int x){
    return (new int[]{0, 0,0,0,1,1, 1,2,2,1,0})[x];
  }

  public double action(String a){
    StringTokenizer st = new StringTokenizer(s,"_");
    long x = Long.parseLong(st.nextToken());
    long y = Long.parseLong(st.nextToken());

    int wi = wind((int)x);
    if(a.indexOf("n")!=-1) y--;
    if(a.indexOf("s")!=-1) y++;
    if(a.indexOf("e")!=-1) x++;
    if(a.indexOf("w")!=-1) x--;
    if( wi!=0 ){
      y-=wi;
      switch( (int)(Math.random()*3) ){
        case 0:
          y--;
          break;
        case 1:
          y++;
          break;
      }
    }

    if( x>10 ){
      x=10;
    }
    if( x<1 ){
      x=1;
    }
    if( y>7 ){
      y=7;
    }
    if( y<1 ){
      y=1;
    }
    s = x + "_" + y;
    return -1;
  }

  public void println(){
    StringTokenizer st = new StringTokenizer(s,"_");
    long sx = Long.parseLong(st.nextToken());
    long sy = Long.parseLong(st.nextToken());
    for( int y=1; y<=7; y++ ){
      for( int x=1; x<=10; x++ ){
        char c = '.';
        if( x==1 && y==4 ) c='S';
        if( x==8 && y==4 ) c='F';
        if( x==sx && y==sy ) c='*';
        System.out.print(c);
      }
      System.out.println();
    }
  }
}
