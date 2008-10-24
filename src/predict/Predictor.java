package predict;

import mem.OneView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 17:59:55
 */
public class Predictor implements PredictorIntf {
  List<PredictorIntf> algs;

  public Predictor() {
    algs = Arrays . <PredictorIntf> asList(
            new predict.singletarget.Pred() /*,
            new predict.singlesensor.Pred()   */
    );
  }

  public void printRules(String elem) {
    for( PredictorIntf i : algs ){
      i.printRules(elem);
    }
  }

  public void appendValsToLastView(Map<String, Object> sensors) {
    for (PredictorIntf i : algs) {
      i.appendValsToLastView(sensors);
    }
  }

  public void add(OneView v) {
    for (PredictorIntf i : algs) {
      i.add(v);
    }
  }

  public OneView predictNext(OneView v) {
    OneView vnext = new OneView();
    vnext.prev = v;
    for (PredictorIntf i : algs) {
      OneView vi = i.predictNext(v);
      vnext.mergeByAddNew(vi);
    }
    return vnext;
  }


}
