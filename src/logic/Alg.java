package logic;

import intf.AlgIntf;
import intf.World;
import mem.*;
import utils.CountingTree;
import utils.Utils;

import java.io.Serializable;
import java.util.*;

import predict.Predictor;
import predict.CmdPredictionTree;
import predict.PredictionTreeBuilder;

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
  List<Approach> approaches = (List)Arrays.asList(new PredictorApproach());
  CmdSet curCmd = CmdSet.EMPTY;

  Map<String, List<String>> cmdGroups = new HashMap<String, List<String>>();

  Causes causes = new Causes();
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
    for (Cause c : causes.applicableCauses(history.getNextHist())) {
      log("c  " + c);
    }
  }

  public synchronized String nextCmd(String forcedCmd) {
    log("------------------------------------");
    Map<String, Object> view = w.view();
    if (forcedCmd != null) {
      curCmd = new CmdSet(forcedCmd);
    } else {
      if (curCmd.finished()) {
        curCmd = calcNextCmd(view, false);
      } else {
        CmdSet cmdStepCheck = calcNextCmd(view, true);
        if (cmdStepCheck != null && cmdStepCheck.getTargetResult() >= curCmd.getTargetResult()
                && cmdStepCheck.len() < curCmd.len()) {
          curCmd = cmdStepCheck;
          log("pre-calced cmd replaced with step command");
        } else {
          log("pre-calced cmd");
        }
      }
    }

    assert (!curCmd.finished());

    String cmd = curCmd.goNext(cmdGroups);
    if (history.last == null) {
      history.setLastResult(0, view);
      approaches.get(0).add(history.next);
    }
    history.nextCmd(cmd);
    approaches.get(0).appendValsToLastView(Collections.singletonMap(Hist.CMD_KEY, (Object)cmd));
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
    approaches.get(0).add(history.next);
  }

  PredictionTree buildPredictionTreeOld(Hist last, Map<String, Object> view) {
    PredictionTree predictionTree = new PredictionTree(last, view);
    predictionTree.smacks = causes.smacksOfResult(predictionTree.histNew());

    List<Hist> unexplainedInterestingEvents = unexplainedInterestingEvents();
    List<PredictionTree> readyNotes = Arrays.asList(predictionTree);
    for (int i = 0; i < 5 && readyNotes.size() > 0; i++) {
      List<PredictionTree> notesToExplore = new ArrayList<PredictionTree>();
      for (PredictionTree pti : readyNotes) {
        if (pti.noop) {
          continue;
        }
        for (String c : allCommands()) {
          expandPredictionOld(pti, c, notesToExplore);
        }
        for (String c : allCommands()) {
          Hist h = new Hist(pti.histOld, pti.viewNext, c);
          if (causes.predictsNoop(h)) {
            PredictionTree child = pti.onCommand.get(c);
            if (child == null) {
              child = pti.addChild(h, c, null);
            }
            child.noop = true;
          }
        }

        for (Hist hinter : unexplainedInterestingEvents) {
          Map<String, Object> com = Utils.intersection(hinter.prev.getViewAll(), view);
          if (!com.isEmpty()) {
            String cmd = hinter.prev.getCommand();
            PredictionTree child = pti.onCommand.get(cmd);
            if (child == null) {
              Hist h = new Hist(pti.histOld, pti.viewNext, cmd);
              child = pti.addChild(h, cmd, null);
            }
            child.smacksEvent = hinter;
          }
        }
      }
      readyNotes = notesToExplore;
    }
    return predictionTree;
  }

  private void expandPredictionOld(PredictionTree pti, String c, List<PredictionTree> notesToExplore) {
    Hist h = new Hist(pti.histOld, pti.viewNext, c);
    Causes.PredictionBy prediction = causes.predictAllViewByCausesWithBy(h);
    if (prediction != null) {
      Map<String, Object> test = new HashMap<String, Object>(prediction.view);
      test.remove(Hist.NOOP_KEY);
      if ("0".equals("" + test.get(Hist.RES_KEY))) {
        test.remove(Hist.RES_KEY);
      }
      if (test.size() > 0) { // if has smth. meaningful
        PredictionTree node = pti.addChild(h, c, prediction);
        node.smacks = causes.smacksOfResult(node.histNew());
        boolean val = prediction.view.get(Hist.RES_KEY) != null && ((Integer) prediction.view.get(Hist.RES_KEY)).intValue() != 0;
        if (!val) { // if result of this branch not yet known
          notesToExplore.add(node);
        }
      }
    }
  }


