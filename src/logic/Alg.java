package logic;

import java.util.*;
import java.io.Serializable;

import mem.*;
import intf.World;
import intf.AlgIntf;
import utils.Utils;
import utils.CountingTree;

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
  CmdSet curCmd = CmdSet.EMPTY;

  Map<String,List<String>> cmdGroups = new HashMap<String,List<String>>();

  Causes causes = new Causes();
  double experimentLevel=0.1;
  boolean byCausesOnly=false;

  Attempts attempts = new Attempts(this);
  CountingTree<String> lastSuccessfulCommands = new CountingTree<String>();


  public Alg(World w) {
    this.w = new CheckedWorld(w);
  }

  public Set<String> cmdGroups(){
    return cmdGroups.keySet();
  }

  public boolean hasPlans(){
    return !curCmd.finished();
  }

  public synchronized String nextCmd(String forcedCmd){
    Map<String,Object> view = w.view();
    if( forcedCmd!=null ){
      curCmd = new CmdSet(forcedCmd);
    }else{
      if( curCmd.finished() ){
        curCmd = calcNextCmd(view, false);
      }else{
        CmdSet cmdStepCheck = calcNextCmd(view, true);
        if( cmdStepCheck!=null && cmdStepCheck.getTargetResult() >= curCmd.getTargetResult()
                && cmdStepCheck.len()<curCmd.len() ){
          curCmd=cmdStepCheck;
          log("pre-calced cmd replaced with step command");
        }else{
          log("pre-calced cmd");
        }
      }
    }
    
    assert (!curCmd.finished());

    String cmd = curCmd.goNext(cmdGroups);
    //if( curCmd.nextCmdForHistory()!=null ){
      Hist h = new Hist();
      h.setView(view);
      h.setCommand(cmd/*curCmd.nextCmdForHistory()*/);
      history.add(h);
      log("===="+cmd/*curCmd.nextCmdForHistory()*/);
    //}

    return cmd;
  }

  public void cmdCompleted(int result) {
    Map<String,Object> view = w.view();
    history.setLastResult(result, view);

    resultAnalyse(result, view);
  }

  void findNoop(Map<String, Object> view){
    Hist thisH = new Hist(history.last, view, null);
    Hist cmp = thisH.prev;
    for( int level=1; level<=4; level++){
      if( cmp==null ){
        return;
      }
      boolean noop = cmp.viewMatch( thisH, false );
      if(noop){
        ResultsAnalyzer a = new NoopResultsAnalyzer(history, causes, level);
        findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
        break; // no use looking for deeper noops
      }
      cmp = cmp.prev;
    }
  }

  private void resultAnalyse(int res, Map<String, Object> view) {
    findNoop(view);

    Map<String,Object> nextViewAll = history.getNextViewAll(history.last);
    Hist hnext = new Hist(history.last, nextViewAll, null);
    causes.verifyAll(hnext);

    if( res>0 ){
      Hist h = history.last;
      List<String> lastCommands = new ArrayList<String>();
      for( int i=0; i<4; i++ ){
        if( h==null ){
          break;
        }
        lastCommands.add((String)h.getViewAll().get(Hist.CMD_KEY));
        h = h.prev;
      }
      if( lastCommands.size()==4 ){
        lastSuccessfulCommands.add(lastCommands);
      }
    }

    if( res!=0 ){
      // try to generate causes
      // if not already explained by other causes
      CauseMaxStruct cms = causePredict(history.last);
      if( cms.active.isEmpty() || cms.promisedMax()!=res ){
        ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, null);
        findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
      }

      // @todo if we have result - analyze not only last step, but also some steps before
    }else{
      // The essence of this prediction tree is so that we could smell the smack
      // of expected result as long before it occurs as only possible.
      // If we can smack it now and weren't able one step before -
      // try to find new relations to be able to track result
      // earlier.

      PredictionTree predictionTree = buildPredictionTree(history.last, view);
      PredictionTree predictionTreeOld=null;
      if( predictionTree.hasPositiveResultOrSmacks() ){
        predictionTreeOld = buildPredictionTree(history.last.prev, history.last.getViewOnly());
        if( !predictionTreeOld.hasPositiveResultOrSmacks() ){
          System.out.println("tree now smacks, tree old not");
          Map<String,Object> prediction = causes.predictAllViewByCauses(history.last);
          if( prediction==null ){
            prediction = new HashMap<String,Object>();
          }
          Map<String, Object> smack0 = predictionTree.getResultOrSmacksKeyView();
          if( !Utils.containsAll(prediction, smack0) ){
            // @todo State1 world: at every result +1 it looks from where f=RED appeared
            // and obviously finds no cause - it's random - how to avoid repeated search?
            // remember last steps to see they are the same? no! random 'r' and random 'f' colors
            // make that impossible. Statistically only?
            ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, smack0.keySet());
            findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
          }
        }
      }
      
