package reinforcement;

import com.pmstation.common.utils.MinMaxFinder;
import com.pmstation.common.utils.CountingMap;

import java.util.*;
import java.util.List;
import java.awt.*;

import utils.Utils;
import reinforcement.worlds.*;

import javax.swing.*;

public class DynaQPlus {
  public static void main(String[] args){
    new DynaQPlus().doit();
  }

  /*
  // params for MazeWorld
  double epsilon=0.01;//0.01;//0.1;
  double alpha=0.1;
  double explorationBonusK = 0.017;
  boolean bonusOnCommand=false;
  boolean bonusOnPlan=true;
  */

  // params for CarParkingWorld
  double epsilon=0.01;
  double alpha=0.1;
  double explorationBonusK = 0.0;
  boolean bonusOnCommand=false;
  boolean bonusOnPlan=false;

  RWorld myWorld;
  Set<RState> statesVisited = new HashSet<RState>();
  Map<StAct,Double> qval = new HashMap<StAct,Double>();
  Map<StAct,Long> stActLastT = new HashMap<StAct,Long>();
  Model model = new Model();
  long t=0;
  int ep=0;
  List<String> actions;

  Visualizer vis = new Visualizer();

  private RWorld mkWorld() {
    //RWorld w = new StochasticWind();
    //RWorld w = new CliffWorld();
    //RWorld w = new MazeWorld();
    //RWorld w = new CarParkingWorld();
    //RWorld w = new BallParkingWorld();
    RWorld w = new PushCaretWorld();

    vis.setWorld(w);
    actions = w.actions();
    return w;
  }

  int qvalsOver(double limit){
    int r=0;
    for( double v : qval.values() ){
      if( v>=limit ){
        r++;
      }
    }
    return r;
  }

  Episode firstHit=null;

