package predict;

import org.junit.Test;
import junit.framework.TestCase;
import mem.OneView;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 18:15:26
 */
public class TestPredictor extends TestCase {

  public void test1() {
    Predictor p = new Predictor();
    p.add(new OneView().pt("a","1"));
    p.add(new OneView().pt("a","1"));
    assertEquals("1",p.predict().get("a"));
  }

  public void test2() {
    Predictor p = new Predictor();
    p.add(new OneView().pt("a","1"));
    p.add(new OneView().pt("a","0"));
    assertEquals(null,p.predict().get("a"));
    p.add(new OneView().pt("a","1"));
    assertEquals("0",p.predict().get("a"));
  }

  public void test3() {
    String task = "110011>0";
    plainSeqProc(task);
  }

  public void test4() {
    // this is a complex task - we must treat 'inside rpt group 101' as another sensor
    // to see difference in situation
    String task = "0 1012 11 1012 01 10>12 0 101>2 0 10>12 101>2 11111 10>12 11";
    plainSeqProc(task);
  }

  public void test5() {
    // this is a complex task - we must treat 'inside rpt group 101' as another sensor
    // to see difference in situation
    String task = "0 101 11 101 00 10>1 00 10>1 10>1 11111 10>1 11";
    plainSeqProc(task);
  }

  private void plainSeqProc(String task) {
    Predictor p = new Predictor();
    for( int i=0; i<task.length(); i++ ){
      char c = task.charAt(i);
      if( c==' ' ){
        continue;
      }
      if( c=='>' ){
        assertEquals(""+task.charAt(i+1), p.predict().get("a"));
      }else{
        p.add(new OneView().pt("a",""+c));
      }
    }
  }

}
