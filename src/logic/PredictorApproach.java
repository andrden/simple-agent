package logic;

import predict.Predictor;
import predict.CmdPredictionTree;
import predict.PredictionTreeBuilder;
import predict.singletarget.SensorHist;
import mem.Hist;
import mem.OneView;

import java.util.*;
import java.io.Serializable;

import utils.Utils;
import com.pmstation.common.utils.MinMaxFinder;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/8/2008
 * Time: 18:04:22
 */
public class PredictorApproach implements Approach{
  Predictor predictor = new Predictor();
  Map<String,SensorHist> goodNextCmd = new HashMap<String,SensorHist>();
  LinkedList<Hist> lastSteps = new LinkedList<Hist>();

  Plan currentPlan = null;
  class Plan implements Serializable {
    List<String> cmds;
    int pos=0;
    String foundFrom;

    Plan(List<String> cmds, String foundFrom) {
      this.cmds = cmds;
      this.foundFrom = foundFrom;
    }

    CmdSet nextCmdSet(){
      String desc = cmds.toString();
      String cmd = next();
      CmdSet cs = new CmdSet(cmd);
      cs.setFoundFrom("currentPlan "+desc+" "+foundFrom);
      return cs;
    }

    String next(){
      String c = cmds.get(pos);
      pos++;
      if( pos>=cmds.size() ){
        currentPlan=null;
      }
      return c;
    }
  }

  public PredictorApproach() {
  }

//  public List<SensorHist> goodNextCmdsWithPositiveRules(){
//    List<SensorHist> ret = new ArrayList<SensorHist>();
//    for( SensorHist s : goodNextCmd.values() ){
//      if( s.hasRulesForVal("+") ){
//        ret.add(s);
//      }
//    }
//    return ret;
//  }

  SensorHist goodNextCmd(String cmd){
    SensorHist s = goodNextCmd.get(cmd);
    if( s==null ){
      s = new SensorHist(cmd);
      s.setSkippedViewKeys(Collections.singleton(Hist.CMD_KEY));
      goodNextCmd.put(cmd, s);
    }
    return s;
  }

  List<String> predictGoodNextCmd(OneView next){
    List<String> bestSec=null;
    StringBuilder sb = new StringBuilder();
    for( SensorHist s : goodNextCmd.values() ){

//      - predict() here need be replaced with predictOrConflict()
//      - if we have a conflicting set of rules we need to check that,
//      APPLY that cmd to IMPROVE our MODEL
//
//      E - had conflicting + and -

      if( s.valsSize()>1 /* has alternatives */ &&  s.valAcceptedByRules(next, "+") ){
        List<String> sec = splitCmdSec(s.getSensorName());
        if( bestSec==null || bestSec.size() > sec.size() ){
          bestSec = sec;
        }
        sb.append(", "+s.getSensorName());
      }
    }
    //System.out.println("predictGoodNextCmd()="+sb);
    return bestSec;
  }

  /**
   * Returns true if no objections
   * @param next
   * @param cmd
   * @return
   */
  public boolean assessCmd(Hist next, String cmd) {
    if( next==null ){
      return true;
    }

    OneView v = next.cloneBranch();
    v.pt(Hist.CMD_KEY, cmd);
    OneView pred = predictor.predictNext(v);

    Integer res = (Integer) pred.get(Hist.RES_KEY);
    if( res!=null && res<0 ){
      return false;
    }

    if( pred.viewMatch(next.getViewAll(), Collections.singleton(Hist.CMD_KEY)) ){
      return false; // noop depth 1
    }

    return true;
  }

  public void printCmdPredictions(Hist next, List<String> possibleCommands){
    CmdPredictionTree tree = new PredictionTreeBuilder(predictor, possibleCommands, 1)
            .build(next);
    for( String c : possibleCommands ){
      OneView v = tree.viewOnCommand(c);

      Map<String, Object> m = next.getViewAll();
      m.remove(Hist.CMD_KEY);
      String vstr;
      if( m.toString().equals(v.getViewAll().toString()) ){
        vstr = "noop";
      }else{
        vstr = v.toString();
      }
      System.out.println("On "+c+": "+v.getViewAll().size()+" "+vstr);
    }
    predictor.printRuleStats();
  }