//  PredictionTree2 buildPredictionTree(Hist last, Map<String, Object> view) {
//    PredictionTree2 predictionTree = new PredictionTree2(last, view);
//    predictionTree.smacks = causes2.smacksOfResult(predictionTree.histNew());
//
//
//    List<PredictionTree2> readyNotes = Arrays.asList(predictionTree);
//    for (int i = 0; i < 5 && readyNotes.size() > 0; i++) {
//      List<PredictionTree2> notesToExplore = new ArrayList<PredictionTree2>();
//      for (PredictionTree2 pti : readyNotes) {
//        if (pti.noop) {
//          continue;
//        }
//        for (String c : allCommands()) {
//          expandPrediction(pti, c, notesToExplore);
//
//          Map<String, Object> viewCmd = new HashMap<String, Object>(view);
//          viewCmd.put(Hist.CMD_KEY, c);
//          for (Cause2 cc : causes2.list) {
//            if (cc.isPositiveResult()) {
//              Hist h = cc.unexplainedExamplesIntersect(viewCmd);
//              if (h != null) {
//                String cmd = h.getCommand();
//                PredictionTree2 child = pti.onCommand.get(cmd);
//                if (child == null) {
//                  Hist hn = new Hist(pti.histOld, pti.viewNext, cmd);
//                  child = pti.addChild(hn, cmd, null);
//                }
//                child.smacksEvent = h;
//                if (child.viewNext == null) {
//                  child.viewNext = new HashMap<String, Object>();
//                }
//                child.viewNext.put(cc.key, cc.val);
//              }
//            }
//          }
//        }
//
//        for (String c : allCommands()) {
//          Hist h = new Hist(pti.histOld, pti.viewNext, c);
//          if (causes.predictsNoop(h)) {
//            PredictionTree2 child = pti.onCommand.get(c);
//            if (child == null) {
//              child = pti.addChild(h, c, null);
//            }
//            child.noop = true;
//          }
//        }
//
//
//      }
//      readyNotes = notesToExplore;
//    }
//    return predictionTree;
//  }


//  private void expandPrediction(PredictionTree2 pti, String c, List<PredictionTree2> notesToExplore) {
//    Hist h = new Hist(pti.histOld, pti.viewNext, c);
//    Causes2.PredictionBy prediction = causes2.predictAllViewByCausesWithBy(h);
//    if (prediction != null) {
//      Map<String, Object> test = new HashMap<String, Object>(prediction.view);
//      test.remove(Hist.NOOP_KEY);
//      if ("0".equals("" + test.get(Hist.RES_KEY))) {
//        test.remove(Hist.RES_KEY);
//      }
//      if (test.size() > 0) { // if has smth. meaningful
//        PredictionTree2 node = pti.addChild(h, c, prediction);
//        node.smacks = causes2.smacksOfResult(node.histNew());
//        boolean val = prediction.view.get(Hist.RES_KEY) != null && ((Integer) prediction.view.get(Hist.RES_KEY)).intValue() != 0;
//        if (!val) { // if result of this branch not yet known
//          notesToExplore.add(node);
//        }
//      }
//    }
//  }