//      Causes.SmacksOfResult smacks = causes.smacksOfResult(new Hist(history.last, view, null));
//      if( smacks !=null ){
//        Causes.SmacksOfResult smacksPrev = causes.smacksOfResult(history.last);
//        if( smacksPrev==null || !smacks.ds.equalsDS(smacksPrev.ds) ){
//          // there has just appeared new smack of result:
//          Map<String,Object> prediction = causes.predictAllViewByCauses(history.last);
//          if( prediction==null ){
//            prediction = new HashMap<String,Object>();
//          }
//          Map<String, Object> smack0 = smacks.ds.getElemsAtDepth(0);
//          if( !Utils.containsAll(prediction, smack0) ){
//            // @todo State1 world: at every result +1 it looks from where f=RED appeared
//            // and obviously finds no cause - it's random - how to avoid repeated search?
//            // remember last steps to see they are the same? no! random 'r' and random 'f' colors
//            // make that impossible. Statistically only?
//            ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, smack0.keySet());
//            findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
//          }
//        }
//      }
    }
  }

  private PredictionTree buildPredictionTree(Hist last, Map<String, Object> view) {
    PredictionTree predictionTree = new PredictionTree(last, view);
    predictionTree.smacks = causes.smacksOfResult(predictionTree.histNew());

    List<PredictionTree> readyNotes = Arrays.asList(predictionTree);
    for( int i=0; i<5 && readyNotes.size()>0; i++ ){
      List<PredictionTree> notesToExplore = new ArrayList<PredictionTree>();
      for( PredictionTree pti : readyNotes ){
        for( String c : allCommands() ){
          Hist h = new Hist(pti.histOld, pti.viewNext, c);
          Causes.PredictionBy prediction = causes.predictAllViewByCausesWithBy(h);
          if( prediction!=null ){
            Map<String,Object> test = new HashMap<String,Object>(prediction.view);
            test.remove(Hist.NOOP_KEY);
            if( "0".equals(""+test.get(Hist.RES_KEY)) ){
              test.remove(Hist.RES_KEY);
            }
            if( test.size()>0 ){ // if has smth. meaningful
              PredictionTree node = pti.addChild(h, c, prediction);
              node.smacks = causes.smacksOfResult(node.histNew());
              boolean val = prediction.view.get(Hist.RES_KEY)!=null && ((Integer)prediction.view.get(Hist.RES_KEY)).intValue()!=0;
              if( !val ){ // if result of this branch not yet known
                notesToExplore.add(node);
              }
            }
          }
        }
      }
      readyNotes = notesToExplore;
    }
    return predictionTree;
  }

  CmdSet findMaxCmdByCauses(Map<String,CauseMaxStruct> predictedMinResults, Map<String, Object> view){
    if( predictedMinResults.isEmpty() ){
      return null;
    }
    int max = Integer.MIN_VALUE;
    int min = Integer.MAX_VALUE;
    for( CauseMaxStruct cms : predictedMinResults.values() ){
      if( cms.hasResult() ){
        max = Math.max(max, cms.promisedMax());
        min = Math.min(min, cms.promisedMax());
      }
    }
    boolean useMax=false;
    boolean avoidAny=false;
    if( max>0 ){
      useMax=true;
    }else if( max<0 ){
      avoidAny=true;
    }else{
      assert( max==0 );
      if( min!=max ){
        useMax=true;
      }
    }

    if( avoidAny ){
      // not yet tested command is likely to have result of 0

      // so if there are other commands - choose ANY - it's better
      // than ignoring Cause results altogether
      List<String> cs =  new ArrayList<String>(allCommands());
      filterNoopCmds(view, cs);

      for( String c : predictedMinResults.keySet() ){
        cs.remove(c);
      }
      if( !cs.isEmpty() ){
        CmdSet cset = attempts.randomWithIntent(cs, view);
        cset.setFoundFrom("by causes - avoidAny");
        return cset;
      }
      return null;
    }

    if( useMax ){
      List<String> cmds = new ArrayList<String>();
      for( String s : predictedMinResults.keySet() ){
        if( predictedMinResults.get(s).promisedMax()==max ){
          cmds.add(s);
        }
      }
      if( max<=0 ){
        // also add commands with no info as yet
        for( String cmd : allCommands() ){
          if( !predictedMinResults.containsKey(cmd) ){
            cmds.add(cmd);
          }
        }
      }
      filterNoopCmds(view, cmds);
      CmdSet cset = attempts.randomWithIntent(cmds, view);
      cset.setTargetResult(max);
      cset.setFoundFrom("by causes - useMax "+min+".."+max+" rnd of "+cmds);
      return cset;
    }

    return null;
  }