  private void doit() {
    //showVFunction();

    while(true){
      long t0=t;
      Episode epis = episode();
      double totalRew = epis.totalRew;
      if( epis.maxRew==1000 && firstHit==null ){
        firstHit=epis;
      }
      ep++;
      long dt = t - t0;
      System.out.println("episode end t="+t+" dt="+ dt
          +" ep="+ep+" totalRew="+totalRew+" qvals>100="+qvalsOver(100));
      if( firstHit!=null ){
        firstHit.printTrace();
      }
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

  void printVFunction(){
    Map<RState, String> m = new HashMap<RState, String>();
    for( RState s : statesVisited ){
      MinMaxFinder mm = new MinMaxFinder();
      String nextSt="";
      String msg = "<b>"+s+"</b> ";
      for( String a : actions ){
        double qvala = qvalGetNoTrend(s, a);
        mm.add(qvala, a);
        if( model.rew(new StAct(s,a))>0 ){
          msg += " <b>rew@"+a+"="+model.rew(new StAct(s,a))+"</b> ";
        }
//        RState ns = model.nextSt.get(new StAct(s,a));
//        if( ns!=null ){
//          nextSt += " "+a+"=>"+ns;
//        }
      }
      msg += String.format("%.1f", mm.getMaxVal());
      String amaxN = (String)mm.getMaxNames().get(0);
      double trend = actionTrend(s, amaxN);
      msg += nextSt+" maxN="+mm.getMaxNames()+
          "nextSt="+model.nextSt(new StAct(s, (String)mm.getMaxNames().get(0)))+
          " trend-"+amaxN+"="+trend;
      m.put(s, msg);
    }
    myWorld.printStateMap(m);

  }

//  private void printPolicy() {
//    if( t>3000 ){
//      Utils.breakPoint();
//      for( int y=1; y<=6; y++ ){
//        for( int x=1; x<=9; x++ ){
//          String s = x+"_"+y;
//          String a = (String) Utils.rnd(greedyActions(s));
//          System.out.printf("%2s ",a);
//        }
//        System.out.println();
//      }
//
//      for( int y=1; y<=6; y++ ){
//        for( int x=1; x<=9; x++ ){
//          String s = x+"_"+y;
//          double val = value(s);
//          System.out.printf("%6.2f ",val);
//        }
//        System.out.println();
//      }
//
//    }
//  }

  class Episode{
    List<StAct> trace = new ArrayList<StAct>();
    double totalRew;
    double maxRew = Double.NaN;

    void addReward(double r){
      totalRew += r;
      if( Double.isNaN(maxRew) ){
        maxRew=r;
      }else{
        maxRew = Math.max(r, maxRew);
      }
    }

    void printTrace(){
      CountingMap<RState> ls = new CountingMap<RState>();
      for( StAct sa : trace ){
        ls.increment(sa.getS());
      }
      System.out.println("trace.len="+trace.size()+" distinct.size="+ls.size());
      for( int i=0; i<trace.size(); i++ ){
        StAct sa = trace.get(i);
        System.out.println(sa +" Q="+qvalGet(sa.getS(), sa.getA()));
      }
    }
  }

  private Episode episode() {
    myWorld = mkWorld();
    Episode ep = new Episode();
    int step=0;
    while(!myWorld.isTerminal()){
      step++;
      t++;
      RState s = myWorld.getS();
      String a = policyAction(s);
      //if( t==1 ) a="4.0";
      //if( t==1 ) a="5.0";
      StAct sa = new StAct(s,a);
      if( t>3000 ){
//        w.println();
//        System.out.println("a="+a+" greedy="+greedyActions(s));
      }
      stActLastT.put(sa, t);
      statesVisited.add(s);

      //double v0a=myWorld.initStateValue(myWorld.getS());
      double r = myWorld.action(a);
      //double v0b=myWorld.initStateValue(myWorld.getS());

      ep.addReward(r);
      ep.trace.add(sa);

      RState s1 = myWorld.getS();
      //if( step<7 ){
        System.out.println(t+":  "+s+" a="+a+" >> "+r+" >> "+s1);
      //}

      // update Q
      qLearn(sa, r, Collections.singletonMap(s1,1.));

//      System.out.println("a="+a+" s="+myWorld.getS()+" dv0="+(v0b-v0a)
//          +" v0a="+v0a+" qvalUpd="+qval.get(sa));

      // update model
      model.update(sa, s1, r);

      // planning using model
      for( int i=0; i<500; i++ ){
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
    return ep;
  }

  private void qLearn(StAct sa, double r, Map<RState,Double> nextSt) {
    Double q = qval.get(sa);
    if( q==null ){
      //q=0d;
      q = qvalGetNoTrend(sa.s, sa.a);
    }
    double stateValAll=0;
    if( nextSt.size()>1 ){
      Utils.breakPoint();
    }
    for( RState s1 : nextSt.keySet() ){
      MinMaxFinder mmfQ = new MinMaxFinder();
      Map<String,Double> aqs = new HashMap<String,Double>();
      for( String a1 : actions ){
        //double q1 = qvalGet(s1, a1);
        double q1 = qvalGetNoTrend(s1, a1);
        mmfQ.add(q1, "");
        aqs.put(a1, q1);
      }
      double stateVal = mmfQ.getMaxVal();
      stateValAll += stateVal*nextSt.get(s1);
    }
    double dq = alpha * (r + stateValAll - q);
    q = q + dq;
    if( q>1000 ){
      Utils.breakPoint();
    }
    qval.put(sa, q);
  }


  String policyAction(RState s){
    //return epsilonGreedy(s);

    SoftGreedy2 sg = mkSoftGreedy(s);
    return sg.policyAction();
  }

  private SoftGreedy2 mkSoftGreedy(RState s) {
    SoftGreedy2 sg = new SoftGreedy2(epsilon);
    boolean tryTrend = false;//Math.random()<epsilon/2;
    for( String a : actions ){
      double actionQ = qvalGet(s, a);
      if( tryTrend ){
        actionQ += actionTrend(s, a);
      }
      if( bonusOnCommand ){
        Long told = stActLastT.get(new StAct(s,a));
        if( told!=null ){
          actionQ += explorationBonusK * Math.sqrt(t-told);
        }
      }
      sg.put(a, actionQ);
    }
    return sg;
  }

  private String epsilonGreedy(RState s) {
    if( Math.random()<epsilon ){
      return Utils.rnd(actions);
    }

    return (String) Utils.rnd(greedyActions(s));
  }

  private List greedyActions(RState s) {
    MinMaxFinder mmf = new MinMaxFinder();
    for( String a : actions ){
      Double val = qvalGet(s, a);
      mmf.add(val, a);
    }
    List acts = mmf.getMaxNames();
    return acts;
  }

  private double qvalGetNoTrend(RState s, String a) {
    //return qvalGet(s, a);
    Double val = qval.get(new StAct(s, a));
    if( val==null ){
       val = myWorld.initStateValue(s);
      //val=0d;
    }
    return val;
  }

  private double qvalGet(RState s, String a) {
    Double val = qval.get(new StAct(s, a));
    if( val==null ){
      //val = myWorld.initStateValue(s);

      //val=0d;
      val=2000d;//encourage exploration
      //val=1d;//encourage exploration
      //val = myWorld.initStateValue(s);
      //val += actionTrend(s, a);
    }
    return val;
  }

  private double actionTrend(RState s, String a) {
    Set<RState> neighbors = model.getNeighbors(s);
    neighbors = new HashSet<RState>(neighbors);
    neighbors.remove(s); // avoid current
    if( neighbors.size()==0 ){
      return 0;
    }
    double sumTrend = 0;
    for( RState nei : neighbors){
      double sum=0;
      double valA=0;
      for( String ai : actions ){
        Double val = qval.get(new StAct(nei, ai));
        if( val==null ){
           val = myWorld.initStateValue(nei);
        }
        if( ai.equals(a) ){
          valA = val;
        }
        sum+=val;
      }
      double trend = valA - sum/actions.size();
      sumTrend += trend;
    }
    double avgTrend = sumTrend / neighbors.size();
    return avgTrend;
  }

  private double value(RState s) {
    MinMaxFinder mmf = new MinMaxFinder();
    for( String a : actions ){
      Double val = qvalGet(s, a);
      mmf.add(val, a);
    }
    return mmf.getMaxVal();
  }

  class VFunctionCanvas extends Canvas{
    VFunctionCanvas() {
      setBackground(Color.GRAY); // to have update() called and flipping suppressed
      setPreferredSize(new Dimension(300,200));
    }

    @Override
    public void paint(Graphics g) {
      g.setColor(Color.GREEN);
      for( int x=0; x<300; x++ ){
        g.drawRect(x,x,1,1);
      }
    }

    int screenX(Point p){
      return p.x;
    }
    int screenY(Point p){
      return 200-p.y;
    }


  }

  void showVFunction(){
    JFrame f = new JFrame();
    f.add(new VFunctionCanvas());
    f.pack();
    f.setVisible(true);

  }
}