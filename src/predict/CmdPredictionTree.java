package predict;

import mem.Hist;
import mem.OneView;

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
public class CmdPredictionTree {
  CmdPredictionTree parent;
  OneView start;
  boolean conflictingPrediction=false;
  int noop = 0;
  boolean loop=false;
  Map<String, CmdPredictionTree> onCommand = new HashMap<String, CmdPredictionTree>();

  public CmdPredictionTree(OneView start) {
    this.start = start;
  }

  public CmdPredictionTree(OneView start, boolean conflictingPrediction) {
    this.start = start;
    this.conflictingPrediction = conflictingPrediction;
  }

  public void loopDetect(List<String> allCommands){
    boolean notAllNoop=false;
    for (String c : allCommands) {
      CmdPredictionTree br = branchOnCommand(c);
      if( br==null || !br.noopDetected() ){
        notAllNoop=true;
      }
    }
    if( !notAllNoop ){
//          if for all commands some kind of noop detected, mark this as loop,
//          proceed to parent, maybe it's a loop too
      loop=true;
      if( parent!=null ){
        parent.loopDetect(allCommands);
      }
    }


  }

  public boolean conflictingPredictionOnCommand(String cmd){
    CmdPredictionTree t = onCommand.get(cmd);
    if( t!=null ){
      return t.isConflictingPrediction();
    }
    return false;
  }

  public CmdPredictionTree branchOnCommand(String cmd){
    return onCommand.get(cmd);
  }

  public OneView viewOnCommand(String cmd){
    CmdPredictionTree t = onCommand.get(cmd);
    if( t!=null ){
      return t.start;
    }
    return null;
  }

  public CmdPredictionTree addChild(String command, OneView nextStart,
                                    boolean conflictingPrediction) {
    CmdPredictionTree branch = new CmdPredictionTree(nextStart, conflictingPrediction);
    branch.parent = this;
    onCommand.put(command, branch);

    detectNoop(branch);

    return branch;
  }

  private void detectNoop(CmdPredictionTree branch) {
    CmdPredictionTree p = this;
    int depth=1; // noop of first order - direct
    for(;;){
      Map<String, Object> m = p.start.getViewAll();
      m.remove(Hist.CMD_KEY);
      Map mBranch = branch.start.getViewAll();
      mBranch.remove(Hist.CMD_KEY);
      if( m.toString().equals(mBranch.toString()) ){
        branch.noop=depth;
        break;
      }

      depth++;
      p=p.parent;
      if( p==null ){
        break;
      }
    }
  }

  public static class PositiveResultOrSmack {
    double probab;
    String cmd;
    String description;
    int depth = 0;

    public PositiveResultOrSmack(double probab, String cmd, String description) {
      this.cmd = cmd;
      this.description = description;
    }


    public String getCmd() {
      return cmd;
    }
  }

  boolean positiveResult(){
    Object r = start.get(Hist.RES_KEY);
    if( r==null ){
      return false;
    }
    return Integer.parseInt(r.toString())>0;
  }

  /**
   * returns smacks description
   *
   * @return
   */
  public CmdPredictionTree.PositiveResultOrSmack findPositiveResultOrSmacks() {
    if (positiveResult()) {
      return new CmdPredictionTree.PositiveResultOrSmack(1, null, "res=" + start.get(Hist.RES_KEY));
    }

    Map<String, CmdPredictionTree.PositiveResultOrSmack> onCmds = new HashMap<String, CmdPredictionTree.PositiveResultOrSmack>();
    for (String nextStepCmd : onCommand.keySet()) {
      CmdPredictionTree nextStep = onCommand.get(nextStepCmd);
      CmdPredictionTree.PositiveResultOrSmack nextSmack = nextStep.findPositiveResultOrSmacks();
      if (nextSmack != null) {
        nextSmack.description = nextStepCmd + " -> " + nextSmack.description;
        nextSmack.cmd = nextStepCmd;
        nextSmack.depth++;
        onCmds.put(nextStepCmd, nextSmack);
      }
    }

    CmdPredictionTree.PositiveResultOrSmack rnow = null;

    if (onCmds.size() != 0) {
      CmdPredictionTree.PositiveResultOrSmack r = findShortest(onCmds);
      return moreDefinite(rnow, r);
    }

    return rnow;
  }

  CmdPredictionTree.PositiveResultOrSmack moreDefinite(CmdPredictionTree.PositiveResultOrSmack a, CmdPredictionTree.PositiveResultOrSmack b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    if (a.probab >= b.probab) {
      return a;
    }
    return b;
  }

  CmdPredictionTree.PositiveResultOrSmack findShortest(Map<String, CmdPredictionTree.PositiveResultOrSmack> onCmds) {
    int minDepth = Integer.MAX_VALUE;
    List<CmdPredictionTree.PositiveResultOrSmack> options = new ArrayList<PositiveResultOrSmack>();
    for (CmdPredictionTree.PositiveResultOrSmack p : onCmds.values()) {
      if (p.depth < minDepth) {
        minDepth = p.depth;
        options = new ArrayList<CmdPredictionTree.PositiveResultOrSmack>();
      }
      if (p.depth == minDepth) {
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

  void print() {
    print(0);
  }

  void print(int level) {
    System.out.println(toString());
    for (String c : onCommand.keySet()) {
      System.out.print(Utils.spaces(level + 2) + c + " -> ");
      onCommand.get(c).print(level + 2);
    }
  }

  public boolean isConflictingPrediction() {
    return conflictingPrediction;
  }

  public boolean noopDetected(){
    return noop>0 || loop;
  }

  boolean resultNotZero(){
    return start.get(Hist.RES_KEY) != null && resultNotZero(start.get(Hist.RES_KEY));
  }

  boolean resultNotZero(Object res){
    return  !"0".equals(""+res);
  }

  @Override
  public String toString() {
    String s = "";
    if( noopDetected() ){
      s += "noop="+noop;
    }
    if( loop ){
      s += " loop";
    }
    if( resultNotZero() ){
      s += " res="+start.get(Hist.RES_KEY);
    }
    s += " viewSize="+start.getViewAll().size();
    return s;
  }
}
