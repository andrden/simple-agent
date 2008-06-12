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
  Map<String,PredictionTree> onCommand = new HashMap<String, PredictionTree>();
  List<Cause> predictionBy;

  public PredictionTree(Hist h, Map<String, Object> viewNext) {
    this.viewNext = viewNext;
    this.histOld =h;
  }

  public PredictionTree addChild(Hist h, String command, Causes.PredictionBy viewNextBy){
    PredictionTree predictionTree = new PredictionTree(h, viewNextBy.view);
    predictionTree.predictionBy =viewNextBy.by;
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
          for( Cause c : nextStep.predictionBy ){
            smack0.putAll(c.recentCoditionBase());
          }
          return smack0; 
        }
      }
    }
    throw new IllegalStateException();
  }

  static class PositiveResultOrSmack{
    String cmd;
    String description;
    int depth=0;

    public PositiveResultOrSmack(String cmd, String description) {
      this.cmd = cmd;
      this.description = description;
    }
  }

  /**
   * returns smacks description
   * @return
   */
  public PositiveResultOrSmack findPositiveResultOrSmacks(){
    Integer res = Hist.getResult(viewNext);
    if( res!=null && res>0 ){
      return new PositiveResultOrSmack(null, "res="+res);
    }
    if( smacks!=null && smacks.cause.getResult()>0 ){
      return new PositiveResultOrSmack(null, ""+smacks.cause);
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

    if( onCmds.size()!=0 ){
      return findShortest(onCmds);
    }

    return null;
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