//  CmdSet findMaxCmdByCauses(Map<String,CauseMaxStruct> predictedMinResults, Map<String, Object> view){
//    if( predictedMinResults.isEmpty() ){
//      return null;
//    }
//    int max = Integer.MIN_VALUE;
//    int min = Integer.MAX_VALUE;
//    for( CauseMaxStruct cms : predictedMinResults.values() ){
//      if( cms.hasResult() ){
//        max = Math.max(max, cms.promisedMax());
//        min = Math.min(min, cms.promisedMax());
//      }
//    }
//    boolean useMax=false;
//    boolean avoidAny=false;
//    if( max>0 ){
//      useMax=true;
//    }else if( max<0 ){
//      avoidAny=true;
//    }else{
//      assert( max==0 );
//      if( min!=max ){
//        useMax=true;
//      }
//    }
//
//    if( avoidAny ){
//      // not yet tested command is likely to have result of 0
//
//      // so if there are other commands - choose ANY - it's better
//      // than ignoring Cause results altogether
//      List<String> cs =  new ArrayList<String>(allCommands());
//      filterNoopCmds(view, cs);
//
//      for( String c : predictedMinResults.keySet() ){
//        cs.remove(c);
//      }
//      if( !cs.isEmpty() ){
//        CmdSet cset = attempts.randomWithIntent(cs, view);
//        cset.setFoundFrom("by causes - avoidAny");
//        return cset;
//      }
//      return null;
//    }
//
//    if( useMax ){
//      List<String> cmds = new ArrayList<String>();
//      for( String s : predictedMinResults.keySet() ){
//        if( predictedMinResults.get(s).promisedMax()==max ){
//          cmds.add(s);
//        }
//      }
//      if( max<=0 ){
//        // also add commands with no info as yet
//        for( String cmd : allCommands() ){
//          if( !predictedMinResults.containsKey(cmd) ){
//            cmds.add(cmd);
//          }
//        }
//      }
//      filterNoopCmds(view, cmds);
//      CmdSet cset = attempts.randomWithIntent(cmds, view);
//      cset.setTargetResult(max);
//      cset.setFoundFrom("by causes - useMax "+min+".."+max+" rnd of "+cmds);
//      return cset;
//    }
//
//    return null;
//  }


  List<String> allCommands() {
    List<String> ret = new ArrayList<String>();
    ret.addAll(w.commands());
    ret.addAll(cmdGroups.keySet());
    return ret;
  }

  public void printCmdPredictions(){
    List<String> cs = allCommands();
    for( Approach appr : approaches ){
      appr.printCmdPredictions(history.getNextHist(), cs);
    }
  }

  private CmdSet calcNextCmd(Map<String, Object> view, boolean stepByStepFastCheck) {
    List<String> cs = allCommands();

    CmdSet cc = null;

    for( Approach appr : approaches ){
      /*@todo maybe Collections.shuffle and let other approaches preview the cmd -
        Rnd can this way sometimes make it's reasonable attempts */

      //@todo maybe introduce to History - approach which suggested Cmd - to see in dynamics which is better
      CmdSet c = appr.suggestCmd(history.getNextHist(), cs);
      if( c!=null ){
        return c;
      }
    }


//      if( cause1use ){
//        PredictionTree pt = buildPredictionTreeOld(history.last, view);
//        PredictionTree.PositiveResultOrSmack smackRes = pt.findPositiveResultOrSmacks();
//        if (smackRes != null && smackRes.cmd != null) {
//          cc = new CmdSet(smackRes.cmd);
//          cc.setFoundFrom("using pred tree smacks " + smackRes.description);
//        }
//      }


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

    // if there is step which smacks of result according to causes, do it
    Set<String> smacksCmds = new HashSet<String>();
    for (String c : cs) {
      Hist ifCmdC = new Hist(history.last, view, c);
      Map<String, Object> next = causes.predictAllViewByCauses(ifCmdC);
      Hist nextHist = new Hist(ifCmdC, next, null);
      Causes.SmacksOfResult smacks = causes.smacksOfResult(nextHist);
      if (smacks != null && smacks.ds.hasElemsOtherThan(new ViewDepthElem(0, Hist.CMD_KEY))
              && smacks.ds.hasElemsAtDepth(0)
              && smacks.cause.getResult() > 0) {
        smacksCmds.add(c);
      }
    }
    if (!smacksCmds.isEmpty() && smacksCmds.size() < cs.size()) {
      return attempts.randomWithIntent(new ArrayList<String>(smacksCmds), view);
    }

//    Map<String,MaxStruct> predictedMinResults2Cmds = predictMinResults2Cmds(view);
//    nextCmd = findMaxCmdPair(predictedMinResults2Cmds);
//    if( nextCmd!=null ){
//      log("predicted positive result with 2 commands ("+nextCmd+"), get it!");
//      return nextCmd;
//    }

//    for( Hist hinter : unexplainedInterestingEvents() ){
//          Map<String,Object> com = Utils.intersection( hinter.prev.getViewAll(), view );
//          if( !com.isEmpty() ){
//            nextCmd = new CmdSet(hinter.prev.getCommand());
//            nextCmd.setFoundFrom("interesting event prev "+hinter.prev + " com="+com);
//            return nextCmd;
//          }
//    }

    if (stepByStepFastCheck) {
      return null;
    }

//    List<String> cset = new ArrayList<String>(cs);

//    filterNoopCmds(view, cset);
//    if (cset.size() == 0) {
//      // 0.1 - emotional koef.
//      // Low if we are before difficult controlled situation.
//      // High if we are in panic.
//      nextCmd = attempts.randomWithIntent(cs, view);
//    } else {
//      nextCmd = attempts.randomWithIntent(cset, view);
//    }

    rpt:
    for( int rpt=0; rpt<5; rpt++ ){
      nextCmd = attempts.randomWithIntent(cs, view);
      for( Approach a : approaches ){
        if( !a.assessCmd(history.getNextHist(), nextCmd.getCommand()) ){
          log(nextCmd+" - not good");
          continue rpt;
        }
      }
    }

    // out of rpt attempts
    nextCmd = attempts.randomWithIntent(cs, view);

    return nextCmd;
  }

  List<Hist> unexplainedInterestingEvents() {
    List<Hist> ret = new ArrayList<Hist>();
    for (Hist hinter : interestingEvents) {
      if (hinter == history.next) {
        continue; // skip current event
      }
      int res = hinter.prev.getResultFromNext();
      if (res > 0) {
        Map<String, Object> prediction = causes.predictAllViewByCauses(hinter.prev);
        if (prediction == null) {
          prediction = new HashMap<String, Object>();
        }
        if (!("" + res).equals("" + prediction.get(Hist.RES_KEY))) {
          // there is not predicted result
          ret.add(hinter);
        }
      }
    }
    return ret;
  }

  Map<String, CauseMaxStruct> predictByCauses(Map<String, Object> view) {
    Map<String, CauseMaxStruct> ret = new HashMap<String, CauseMaxStruct>();
    List<String> cs = allCommands();
    for (String c : cs) {
      Hist ifCmdC = new Hist(history.last, view, c);
      CauseMaxStruct m = causePredict(ifCmdC);
      if (m.active.size() > 0) {
        ret.put(c, m);
      }
    }
    return ret;
  }

  CauseMaxStruct causePredict(Hist ifCmdC) {
    CauseMaxStruct m = new CauseMaxStruct();
    for (Cause cause : causes.list) {
      if (cause.canPredict(ifCmdC) && cause.valid()) {
        m.active.add(cause);
      }
    }
    return m;
  }

  boolean zeroForAllCmds(Map<String, CauseMaxStruct> m) {
    List<String> cs = allCommands();
    for (String c : cs) {
      if (!m.containsKey(c)) {
        return false;
      }
      if (m.get(c).promisedMax() != 0) {
        return false;
      }
    }
    return true;
  }

  boolean explains(Cause newc, List<Cause> existsing) {
    for (Cause c : existsing) {
      if (c.explains(newc)) {
        return true;
      }
    }
    return false;
  }

  // C1 V1 C0, V0-unknown - 2 steps ahead prediction

