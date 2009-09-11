package neuro;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 7, 2009
 * Time: 11:49:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class HebbianMain {
  public static void main(String[] args){
    new HebbianMain().doit();
  }

  List<Sensor> sensors = new ArrayList<Sensor>();
  List<Neuron> level1 = new ArrayList<Neuron>();
  double[] pat1 = {0,0,1,1,1, 0,0,0,0,0};
  double[] pat2 = {0,0,0,1,1, 0,0,1,0,0};

  private void doit() {
    for( int i=0; i<10; i++ ){
      sensors.add(new Sensor());
    }
    for( int i=0; i<5; i++ ){
      Neuron n = new Neuron(sensors, 0.5);
      level1.add(n);
    }
    for( int t=0; t<100; t++ ){
      if( t%20==0 ){
        System.out.println("---------------");
      }
      String type = "A";
      double[] pat = pat1;
      if( t%20<10 ){
        pat = pat2;
        type="B";
      }

      for( int i=0; i<sensors.size(); i++ ){
        double str = 0;//Math.random()*0.05;
        str += pat[i];//*(1+Math.random()*0.2)/1.2;
        sensors.get(i).setStrength(str);
        System.out.print(sensors.get(i).fired01());
      }
      System.out.println();
      System.out.print("L1 "+type+" ");
      for( int i=0; i<level1.size(); i++ ){
        level1.get(i).update();
        System.out.print(level1.get(i).fired01());
      }
      System.out.println();
    }
    System.out.println();
  }
}
