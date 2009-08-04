package reinforcement;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Aug 4, 2009
 * Time: 1:41:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class DamperAvg extends Damper{
  @Override
  void add(double val) {
    vals.add(val);
  }

  @Override
  double value() {
    return value(vals);
  }
}
