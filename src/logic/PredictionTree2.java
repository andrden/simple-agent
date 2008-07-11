package logic;

import mem.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 29/5/2008
 * Time: 18:48:30
 */
public class PredictionTree2 {
  Hist histOld;
  Causes2.SmacksOfResult smacks;
  Map<String,Object> viewNext;
  Hist smacksEvent;
  Map<String, PredictionTree2> onCommand = new HashMap<String, PredictionTree2>();
  List<Cause2> predictionBy;
  boolean noop=false;

  public PredictionTree2(Hist h, Map<String, Object> viewNext) {
    this.viewNext = viewNext;
    this.histOld =h;
  }

  public PredictionTree2 addChild(Hist h, String command, Causes2.PredictionBy viewNextBy){
    Map<String, Object> viewNext = null;
    if( viewNextBy!=null ){
      viewNext = viewNextBy.view;
    }
    PredictionTree2 predictionTree = new PredictionTree2(h, viewNext);
    if( viewNextBy!=null ){
      predictionTree.predictionBy =viewNextBy.by;
    }
    onCommand.put(command, predictionTree);
    return predictionTree;
  }

  public Hist histNew(){
    return new Hist(histOld, viewNext, null);
  }

  public Map<String, Object> getResultOrSmacksKeyView(){
    Integer res = Hist.getResult(viewNext);
    if( res!=null && res>0 ){
      throw new UnsupportedOperationException(); // handled by a separate IF in Alg
    }
    if( smacks!=null ){
      return smacks.ds.getElemsAtDepth(0);
    }else{
      for( PredictionTree2 nextStep : onCommand.values() ){
        // run over first level branches
        if( nextStep.findPositiveResultOrSmacks()!=null ){
          // intersting branch...
          Map<String, Object> smack0 = new HashMap<String, Object>();
          if( nextStep.predictionBy!=null ){
            for( Cause2 c : nextStep.predictionBy ){
              smack0.putAll(c.recentCoditionBase());
            }
          }
          return smack0;
        }
      }
    }
    throw new IllegalStateException();
  }

  static class PositiveResultOrSmack{
    double probab;
    String cmd;
    String description;
    int depth=0;

    public PositiveResultOrSmack(double probab, String cmd, String description) {
      this.cmd = cmd;
      this.description = description;
    }
  }

  /**
   * returns smacks description
   * @return
   */
  public PredictionTree2.PositiveResultOrSmack findPositiveResultOrSmacks(){
    if( noop ){
      return null;
    }
    Integer res = viewNext==null ? null : Hist.getResult(viewNext);
    if( res!=null && res>0 ){
      return new PredictionTree2.PositiveResultOrSmack(1, null, "res="+res);
    }

    Map<String, PredictionTree2.PositiveResultOrSmack> onCmds = new HashMap<String, PredictionTree2.PositiveResultOrSmack>();
    for( String nextStepCmd : onCommand.keySet() ){
      PredictionTree2 nextStep = onCommand.get(nextStepCmd);
      PredictionTree2.PositiveResultOrSmack nextSmack = nextStep.findPositiveResultOrSmacks();
      if(nextSmack!=null){
        nextSmack.description = nextStepCmd + " -> " + nextSmack.description;
        nextSmack.cmd = nextStepCmd;
        nextSmack.depth++;
        onCmds.put(nextStepCmd, nextSmack);
      }
    }

    PredictionTree2.PositiveResultOrSmack rnow=null;
    if( smacks!=null && smacks.cause.isPositiveResult() ){
      rnow = new PredictionTree2.PositiveResultOrSmack(0.5, null, ""+smacks.cause);
    }
    if( rnow==null && smacksEvent!=null ){
      rnow = new PredictionTree2.PositiveResultOrSmack(0.5, null, "event.prev="+smacksEvent.prev);
    }

    if( onCmds.size()!=0 ){
      PredictionTree2.PositiveResultOrSmack r = findShortest(onCmds);
      return moreDefinite(rnow, r);
    }

    return rnow;
  }

  PredictionTree2.PositiveResultOrSmack moreDefinite(PredictionTree2.PositiveResultOrSmack a, PredictionTree2.PositiveResultOrSmack b){
    if( a==null ){
      return b;
    }
    if( b==null ){
      return a;
    }
    if( a.probab>=b.probab ){
      return a;
    }
    return b;
  }

  PredictionTree2.PositiveResultOrSmack findShortest(Map<String, PredictionTree2.PositiveResultOrSmack> onCmds){
    int minDepth = Integer.MAX_VALUE;
    List<PredictionTree2.PositiveResultOrSmack> options = new ArrayList<PositiveResultOrSmack>();
    for( PredictionTree2.PositiveResultOrSmack p : onCmds.values() ){
      if( p.depth<minDepth ){
        minDepth=p.depth;
        options = new ArrayList<PredictionTree2.PositiveResultOrSmack>();
      }
      if( p.depth==minDepth ){
        options.add(p);
      }
    }

    //ERROR!!!
    /*
    for each command there must be some assessment taken
    from causes, prediction tree, etc.
    and if there is still ambiguity then
    continue with other assessment methods to
    find the really best command.
    In case of non-discrete worlds there will be intervals -
    don't move arm too far - you'll fell down.
    Reach within x..y limit to get to the bottle.
    And don't move your leg meantime because we can't predict results
    then, while currently we can see a result smack.
     */

    return Utils.rnd(options);
  }

  public String toString() {
    return ""+viewNext;
  }

  void print(){
    print(0);
  }
  void print(int level){
    System.out.println(toString());
    for( String c : onCommand.keySet() ){
      System.out.print(Utils.spaces(level+2)+c+" -> ");
      onCommand.get(c).print(level+2);
    }
  }
}
