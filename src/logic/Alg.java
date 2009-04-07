package logic;

import intf.AlgIntf;
import intf.World;
import mem.*;
import utils.CountingTree;
import utils.Utils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/1/2008
 * Time: 19:18:04
 * To change this template use File | Settings | File Templates.
 */
public class Alg implements AlgIntf, Serializable {
  World w;
  History history = new History();
  PredictorApproach predictor = new PredictorApproach();
  CmdSet curCmd = CmdSet.EMPTY;

  Map<String, List<String>> cmdGroups = new HashMap<String, List<String>>();

  //Causes causes = new Causes();
  //Causes2 causes2 = new Causes2();
  List<Hist> interestingEvents = new ArrayList<Hist>(); //- copy to Cause2

  // experimentLevel - emotional koef.
  // Low if we are before difficult controlled situation.
  // High if we are in panic.
  final double experimentLevel = 0.1;
  final double experimentLevelIfSmacks = 0.02;

  boolean byCausesOnly = false;

  Attempts attempts = new Attempts(this);
  CountingTree<String> lastSuccessfulCommands = new CountingTree<String>();
  //private boolean cause1use=false;


  public Alg(World w) {
    this.w = new CheckedWorld(w);
  }

  public Set<String> cmdGroups() {
    return cmdGroups.keySet();
  }

  public boolean hasPlans() {
    return !curCmd.finished();
  }

  public void printRelavantCauses() {
//    for (Cause c : causes.applicableCauses(history.getNextHist())) {
//      log("c  " + c);
//    }
  }

  CmdSet nextCmdLogic(){
    CmdSet ret;
    Map<String, Object> view = w.view();
    if (curCmd.finished()) {
      ret = calcNextCmd(view, false);
    } else {
      CmdSet cmdStepCheck = calcNextCmd(view, true);
      if (cmdStepCheck != null && cmdStepCheck.getTargetResult() >= curCmd.getTargetResult()
              && cmdStepCheck.len() < curCmd.len()) {
        ret = cmdStepCheck;
        log("pre-calced cmd replaced with step command");
      } else {
        ret = curCmd;
        log("pre-calced cmd");
      }
    }
    return ret;
  }

  public synchronized String nextCmd(String forcedCmd) {
    log("------------------------------------");
    if (forcedCmd != null) {
      curCmd = new CmdSet(forcedCmd);
    } else {
      curCmd = nextCmdLogic();
    }

    assert (!curCmd.finished());

    String cmd = curCmd.goNext(cmdGroups);
    Map<String, Object> view = w.view();
    if (history.last == null) {
      history.setLastResult(0, view);
      predictor.add(history.next);
    }
    history.nextCmd(cmd);
    predictor.appendValsToLastView(Collections.singletonMap(Hist.CMD_KEY, (Object)cmd));
    log("====" + cmd + " foundFrom=" + curCmd.getFoundFrom());

//    log("====PRED "+approaches.get(0).predictionInfo(history.last));
//    log("====PREDv1 "+causes.predictAllViewByCauses(history.last));
//    log("====PREDv2 "+causes2.predictAllViewByCauses(history.last));

    return cmd;
  }

  public void cmdCompleted(int result) {
    Map<String, Object> view = w.view();
    history.setLastResult(result, view);
    if (result > 0) {
      interestingEvents.add(history.getNextHist());
    }
//    if( cause1use ){
//      new CmdCompletionAnalyzer(this).resultAnalyse(result, view);
//    }
    predictor.add(history.next);
  }



  List<String> allCommands() {
    List<String> ret = new ArrayList<String>();
    ret.addAll(w.commands());
    ret.addAll(cmdGroups.keySet());
    return ret;
  }

  public void printCmdPredictions(){
    List<String> cs = allCommands();
    predictor.printCmdPredictions(history.getNextHist(), cs);
    CmdSet nextCmd = nextCmdLogic();
    System.out.println( "nextCmdLogic="+ nextCmd + " foundFrom=" + nextCmd.getFoundFrom());
  }

