package predict.singletarget;

import predict.PredictorIntf;
import mem.OneView;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 29/7/2008
 * Time: 11:54:45
 */
public class Pred implements PredictorIntf {
  //List<OneView> hist = new ArrayList<OneView>();
  Map<Target,TargetHist> singles = new HashMap<Target,TargetHist>();

  public void add(OneView v) {
    //hist.add(v);
    //int idx = hist.size()-1;
    if( v.prev!=null /*idx>0*/ ){
      Map<String, Object> m = v.getViewAll();
      for( String s : m.keySet() ){
        Target t = new Target(s, m.get(s));
        TargetHist th = singles.get(t);
        if( th==null ){
          th = new TargetHist();
          singles.put(t, th);
        }
        th.examples.add(v.prev);
      }
    }
  }

  public OneView predictNext(OneView v) {
    return null;
  }
}
