package logic;

import predict.Predictor;
import predict.CmdPredictionTree;
import predict.PredictionTreeBuilder;
import predict.singletarget.SensorHist;
import mem.Hist;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/8/2008
 * Time: 18:04:22
 */
public class PredictorApproach implements Approach{
  Predictor predictor = new Predictor();
  Map<String,SensorHist> goodNextCmd = new HashMap<String,SensorHist>();

  public PredictorApproach() {
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

  void predictGoodNextCmd(Hist next){
    StringBuilder sb = new StringBuilder();
    for( SensorHist s : goodNextCmd.values() ){
      if( "+".equals(s.predict(next)) ){
        sb.append(" "+s.getSensorName());
      }
    }
    System.out.println("predictGoodNextCmd()="+sb);
  }

  public CmdSet suggestCmd(Hist next, List<String> possibleCommands) {
    predictGoodNextCmd(next);

    CmdPredictionTree.PositiveResultOrSmack positiveRes = null;
    if( next!=null ){
      CmdPredictionTree tree = new PredictionTreeBuilder(predictor, possibleCommands)
              .build(next);
      positiveRes = tree.findPositiveResultOrSmacks();
    }
    if( positiveRes!=null && positiveRes.getCmd()!=null ){
      CmdSet cc2 = new CmdSet(positiveRes.getCmd());
      cc2.setFoundFrom("using CmdPredictionTree smacks " /*+ smackRes2.description*/);
      return cc2;
    }
    return null;
  }

    public String predictionInfo(Hist curr) {
        return ""+predictor.predictNext(curr);
    }

    public void appendValsToLastView(Map<String, Object> sensors) {
        predictor.appendValsToLastView(sensors);
    }

    public void add(Hist next) {
        predictor.add(next);

        if( next.prev!=null ){
          SensorHist s = goodNextCmd(next.prev.getCommand());
          if( next.getResult()>0 ){
            s.add("+",next);
          }else{
            s.add("-",next);
          }
        }
      /*
          if( next.getResult()>0 ){
            goodNextCmd.add(next.prev.getCommand(), next);
          }else{
//            not sure E would be wrong here! can't state "" as cmd
//            need some way to state just that "next.prev.getCommand() was NOT good here"
            goodNextCmd.add("", next);
          }
          */
    }
}
