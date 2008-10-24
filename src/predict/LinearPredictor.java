package predict;

import mem.OneView;

/**
 * Automatically chains OneViews - useful for testing
 */
public class LinearPredictor {
  Predictor p = new Predictor();
  OneView last;

  void add(OneView v) {
    v.prev = last;
    if (last != null) {
      last.next = v;
    }
    last = v;
    p.add(v);
  }

  void printRules(String elem){
    p.printRules(elem);
  }

  OneView predict() {
    return p.predictNext(last);
  }

  public OneView getLast() {
    return last;
  }


  public Predictor getPredictor() {
    return p;
  }
}
