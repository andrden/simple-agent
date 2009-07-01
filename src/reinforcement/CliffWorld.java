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
public class CliffWorld implements RWorld{
  private String s="1_4";
  public boolean isTerminal(){
    return s.equals("12_4");
  }

  public List<String> actions(){
    return Arrays.asList("n","e","s","w");
  }

  public String getS() {
    return s;
  }

  public double action(String a){
    StringTokenizer st = new StringTokenizer(s,"_");
    long x = Long.parseLong(st.nextToken());
    long y = Long.parseLong(st.nextToken());

    if(a.indexOf("n")!=-1) y--;
    if(a.indexOf("s")!=-1) y++;
    if(a.indexOf("e")!=-1) x++;
    if(a.indexOf("w")!=-1) x--;

    if( x>12 ){
      x=12;
    }
    if( x<1 ){
      x=1;
    }
    if( y>4 ){
      y=4;
    }
    if( y<1 ){
      y=1;
    }
    int ret=-1;
    if( y==4 && x>1 && x<12 ){
      x=1;
      ret=-100;
    }
    s = x + "_" + y;
    return ret;
  }

  public void println(){
    StringTokenizer st = new StringTokenizer(s,"_");
    long sx = Long.parseLong(st.nextToken());
    long sy = Long.parseLong(st.nextToken());
    for( int y=1; y<=4; y++ ){
      for( int x=1; x<=12; x++ ){
        char c = '.';
        if( x==1 && y==4 ) c='S';
        if( x==12 && y==4 ) c='F';
        if( x==sx && y==sy ) c='*';
        System.out.print(c);
      }
      System.out.println();
    }
  }
}