//  Map<String,MaxStruct> predictMinResults(Map<String, Object> view) {
//    Map<String,MaxStruct> ret = new HashMap<String,MaxStruct>();
//    List<String> cs =  allCommands();
//    //List<Cause> newCauseCandidates = new ArrayList<Cause>();
//    for( String c : cs ){
//      Hist histVariant = new Hist(history.last, view, c);
//
//      MaxStruct allmax = findRelations(new ViewDepthGenerator(view.keySet()), histVariant);
//
//      if( allmax.valid() ){
//        ret.put(c, allmax);
//      }
//    }
//
//    return ret;
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

  private void filterNoopCmds(Map<String, Object> view, List<String> cset) {
    List<String> toRemove = new ArrayList<String>();
    for (String c : cset) {
      Hist hnow = new Hist(history.last, view, c);
      CauseMaxStruct cms = causePredict(hnow);
      if (cms.noop()) {
        //cset.remove(c);
        toRemove.add(c);
        log("removing noop " + c + " - " + cms.toString());
      }
    }
    cset.removeAll(toRemove);
  }


  public void setByCausesOnly(boolean byCausesOnly) {
    this.byCausesOnly = byCausesOnly;
  }

  void log(String s) {
    System.out.println(s);
  }

  private static final long serialVersionUID = 7532931955747736010L;
}
