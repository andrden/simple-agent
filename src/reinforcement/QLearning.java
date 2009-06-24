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
public class QLearning {
  public static void main(String[] args){
    new QLearning().doit();
  }

  double epsilon=0.1;
  double alpha=0.1;
  Map<StAct,Double> qval = new HashMap<StAct,Double>();
  int t=0;
  int ep=0;
  List<String> actions;

  private RWorld mkWorld() {
    RWorld w = new StochasticWind();
    actions = w.actions();
    return w;
  }

  private void doit() {
    while(true){
      int t0=t;
      episode();
      ep++;
      int dt = t - t0;
      System.out.println("episode end t="+t+" dt="+ dt +" ep="+ep);
      if( t/ep<20 ){
        Utils.breakPoint();
        // t=195k-215k on SoftGreedy2 - QLearning
      }
      printPolicy();
    }
  }

  private void printPolicy() {
    if( ep>200000 ){
      for( int y=1; y<=7; y++ ){
        for( int x=1; x<=10; x++ ){
          String s = x+"_"+y;
          String a = (String) Utils.rnd(greedyActions(s));
          System.out.printf("%2s ",a);
        }
        System.out.println();
      }

      for( int y=1; y<=7; y++ ){
        for( int x=1; x<=10; x++ ){
          String s = x+"_"+y;
          double val = value(s);
          System.out.printf("%6.2f ",val);
        }
        System.out.println();
      }

    }
  }

  private void episode() {
    RWorld w = mkWorld();
    while(!w.isTerminal()){
      t++;
      String s = w.getS();
      String a = policyAction(s);
//      if( ep>5000 ){
//        w.println();
//        System.out.println("a="+a+" greedy="+greedyActions(s));
//      }
      double r = w.action(a);
      String s1 = w.getS();

      // update Q
      StAct sa = new StAct(s,a);
      Double q = qval.get(sa);
      if( q==null ){
        q=0d;
      }
      MinMaxFinder mmfQ = new MinMaxFinder();
      for( String a1 : actions ){
        double q1 = qvalGet(s1, a1);
        mmfQ.add(q1, "");
      }
      q = q + alpha*(r+mmfQ.getMaxVal()-q);
      qval.put(sa, q);
    }
  }


  String policyAction(String s){
    //return epsilonGreedy(s);
    return softGreedy(s);
  }

  private String softGreedy(String s) {
    SoftGreedy2 sg = new SoftGreedy2(epsilon);
    for( String a : actions ){
      sg.put(a, qvalGet(s,a));
    }
    return sg.policyAction();
  }

  private String epsilonGreedy(String s) {
    if( Math.random()<epsilon ){
      return Utils.rnd(actions);
    }

    return (String) Utils.rnd(greedyActions(s));
  }

  private List greedyActions(String s) {
    MinMaxFinder mmf = new MinMaxFinder();
    for( String a : actions ){
      Double val = qvalGet(s, a);
      mmf.add(val, a);
    }
    List acts = mmf.getMaxNames();
    return acts;
  }

  private double qvalGet(String s, String a) {
    Double val = qval.get(new StAct(s, a));
    if( val==null ){
      val=0d;
    }
    return val;
  }

  private double value(String s) {
    MinMaxFinder mmf = new MinMaxFinder();
    for( String a : actions ){
      Double val = qvalGet(s, a);
      mmf.add(val, a);
    }
    return mmf.getMaxVal();
  }
}