  public CmdSet suggestCmd(Hist next, List<String> possibleCommands) {
    if( currentPlan!=null ){
      return currentPlan.nextCmdSet();
    }
    if( next==null ){
      return null;
    }

    // If there is time we should try to calc. the best command
    // using the most recent data rules. Only in time-critical situations
    // or when no clear result can be calculated we use pre-learned command
    // sequences.
    //Prediction system knows that Ep at YELLOW causes ORANGE long before
    //goodCmd encounters such happy event. We need to use that!

    MinMaxFinder<Plan> shortestPlan = new MinMaxFinder<Plan>();

    List<String> bcmd = predictGoodNextCmd(next);
    if( bcmd!=null ){
      shortestPlan.add(bcmd.size(), new Plan(bcmd, "direct predictGoodNextCmd"));
    }

    CmdPredictionTree tree = new PredictionTreeBuilder(predictor, possibleCommands, 1)
            .build(next);

    MinMaxFinder<String> minPredicted = new MinMaxFinder<String>();
    List<String> conflictingPredictionCommands = new ArrayList<String>();
    for( String c : possibleCommands ){
      if( tree.conflictingPredictionOnCommand(c) ){
        conflictingPredictionCommands.add(c);
      }
      OneView v = tree.viewOnCommand(c);
      int countKnown = 0;
      if( v != null ){
        Map<String, Object> m = v.getViewAll();
        m.remove(Hist.CMD_KEY); // don't count predicted Cmd as known system state
        countKnown = m.size();

        Integer rr = Hist.getResult(v.getViewAll());
        if( rr!=null && rr>0 ){
          shortestPlan.add(1, new Plan(Arrays.asList(c),"from res>0 after prediction on " + c));
        }
        
        List<String> bcmd2 = predictGoodNextCmd(v);
        if( bcmd2!=null ){
          shortestPlan.add(1+bcmd2.size(),
              new Plan(Arrays.asList(c), "from sequence " + bcmd2 + " after prediction on " + c));
        }
      }
      minPredicted.add(countKnown, c);
    }

    if( !shortestPlan.getMinNames().isEmpty() ){
      currentPlan = Utils.rnd(shortestPlan.getMinNames());
      return currentPlan.nextCmdSet();
    }

    //====== random attempts below: ======

    if( !conflictingPredictionCommands.isEmpty() &&
        conflictingPredictionCommands.size()!=possibleCommands.size() ){
      String rndCmd = Utils.rnd(conflictingPredictionCommands);
      CmdSet cs = new CmdSet(rndCmd);
      cs.setFoundFrom("conflictingPredictionCommands - rnd of "+conflictingPredictionCommands);
      return cs;
    }

    List<String> minPredCmds = minPredicted.getMinNames();
    if( minPredCmds.size()!=possibleCommands.size() ){
      String rndCmd = Utils.rnd(minPredCmds);
      CmdSet cs = new CmdSet(rndCmd);
      cs.setFoundFrom("min predicted - rnd "+minPredCmds+" view="+tree.viewOnCommand(rndCmd));
      return cs;
    }


//    CmdPredictionTree.PositiveResultOrSmack positiveRes = null;
//    if( next!=null ){
//      CmdPredictionTree tree = new PredictionTreeBuilder(predictor, possibleCommands)
//              .build(next);
//      positiveRes = tree.findPositiveResultOrSmacks();
//    }
//    if( positiveRes!=null && positiveRes.getCmd()!=null ){
//      CmdSet cc2 = new CmdSet(positiveRes.getCmd());
//      cc2.setFoundFrom("using CmdPredictionTree smacks " /*+ smackRes2.description*/);
//      return cc2;
//    }
    return null;
  }

    public String predictionInfo(Hist curr) {
        return ""+predictor.predictNext(curr);
    }

    public void appendValsToLastView(Map<String, Object> sensors) {
        predictor.appendValsToLastView(sensors);
    }

    String cmdSeq(List<Hist> steps){
      StringBuilder sb = new StringBuilder();
      for( Hist h : steps ){
        sb.append(" "+h.prev.getCommand());
      }
      return sb.toString();
    }

    List<SensorHist> cmdSeqsStartingWith(String cmd){
      List<SensorHist> ret =  new ArrayList<SensorHist>();
      for( String s : goodNextCmd.keySet() ){
        if( splitCmdSec(s).get(0).equals(cmd) ){
          ret.add(goodNextCmd.get(s));
        }
      }
      return ret;
    }

    List<String> splitCmdSec(String cmdSec){
      StringTokenizer st = new StringTokenizer(cmdSec," ");
      List<String> ret = new ArrayList<String>();
      while(st.hasMoreTokens()){
        ret.add(st.nextToken());
      }
      return ret;
    }

    public void add(Hist next) {
        predictor.add(next);

        if( next.prev!=null ){

//          @todo Commands can lead to result, but with different step count.
//          So duplicating Ep is treeted as good here.
//          How to optimize?

//          Also right sequence beginning can be finished wrongly - and then
//          right commands get into '-' category. Priorities?

          lastSteps.addLast(next);


          for( int i=lastSteps.size()-1; i>=0; i-- ){
            String sec = cmdSeq(lastSteps.subList(i, lastSteps.size()));
            if( next.getResult()>0 ){
              SensorHist s = goodNextCmd(sec);
              s.add("+", lastSteps.get(i));
              negateCmdSeqsStarting(sec, lastSteps.get(i));
            }else{
              SensorHist s = goodNextCmd.get(sec);
              if( s!=null ){
                s.add("-", lastSteps.get(i));
              }
            }
          }

          boolean noop = next.viewMatch(next.prev, false);
          if( next.getResult()<0 || noop){
            String cmd = next.prev.getCommand();
            List<SensorHist> seqs = cmdSeqsStartingWith(cmd);
            for( SensorHist s : seqs ){
              s.add("-", next);
            }
          }

          if( next.getResult()>0 ){
            lastSteps.clear();
          }else{
            if( lastSteps.size()>5 ){
              Hist old = lastSteps.removeFirst();
            }
          }

        }
    }

  void negateCmdSeqsStarting(String sec, OneView v){
    for( String g : goodNextCmd.keySet() ){
      if( g.startsWith(sec + " ") ){
        // don't call this seq. a 'resultative' despite result occurs during its
        // execution - there is a shorter one we should use in future
        goodNextCmd.get(g).add("+-", v);
      }
    }
  }

  private static final long serialVersionUID = -7282071316883621857L;

}
