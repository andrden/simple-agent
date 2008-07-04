package logic;

import mem.Hist;
import mem.Causes;
import mem.Cause;

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
public class PredictionTree {
  Hist histOld;
  Causes.SmacksOfResult smacks;
  Map<String,Object> viewNext;
  Hist smacksEvent;
  Map<String,PredictionTree> onCommand = new HashMap<String, PredictionTree>();
  List<Cause> predictionBy;
  boolean noop=false;

  public PredictionTree(Hist h, Map<String, Object> viewNext) {
    this.viewNext = viewNext;
    this.histOld =h;
  }

  public PredictionTree addChild(Hist h, String command, Causes.PredictionBy viewNextBy){
    Map<String, Object> viewNext = null;
    if( viewNextBy!=null ){
      viewNext = viewNextBy.view;
    }
    PredictionTree predictionTree = new PredictionTree(h, viewNext);
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
      for( PredictionTree nextStep : onCommand.values() ){
        // run over first level branches
        if( nextStep.findPositiveResultOrSmacks()!=null ){
          // intersting branch...
          Map<String, Object> smack0 = new HashMap<String, Object>();
          if( nextStep.predictionBy!=null ){
            for( Cause c : nextStep.predictionBy ){
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
  public PositiveResultOrSmack findPositiveResultOrSmacks(){
    if( noop ){
      return null;
    }
    Integer res = viewNext==null ? null : Hist.getResult(viewNext);
    if( res!=null && res>0 ){
      return new PositiveResultOrSmack(1, null, "res="+res);
    }

    Map<String, PositiveResultOrSmack> onCmds = new HashMap<String, PositiveResultOrSmack>();
    for( String nextStepCmd : onCommand.keySet() ){
      PredictionTree nextStep = onCommand.get(nextStepCmd);
      PositiveResultOrSmack nextSmack = nextStep.findPositiveResultOrSmacks();
      if(nextSmack!=null){
        nextSmack.description = nextStepCmd + " -> " + nextSmack.description;
        nextSmack.cmd = nextStepCmd;
        nextSmack.depth++;
        onCmds.put(nextStepCmd, nextSmack);
      }
    }

    PositiveResultOrSmack rnow=null;
    if( smacks!=null && smacks.cause.getResult()>0 ){
      rnow = new PositiveResultOrSmack(0.5, null, ""+smacks.cause);
    }
    if( rnow==null && smacksEvent!=null ){
      rnow = new PositiveResultOrSmack(0.5, null, "event.prev="+smacksEvent.prev);
    }

    if( onCmds.size()!=0 ){
      PositiveResultOrSmack r = findShortest(onCmds);
      return moreDefinite(rnow, r);
    }

    return rnow;
  }

  PositiveResultOrSmack moreDefinite(PositiveResultOrSmack a, PositiveResultOrSmack b){
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

  PositiveResultOrSmack findShortest(Map<String, PositiveResultOrSmack> onCmds){
    int minDepth = Integer.MAX_VALUE;
    List<PositiveResultOrSmack> options = new ArrayList<PositiveResultOrSmack>();
    for( PositiveResultOrSmack p : onCmds.values() ){
      if( p.depth<minDepth ){
        minDepth=p.depth;
        options = new ArrayList<PositiveResultOrSmack>();
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
