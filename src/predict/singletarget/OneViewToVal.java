package predict.singletarget;

import mem.OneView;

public interface OneViewToVal {
  /**
   *
   * @param v
   * @return sensor value obtained after seeing view v -
   * in case of multisensor predictor - sensor value in next view
   */
  Object val(OneView v);
}