  private CmdSet calcNextCmd(Map<String, Object> view, boolean stepByStepFastCheck) {
    List<String> cs = allCommands();

    CmdSet cc = null;
      SuggestionResult sugg = predictor.suggestCmd(history.getNextHist(), cs);
      CmdSet cp = sugg.cmdSet;
      if( cp!=null ){
        return cp;
      }

    double emotionCoef = cc == null ? experimentLevel : experimentLevelIfSmacks;
    CmdSet nextCmd;
    if (!stepByStepFastCheck && !byCausesOnly && Math.random() < emotionCoef) {
      log("random cmd to avoid loops or stalls in already discovered domain probab=" + emotionCoef);
      nextCmd = attempts.randomWithIntent(cs, view);
      return nextCmd;
    }

    if (cc != null) {
      return cc;
    }

    if( history.last!=null &&
        Utils.intersection(history.last.getViewAll(), history.next.getViewAll()).size()
            == history.next.getViewAll().size() ){
      OneView vp = predictor.predictor.predictNext(history.last);
      vp.remove(Hist.CMD_KEY);
      if( history.next.getViewAll().size()!=vp.getViewAll().size() ){
        CmdSet cm = new CmdSet(history.last.getCommand());
        cm.setFoundFrom("repeat noop ensure");
        return cm;
      }
    }

    // if there is step which smacks of result according to causes, do it
//    Set<String> smacksCmds = new HashSet<String>();
//    for (String c : cs) {
//      Hist ifCmdC = new Hist(history.last, view, c);
//      Map<String, Object> next = causes.predictAllViewByCauses(ifCmdC);
//      Hist nextHist = new Hist(ifCmdC, next, null);
//      Causes.SmacksOfResult smacks = causes.smacksOfResult(nextHist);
//      if (smacks != null && smacks.ds.hasElemsOtherThan(new ViewDepthElem(0, Hist.CMD_KEY))
//              && smacks.ds.hasElemsAtDepth(0)
//              && smacks.cause.getResult() > 0) {
//        smacksCmds.add(c);
//      }
//    }
//    if (!smacksCmds.isEmpty() && smacksCmds.size() < cs.size()) {
//      return attempts.randomWithIntent(new ArrayList<String>(smacksCmds), view);
//    }

    if (stepByStepFastCheck) {
      return null;
    }

    List<String> csFiltered = sugg.csFiltered;
    if( csFiltered.isEmpty() ){
      csFiltered = cs;
    }

    nextCmd = attempts.randomWithIntent(csFiltered, view);

    return nextCmd;
  }

//  List<Hist> unexplainedInterestingEvents() {
//    List<Hist> ret = new ArrayList<Hist>();
//    for (Hist hinter : interestingEvents) {
//      if (hinter == history.next) {
//        continue; // skip current event
//      }
//      int res = hinter.prev.getResultFromNext();
//      if (res > 0) {
//        Map<String, Object> prediction = causes.predictAllViewByCauses(hinter.prev);
//        if (prediction == null) {
//          prediction = new HashMap<String, Object>();
//        }
//        if (!("" + res).equals("" + prediction.get(Hist.RES_KEY))) {
//          // there is not predicted result
//          ret.add(hinter);
//        }
//      }
//    }
//    return ret;
//  }

//  Map<String, CauseMaxStruct> predictByCauses(Map<String, Object> view) {
//    Map<String, CauseMaxStruct> ret = new HashMap<String, CauseMaxStruct>();
//    List<String> cs = allCommands();
//    for (String c : cs) {
//      Hist ifCmdC = new Hist(history.last, view, c);
//      CauseMaxStruct m = causePredict(ifCmdC);
//      if (m.active.size() > 0) {
//        ret.put(c, m);
//      }
//    }
//    return ret;
//  }

//  CauseMaxStruct causePredict(Hist ifCmdC) {
//    CauseMaxStruct m = new CauseMaxStruct();
//    for (Cause cause : causes.list) {
//      if (cause.canPredict(ifCmdC) && cause.valid()) {
//        m.active.add(cause);
//      }
//    }
//    return m;
//  }

//  boolean zeroForAllCmds(Map<String, CauseMaxStruct> m) {
//    List<String> cs = allCommands();
//    for (String c : cs) {
//      if (!m.containsKey(c)) {
//        return false;
//      }
//      if (m.get(c).promisedMax() != 0) {
//        return false;
//      }
//    }
//    return true;
//  }

//  boolean explains(Cause newc, List<Cause> existsing) {
//    for (Cause c : existsing) {
//      if (c.explains(newc)) {
//        return true;
//      }
//    }
//    return false;
//  }

  void printFound(List<Hist> found, int depth) {
    for (Hist h : found) {
      log("found");
      Hist hi = h;
      for (int i = 0; i < depth && hi != null; i++) {
        log("" + hi);
        hi = hi.prev;
      }
    }
  }


  void breakPoint() {

  }

//  private void filterNoopCmds(Map<String, Object> view, List<String> cset) {
//    List<String> toRemove = new ArrayList<String>();
//    for (String c : cset) {
//      Hist hnow = new Hist(history.last, view, c);
//      CauseMaxStruct cms = causePredict(hnow);
//      if (cms.noop()) {
//        //cset.remove(c);
//        toRemove.add(c);
//        log("removing noop " + c + " - " + cms.toString());
//      }
//    }
//    cset.removeAll(toRemove);
//  }


  public void setByCausesOnly(boolean byCausesOnly) {
    this.byCausesOnly = byCausesOnly;
  }

  void log(String s) {
    System.out.println(s);
  }

  private static final long serialVersionUID = 7532931955747736010L;
}
