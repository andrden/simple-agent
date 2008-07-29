package predict;

import mem.OneView;

import java.util.List;
import java.util.Arrays;

import predict.singletarget.Pred;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 17:59:55
 */
public class Predictor implements PredictorIntf {
  List<PredictorIntf> algs;

  public Predictor() {
    algs = (List)Arrays.asList(
      new Pred()
    );
  }

  public void add(OneView v) {
    for( PredictorIntf i : algs ){
      i.add(v);
    }
  }

  public OneView predictNext(OneView v) {
    OneView vnext = new OneView();
    for( PredictorIntf i : algs ){
      OneView vi = i.predictNext(v);
      vnext.mergeByAddNew(vi);
    }
    return vnext;
  }
}
