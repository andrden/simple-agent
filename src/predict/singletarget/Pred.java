package predict.singletarget;

import mem.OneView;
import predict.PredictorIntf;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 29/7/2008
 * Time: 11:54:45
 */
public class Pred implements PredictorIntf {
  Map<String, SensorHist> singles = new HashMap<String, SensorHist>();

  OneView lastOneView;

  public void printRules(String elem) {
    SensorHist sh = singles.get(elem);
    if( sh!=null ){
      sh.printRules();
    }
  }

  public void printRules(){
//    for( String s : singles.keySet() ){
//      log( " === " + s);
//      SensorHist sh = singles.get(s);
//      for( Object val : sh.vals.keySet() ){
//        TargetHist th = sh.vals.get(val);
//        if( !th.rules.isEmpty() ){
//          log(val+" when "+th.rules+" unexpl="+th.unexpainedExamples().size());
//        }
//      }
//    }
  }

  void log(String s){
    System.out.println(s);
  }

  public void appendValsToLastView(Map<String, Object> sensors) {
    add(sensors);
  }

  public void add(OneView v) {
    lastOneView=v;
    if (v.prev != null) {
      Map<String, Object> m = v.getViewAll();
      add(m);
    }
  }

  private void add(Map<String, Object> m) {
    for (String s : m.keySet()) {
      SensorHist th = singles.get(s);
      if (th == null) {
        th = new SensorHist(s);
        singles.put(s, th);
      }
      th.add(m.get(s), lastOneView);
    }
  }

  public OneView predictNext(OneView v) {
    OneView ret = new OneView();
    for (String t : singles.keySet()) {
      SensorHist th = singles.get(t);
      Object val = th.predict(v);
      if (val != null) {
        ret.pt(t, val);
      }
    }
    return ret;
  }
}
