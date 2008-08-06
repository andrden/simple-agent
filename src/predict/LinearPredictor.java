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

  OneView predict() {
    return p.predictNext(last);
  }

}
