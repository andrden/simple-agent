package logic;

import predict.Predictor;
import predict.CmdPredictionTree;
import predict.PredictionTreeBuilder;
import mem.Hist;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/8/2008
 * Time: 18:04:22
 */
public class PredictorApproach implements Approach{
  Predictor predictor = new Predictor();


  public CmdSet suggestCmd(Hist next, List<String> possibleCommands) {
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
    }
}
