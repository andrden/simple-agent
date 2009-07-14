package reinforcement;

import com.pmstation.common.utils.MinMaxFinder;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import utils.Utils;

public class DynaQPlus {
  public static void main(String[] args){
    new DynaQPlus().doit();
  }

  double epsilon=0.01;//0.01;//0.1;
  double alpha=0.1;
  double explorationBonusK = 0.017;
  boolean bonusOnCommand=false;
  boolean bonusOnPlan=true;

  Map<StAct,Double> qval = new HashMap<StAct,Double>();
  Map<StAct,Long> stActLastT = new HashMap<StAct,Long>();
  Model model = new Model();
  long t=0;
  int ep=0;
  List<String> actions;

  class Model{
    Map<StAct,String> nextSt = new HashMap<StAct,String>();
    Map<StAct,Double> rew = new HashMap<StAct,Double>();
    void update(StAct sa, String nextSt, double rew){
      String nextSt0 = this.nextSt.get(sa);
//      if( nextSt0!=null && !nextSt0.equals(nextSt) ){
//        throw new IllegalArgumentException("stochastic not yet supported");
//      }
      Double rew0 = this.rew.get(sa);
//      if( rew0!=null && !rew0.equals(rew) ){
//        throw new IllegalArgumentException("stochastic not yet supported");
//      }

      this.nextSt.put(sa, nextSt);
      this.rew.put(sa, rew);
    }
    StAct randomStAct(){
      if( nextSt.isEmpty() ){
        return null;
      }
      List<StAct> allStAct = new ArrayList<StAct>(nextSt.keySet());
      return allStAct.get((int)(Math.random() * allStAct.size()));
    }
    String nextSt(StAct sa){
      return nextSt.get(sa);
    }
    double rew(StAct sa){
      return rew.get(sa);
    }
  }

  private RWorld mkWorld() {
    //RWorld w = new StochasticWind();
    //RWorld w = new CliffWorld();
    RWorld w = new MazeWorld();
    actions = w.actions();
    return w;
  }

  private void doit() {
    while(true){
      long t0=t;
      double totalRew=episode();
      ep++;
      long dt = t - t0;
      System.out.println("episode end t="+t+" dt="+ dt
          +" ep="+ep+" totalRew="+totalRew);
      if(ep==1000){
        // DynaQ - t=19000-19200 ep=1000
        // DynaQPlusAct k=0.1 - t=22800-22900 ep=1000
        // DynaQPlusAct k=0.01 - t=18200-18400 ep=1000
        // DynaQPlusAct k=0.001 - t=18000-18400 ep=1000
        // DynaQPlusAct k=0.0000001 - t=18400-18600 ep=1000

        Utils.breakPoint();
      }
      if( dt<20 /*t/ep<20*/ ){
        // t=7000-9000 => dt<20 - qlearning
        // t=400-600 => dt<20 - DynaQ
        Utils.breakPoint();
        // t=195k-215k on SoftGreedy2 - QLearning
      }
      //printPolicy();
    }
  }

  private void printPolicy() {
    if( t>3000 ){
      Utils.breakPoint();
      for( int y=1; y<=6; y++ ){
        for( int x=1; x<=9; x++ ){
          String s = x+"_"+y;
          String a = (String) Utils.rnd(greedyActions(s));
          System.out.printf("%2s ",a);
        }
        System.out.println();
      }

      for( int y=1; y<=6; y++ ){
        for( int x=1; x<=9; x++ ){
          String s = x+"_"+y;
          double val = value(s);
          System.out.printf("%6.2f ",val);
        }
        System.out.println();
      }

    }
  }

  private double episode() {
    RWorld w = mkWorld();
    double totalRew=0;
    while(!w.isTerminal()){
      t++;
      String s = w.getS();
      String a = policyAction(s);
      StAct sa = new StAct(s,a);
      if( t>3000 ){
//        w.println();
//        System.out.println("a="+a+" greedy="+greedyActions(s));
      }
      stActLastT.put(sa, t);
      double r = w.action(a);
      totalRew+=r;
      String s1 = w.getS();

      // update Q
      qLearn(sa, r, s1);

      // update model
      model.update(sa, s1, r);

      // planning using model
      for( int i=0; i<50; i++ ){
        StAct rndSa = model.randomStAct();
        if( rndSa==null ){
          break;
        }

        double simulRew = model.rew(rndSa);
        if( bonusOnPlan ){
          Long told = stActLastT.get(rndSa);
          if( told!=null ){
            simulRew += explorationBonusK * Math.sqrt(t-told);
          }
        }
        qLearn(rndSa, simulRew, model.nextSt(rndSa));
      }
    }
    return totalRew;
  }

  private void qLearn(StAct sa, double r, String s1) {
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


  String policyAction(String s){
    //return epsilonGreedy(s);
    return softGreedy(s);
  }

  private String softGreedy(String s) {
    SoftGreedy2 sg = new SoftGreedy2(epsilon);
    for( String a : actions ){
      double actionQ = qvalGet(s, a);
      if( bonusOnCommand ){
        Long told = stActLastT.get(new StAct(s,a));
        if( told!=null ){
          actionQ += explorationBonusK * Math.sqrt(t-told);
        }
      }
      sg.put(a, actionQ);
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