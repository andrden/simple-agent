package neuro;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 7, 2009
 * Time: 11:53:57 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstrNeuron {
  boolean fired;

  final boolean isFired() {
    return fired;
  }

  int fired01(){
    return isFired()?1:0;
  }
}
