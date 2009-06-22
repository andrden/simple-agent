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
public class StochasticWind {
  static List<String> actions = Arrays.asList("n","ne","e","se","s","sw","w","nw");
  private String s="1_4";
  boolean isTerminal(){
    return s.equals("8_4");
  }

  public String getS() {
    return s;
  }

  int wind(int x){
    return (new int[]{0, 0,0,0,1,1, 1,2,2,1,0})[x];
  }

  double action(String a){
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
}
