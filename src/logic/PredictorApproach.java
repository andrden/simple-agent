package logic;

import predict.Predictor;
import predict.CmdPredictionTree;
import predict.PredictionTreeBuilder;
import predict.singletarget.SensorHist;
import mem.Hist;
import mem.OneView;

import java.util.*;

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

  public PredictorApproach() {
  }

  public List<SensorHist> goodNextCmdsWithPositiveRules(){
    List<SensorHist> ret = new ArrayList<SensorHist>();
    for( SensorHist s : goodNextCmd.values() ){
      if( s.hasRulesForVal("+") ){
        ret.add(s);
      }
    }
    return ret;
  }

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

  public CmdSet suggestCmd(Hist next, List<String> possibleCommands) {
    List<String> bcmd = predictGoodNextCmd(next);
    if( bcmd!=null ){
      CmdSet cc2 = new CmdSet(bcmd.get(0));
      cc2.setFoundFrom("from sequence " + bcmd);
      return cc2;
    }

    //Prediction system knows that Ep at YELLOW causes ORANGE long before
    //goodCmd encounters such happy event. We need to use that!
    if( next!=null ){
      CmdPredictionTree tree = new PredictionTreeBuilder(predictor, possibleCommands, 1)
              .build(next);
      for( String c : possibleCommands ){
        OneView v = tree.viewOnCommand(c);
        List<String> bcmd2 = predictGoodNextCmd(v);
        if( bcmd2!=null ){
          CmdSet cc2 = new CmdSet(c);
          cc2.setFoundFrom("from sequence " + bcmd2 + " after prediction on " + c);
          return cc2;
        }
      }
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
}
