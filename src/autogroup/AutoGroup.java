package autogroup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 17 лист 2008
 * Time: 14:53:15
 */
public class AutoGroup {
  public static void main(String[] args) throws Exception{
    new AutoGroup().doit();
  }

  Grper1 g1 = new Grper1();
  Grper g = new Grper();


  void doit() throws Exception{
    BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("log1.properties")));
    String s;
    while(  (s=br.readLine())!=null ){
      s = s.trim();
      if( s.length()>0 ){
        next(s);
      }
    }

    br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("log2.properties")));
    while(  (s=br.readLine())!=null ){
      s = s.trim();
      if( s.length()>0 ){
        next(s.substring(33));
      }
    }

    System.nanoTime();
  }

  void next(final String s){
    g1.next(s);
    g.next(s);
  }
}
