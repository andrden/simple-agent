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
public class PredictorApproach{
  static final int TREE_CALC_DEPTH=4;
  Predictor predictor = new Predictor();
  Map<String,SensorHist> goodNextCmd = new HashMap<String,SensorHist>();
  LinkedList<Hist> lastSteps = new LinkedList<Hist>();
  String prevRuleStats="";

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

  List<String> filterSenselessCmds(CmdPredictionTree tree,
                                   Hist next, List<String> possibleCommands){
    if( next==null ){
      return possibleCommands;
    }
    List<String> ret = new ArrayList<String>();
    for( String c : possibleCommands ){
      OneView v = tree.viewOnCommand(c);
      if( v!=null ){
        if( tree.branchOnCommand(c).noopDetected() ){
          continue;
        }

        Integer res = (Integer) v.get(Hist.RES_KEY);
        if( res!=null && res<0 ){
          continue;
        }
      }

      ret.add(c);
    }
    return ret;
  }


  public void printCmdPredictions(Hist next, List<String> possibleCommands){
    CmdPredictionTree tree;
    if( next==null ){
      tree = new CmdPredictionTree(next);
    }else{
      tree = new PredictionTreeBuilder(predictor, possibleCommands, TREE_CALC_DEPTH)
            .build(next);
    }
    for( String c : possibleCommands ){
      OneView v = tree.viewOnCommand(c);
      CmdPredictionTree branch = tree.branchOnCommand(c);
      String vstr;
      String cmdDescr = "On " + c + ": ";
      if( branch==null ){
        cmdDescr += " ? ";
      }else{
        if( branch.noopDetected() ){
          vstr = branch.toString(); // "noop";
        }else{
          vstr = v.toString();
        }
        cmdDescr +=  + v.getViewAll().size() + " " + vstr + " " + v.toString();
      }
      System.out.println(cmdDescr);
    }
    String ruleStats = predictor.ruleStats();
    System.out.println("RuleStats:"+ruleStats);
  }

  public SuggestionResult suggestCmd(final Hist next, final List<String> possibleCommands) {
    SuggestionResult sugg = new SuggestionResult();
    if( currentPlan!=null ){
      sugg.cmdSet = currentPlan.nextCmdSet();
      return sugg;
    }
    if( next==null ){
      sugg.csFiltered = possibleCommands;
      return sugg;
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

    CmdPredictionTree tree = new PredictionTreeBuilder(predictor, possibleCommands, TREE_CALC_DEPTH)
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
      sugg.cmdSet = currentPlan.nextCmdSet();
      return sugg;
    }

    //====== random attempts below: ======

    if( !conflictingPredictionCommands.isEmpty() &&
        conflictingPredictionCommands.size()!=possibleCommands.size() ){
      String rndCmd = Utils.rnd(conflictingPredictionCommands);
      CmdSet cs = new CmdSet(rndCmd);
      cs.setFoundFrom("conflictingPredictionCommands - rnd of "+conflictingPredictionCommands);
      sugg.cmdSet = cs;
      return sugg;
    }

    String ruleStats = predictor.ruleStats();
//    don't repeat if last step mispred=0,
//    or maybe need count all convergent rules, not pruned list
//    to know when really change occured
//    if( !ruleStats.equals(prevRuleStats) ){
//      // last cmd changed ruleStats, try to repeat that command
//      CmdSet cs = new CmdSet(next.prev.getCommand());
//      cs.setFoundFrom("ruleStats changed cmd repeat");
//      sugg.cmdSet = cs;
//      return sugg;
//    }

    List<String> minPredCmds = minPredicted.getMinNames();
    if( minPredCmds.size()!=possibleCommands.size() ){
      String rndCmd = Utils.rnd(minPredCmds);
      CmdSet cs = new CmdSet(rndCmd);
      cs.setFoundFrom("min predicted - rnd "+minPredCmds+" view="+tree.viewOnCommand(rndCmd));
      sugg.cmdSet = cs;
      return sugg;
    }

    sugg.csFiltered = filterSenselessCmds(tree, next, possibleCommands);
    return sugg;
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
      prevRuleStats = predictor.ruleStats();
      logMispredictions(next);

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

  private void logMispredictions(Hist next) {
    if( next.prev!=null ){
      OneView vpred = predictor.predictNext(next.prev);
      String cmd = next.prev.getCommand();
      StringBuilder s = new StringBuilder();
      int mis=0;
      for( String k : next.getViewAll().keySet() ){
        if( vpred==null || !next.get(k).equals(vpred.get(k)) ){
          s.append(k+" ");
          mis++;
        }
      }
      s.insert(0, cmd+" misPred "+mis+": ");
      System.out.println(s);
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