//  <T> T rnd(List<T> l){
//    return l.get( (int)(Math.random()*l.size()) );
//  }

//  CmdSet findMaxCmd(Map<String,MaxStruct> predictedMinResults){
//    String cmd = null;
//    int max = Integer.MIN_VALUE;
//    for( String s : predictedMinResults.keySet() ){
//      MaxStruct str = predictedMinResults.get(s);
//      if( str.allmax>max ){
//        max = str.allmax;
//        if( max>0 ){
//          cmd = s;
//        }
//      }
//    }
//    if( cmd==null ){
//      return null;
//    }
//    CmdSet cset = new CmdSet(cmd);
//    cset.setTargetResult(max);
//    cset.setFoundFrom("findMaxCmd - direct history scan");
//    return cset;
//  }

//  CmdSet findMaxCmdPair(Map<String,MaxStruct> predictedMinResults2Cmd){
//    String cmd = null;
//    int baseSize=0;
//    int max = Integer.MIN_VALUE;
//    for( String s : predictedMinResults2Cmd.keySet() ){
//      MaxStruct struct = predictedMinResults2Cmd.get(s);
//      if( struct.allmax>max ){
//        max = struct.allmax;
//        if( max>0 ){
//          cmd = s;
//          baseSize = struct.baseSize;
//        }
//      }
//    }
//    if( cmd==null ){
//      return null;
//    }
//
//    CmdSet cset;
//    StringTokenizer st = new StringTokenizer(cmd, ",");
//    String nextCmd1 = st.nextToken();
//    String nextCmd2 = st.nextToken();
//    if( baseSize>1 ){
//      String group = nextCmd1+"_"+nextCmd2;
//      cmdGroups.put(group, Arrays.asList(nextCmd1, nextCmd2));
//      log(">>>>>> created cmdGroup "+group);
//      cset = new CmdSet(group);
//      cset.setFoundFrom("findMaxCmdPair - grp");
//    }else{
//      cset = new CmdSet(nextCmd1, nextCmd2);
//      cset.setFoundFrom("findMaxCmdPair - pair");
//    }
//    cset.setTargetResult(max);
//    return cset;
//  }

  List<String> allCommands(){
    List<String> ret = new ArrayList<String>();
    ret.addAll(w.commands());
    ret.addAll(cmdGroups.keySet());
    return ret;
  }

  private CmdSet calcNextCmd(Map<String, Object> view, boolean stepByStepFastCheck) {
    List<String> cs =  allCommands();

    CmdSet nextCmd;
    if( !stepByStepFastCheck && !byCausesOnly && Math.random()<experimentLevel ){
      // experimentLevel - emotional koef.
      // Low if we are before difficult controlled situation.
      // High if we are in panic.
      log("random cmd to avoid loops or stalls in already discovered domain");
      nextCmd = attempts.randomWithIntent(cs, view);
      return nextCmd;
    }

    Map<String,CauseMaxStruct> predictedByCauses = predictByCauses(view);
    nextCmd = findMaxCmdByCauses(predictedByCauses, view);
    if( nextCmd!=null ){
      log(/*"predicted positive result with causes, get it! "+*/nextCmd.getFoundFrom());
      return nextCmd;
    }

//    if( !zeroForAllCmds(predictedByCauses) ){ // don't run complex computations if 0 already predicted by causes
//      /*
//      @todo sometimes there is unpleasant situation: 0 WRONGLY predicted by causes
//       no directed exploration attempts are made - and it takes too long to hit the
//       correct answer by random attempts
//       */
//      Map<String,MaxStruct> predictedMinResults = predictMinResults(view);
//      nextCmd = findMaxCmd(predictedMinResults);
//      if( nextCmd!=null ){
//        log("predicted positive result, get it!");
//        return nextCmd;
//      }
//    }

//    first of all, just do a command if it takes us one step closer to result!
//    even if result not yet precisely predicted!

    // if there is step which smacks of result according to causes, do it
    Set<String> smacksCmds = new HashSet<String>();
    for( String c : cs ){
      Hist ifCmdC = new Hist(history.last, view, c);
      Map<String,Object> next = causes.predictAllViewByCauses(ifCmdC);
      Hist nextHist = new Hist(ifCmdC, next, null);
      Causes.SmacksOfResult smacks = causes.smacksOfResult(nextHist);
      if( smacks!=null && smacks.ds.hasElemsOtherThan(new ViewDepthElem(0, Hist.CMD_KEY))
              && smacks.ds.hasElemsAtDepth(0)
              && smacks.cause.getResult()>0 ){
        smacksCmds.add(c);
      }
    }
    if( !smacksCmds.isEmpty() && smacksCmds.size()<cs.size() ){
      return attempts.randomWithIntent(new ArrayList<String>(smacksCmds), view);
    }

//    Map<String,MaxStruct> predictedMinResults2Cmds = predictMinResults2Cmds(view);
//    nextCmd = findMaxCmdPair(predictedMinResults2Cmds);
//    if( nextCmd!=null ){
//      log("predicted positive result with 2 commands ("+nextCmd+"), get it!");
//      return nextCmd;
//    }

    if( stepByStepFastCheck ){
      return null;
    }

    List<String> cset = new ArrayList<String>(cs);

    filterNoopCmds(view, cset);
    if( cset.size()==0 ){
      // 0.1 - emotional koef.
      // Low if we are before difficult controlled situation.
      // High if we are in panic.
      nextCmd = attempts.randomWithIntent(cs, view);
    }else{
      nextCmd = attempts.randomWithIntent(cset, view);
    }
    return nextCmd;
  }



  Map<String,CauseMaxStruct> predictByCauses(Map<String, Object> view) {
    Map<String,CauseMaxStruct> ret = new HashMap<String,CauseMaxStruct>();
    List<String> cs =  allCommands();
    for( String c : cs ){
      Hist ifCmdC = new Hist(history.last, view, c);
      CauseMaxStruct m = causePredict(ifCmdC);
      if( m.active.size()>0 ){
        ret.put(c, m);
      }
    }
    return ret;
  }

  private CauseMaxStruct causePredict(Hist ifCmdC) {
    CauseMaxStruct m = new CauseMaxStruct();
    for( Cause cause : causes.list ){
      if( cause.canPredict(ifCmdC) && cause.valid() ){
         m.active.add(cause);
      }
    }
    return m;
  }

  boolean zeroForAllCmds(Map<String,CauseMaxStruct> m){
    List<String> cs =  allCommands();
    for( String c : cs ){
      if( !m.containsKey(c) ){
        return false;
      }
      if( m.get(c).promisedMax()!=0 ){
        return false;
      }
    }
    return true;
  }

  boolean explains(Cause newc, List<Cause> existsing){
    for( Cause c : existsing ){
      if( c.explains(newc) ){
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

  /**
   * Explores direct results of situations similar to histVariant,
   * if results are consistent and interesting (useful), new cause is created
   * @param vdi
   * @param histVariant
   * @return
   */
  private void findRelations(ViewDepthIterator vdi, Hist histVariant, ResultsAnalyzer analyzer) {
    SkippingViewDepthIterator svdi = new SkippingViewDepthIterator(vdi);
    int maxFound=0;
    Cause bestCause=null;

    for(;;){
      ViewDepth vd = svdi.next();
      if( vd==null ){
        break;
      }
      if( !vd.canUse(history) ){
        continue;
      }
      List<DeepState> list = DeepState.lookBehind(vd, histVariant);
      if( list.isEmpty() ){
        continue;
      }
      DeepState ds = list.get(0);
      List<Hist> found = history.find(ds);

      if(!found.isEmpty()){
        Cause newc = analyzer.test(found, ds);
        if( newc!=null && found.size()>maxFound ){
          maxFound = found.size();
          bestCause = newc;
        }
      }else{
        svdi.addSkipPattern(vd); // nothing found, no longer try any similar
      }
    }

    if( bestCause!=null ){ // bestcause likely exists and will be accepted as it explains new data
      if( causes.newCause(bestCause) ){
        causes.generalize(history);
      }
    }

  }

  void printFound(List<Hist> found, int depth){
    for( Hist h : found ){
      log("found");
      Hist hi=h;
      for( int i=0; i<depth && hi!=null; i++ ){
        log(""+hi);
        hi=hi.prev;
      }
    }
  }

//  Map<String, MaxStruct> predictMinResults2Cmds(Map<String, Object> view) {
//    Map<String, MaxStruct> ret = new HashMap<String, MaxStruct>();
//    List<String> cs = allCommands();
//    for (String c : cs) {
//      for (String c2 : cs) {
//        MaxStruct allmax = new MaxStruct();
//        for (StateDepth sd : StateDepth.TRACKABLES) {
//          if (!sd.canUse(history)) {
//            continue;
//          }
//          if( sd.contains("V0") ){ // we don't know what V0 will be after command 'c'
//            continue;
//          }
//          Hist project = new Hist( new Hist(history.last, view, c), null, c2 );
//          DeepState ds = DeepState.lookBehind(sd, project).get(0);
//
//          List<Hist> found = history.find(ds);
//          if (!found.isEmpty()) {
//            int min = Integer.MAX_VALUE;
//            for (Hist h : found) {
//              min = Math.min(min, history.getResult(h));
//            }
//            allmax.recMax(min, ds, found.size());
//          }
//
//          if (allmax.valid()) {
//            if( !cmdGroups.containsKey(c+"_"+c2) ){ // don't count already grouped command separately
//              if(allmax.allmax!=0){
//                breakPoint();
//              }
//              ret.put(c + "," + c2, allmax);
//            }
//          }
//        }
//      }
//    }
//    return ret;
//  }

  void breakPoint(){
    
  }

  private void filterNoopCmds(Map<String, Object> view, List<String> cset) {
    //List<String> cs =  allCommands();
    for( String c : cset ){
      Hist hnow = new Hist(history.last, view, c);
//      for( StateDepth sd : StateDepth.TRACKABLES ){
//        if( !sd.canUse(history) ){
//          continue;
//        }
//        DeepState ds = DeepState.lookBehind(sd, hnow).get(0);
//        List<Hist> found = history.find(ds);
//        boolean allNoop = true;
//        for( Hist h : found ){
//          if( !sd.noop(h) ){
//            allNoop=false;
//          }
//        }
        CauseMaxStruct cms = causePredict(hnow);
        if( cms.noop() ){
          cset.remove(c);
          log("removing noop "+c+" - "+cms.toString());
          break;
        }
//      }
    }
  }


  public void setByCausesOnly(boolean byCausesOnly) {
    this.byCausesOnly = byCausesOnly;
  }

  void log(String s){
    System.out.println(s);
  }
}
