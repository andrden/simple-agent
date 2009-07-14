package reinforcement;

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
public class MazeWorld implements RWorld{
  private String s="4_6";
  static long step=0;
  public boolean isTerminal(){
    return s.equals("9_1");
  }

  public List<String> actions(){
    return Arrays.asList("n","e","s","w");
  }

  public String getS() {
    return s;
  }

  public double action(String a){
    step++;
    StringTokenizer st = new StringTokenizer(s,"_");
    long x = Long.parseLong(st.nextToken());
    long y = Long.parseLong(st.nextToken());
    long x0=x;
    long y0=y;

    if(a.indexOf("n")!=-1) y--;
    if(a.indexOf("s")!=-1) y++;
    if(a.indexOf("e")!=-1) x++;
    if(a.indexOf("w")!=-1) x--;

    if( x>9 ){
      x=9;
    }
    if( x<1 ){
      x=1;
    }
    if( y>6 ){
      y=6;
    }
    if( y<1 ){
      y=1;
    }

    //if( y==4 && (step<=3000 && x>=2 || step>3000 && x<=8) ){
    //if( y==4 && x>=2 ){
    if( y==4 && x>=2 && (step<=3000 || x<=8) ){
      // barrier in the maze
      x=x0;
      y=y0;
    }


    int ret=-1;
    s = x + "_" + y;
    return ret;
  }

  public void println(){
    StringTokenizer st = new StringTokenizer(s,"_");
    long sx = Long.parseLong(st.nextToken());
    long sy = Long.parseLong(st.nextToken());
    for( int y=1; y<=6; y++ ){
      for( int x=1; x<=9; x++ ){
        char c = '.';
        if( x==6 && y==4 ) c='S';
        if( x==9 && y==1 ) c='F';
        if( x==sx && y==sy ) c='*';
        System.out.print(c);
      }
      System.out.println();
    }
  }
}