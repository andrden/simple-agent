package reinforcement;

import com.pmstation.common.utils.MinMaxFinder;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jun 22, 2009
 * Time: 7:48:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class Sarsa {
  public static void main(String[] args){
    new Sarsa().doit();
  }

  double epsilon=0.1;
  double alpha=0.1;
  Map<StAct,Double> qval = new HashMap<StAct,Double>();
  int t=0;

  private void doit() {
    while(true){
      int t0=t;
      episode();
      System.out.print ln("episode end t="+t+" dt="+(t-t0));
    }
  }

  private void episode() {
    StochasticWind w = new StochasticWind();
    String s = w.getS();
    String a = policyAction(s);
    while(!w.isTerminal()){
      t++;
      double r = w.action(a);
      String s1 = w.getS();
      String a1 = policyAction(s1);
//      if( w.isTerminal() ){
//        a1="";
//      }

      // update Q
      StAct sa = new StAct(s,a);
      StAct sa1 = new StAct(s1,a1);
      Double q = qval.get(sa);
      if( q==null ){
        q=0d;
      }
      Double q1 = qval.get(sa1);
      if( q1==null ){
        q1=0d;
      }
      q = q + alpha*(r+q1-q);
      qval.put(sa, q);

      s=s1;
      a=a1;
    }
  }

  String policyAction(String s){
    if( Math.random()<epsilon ){
      return Utils.rnd(StochasticWind.actions);
    }

    MinMaxFinder mmf = new MinMaxFinder();
    for( String a : StochasticWind.actions ){
      Double val = qval.get(new StAct(s, a));
      if( val==null ){
        val=0d;
      }
      mmf.add(val, a);
    }
    List acts = mmf.getMaxNames();
    return (String)Utils.rnd(acts);
  }
}
