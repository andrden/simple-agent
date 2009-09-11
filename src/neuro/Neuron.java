package neuro;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 7, 2009
 * Time: 11:51:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class Neuron extends AbstrNeuron{
  List<? extends AbstrNeuron> dendrites;
  double[] synapses;
  double threshold = 0.3;

  public Neuron(List<? extends AbstrNeuron> dendrites) {
    init(dendrites);
  }

  private void init(List<? extends AbstrNeuron> dendrites) {
    this.dendrites = dendrites;
    synapses = new double[dendrites.size()];
    for( int i=0; i<synapses.length; i++ ){
      synapses[i]=0.1;
    }
  }

  public Neuron(List<? extends AbstrNeuron> possibleSources, double rndRatio) {
    List<AbstrNeuron> connected = new ArrayList<AbstrNeuron>();
    for( AbstrNeuron n : possibleSources ){
      if( Math.random()<rndRatio ){
        connected.add(n);
      }
    }
    init(connected);
  }

  void update(){
    fired = calcFired();
    if( fired ){
      for( int i=0; i<dendrites.size(); i++ ){
        if( dendrites.get(i).isFired() ){
          synapses[i] = synapses[i] + (0.3-synapses[i])*0.1;
        }
      }
    }
  }

  private boolean calcFired() {
    double sum=0;
    for( int i=0; i<dendrites.size(); i++ ){
      if( dendrites.get(i).isFired() ){
        sum += synapses[i];
      }
    }
    if( sum>=threshold ){
      return true;
    }
    return false;
  }
}
