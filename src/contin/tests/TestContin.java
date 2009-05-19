package contin.tests;

import junit.framework.TestCase;
import contin.alg.ContinAlg;
import contin.Command;
import contin.World;

import java.util.Map;

public class TestContin extends TestCase {
  public void testWorld1(){
    World1 w = new World1();
    wTest(w);
  }

  public void testWorld01(){
    World w = new World01();
    wTest(w);
  }

    private void wTest(World w) {
        ContinAlg alg = new ContinAlg(w.commands());
        for(int i=0; i<200; i++){
          Command cmd = alg.suggestCmd();
          w.command(cmd.getCommand(), cmd.getForce());
          Map<String,Double> senses = w.senses();
          double sumS = sum(senses);
          if( i>100 && sumS <-0.1 ){
            fail("step "+i+ " senses="+senses);
          }
          alg.cmdExecuted(cmd, w.view(), senses);
        }
    }

    double sum(Map<String,Double> senses){
    double r=0;
    for( Double d : senses.values() ){
      r+=d;
    }
    return r;
  }
}
