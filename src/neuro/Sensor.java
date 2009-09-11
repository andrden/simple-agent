package neuro;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 7, 2009
 * Time: 11:54:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class Sensor extends AbstrNeuron{
  double strength;


  public void setFired(boolean fired) {
    this.fired = fired;
  }

  public void setStrength(double strength) {
    this.strength = strength;
    fired = Math.random()<strength;
  }
}
