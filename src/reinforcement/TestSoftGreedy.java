package reinforcement;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jun 24, 2009
 * Time: 10:39:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestSoftGreedy extends TestCase {
  public void test1(){
    SoftGreedy sg = new SoftGreedy(0.1);
    sg.put("a",100);
    sg.put("b",0);
    assertEquals(sg.policy().get("a"), 0.9);
  }
  public void test2(){
    SoftGreedy sg = new SoftGreedy(0.1);
    sg.put("a",1);
    sg.put("b",-10);
    //100 99-10=89
    assertEquals(sg.policy().get("b"), 0.01, 0.001);
  }
  public void test3(){
    SoftGreedy sg = new SoftGreedy(0.1);
    sg.put("a",4);
    sg.put("b",4);
    assertEquals(sg.policy().get("b"), 0.5);
  }

  public void test4(){
    SoftGreedy sg = new SoftGreedy(0.1);
    sg.put("a",4);
    sg.put("b",2);
    assertEquals(sg.policy().get("b"), 0.2);
  }

  public void test5(){
    SoftGreedy sg = new SoftGreedy(0.1);
    sg.put("a",4);
    sg.put("b",3.7);
    assertEquals(sg.policy().get("b"), 0.5);
  }

  public void test5aa(){
    SoftGreedy sg = new SoftGreedy(0.1);
    sg.put("a",4);
    sg.put("b",3.7);
    sg.put("c",0);
    assertEquals(sg.policy().get("c"), 0.05); // loss<0.1, so this is controversial
  }

  public void test5a(){
    SoftGreedy sg = new SoftGreedy(0.1);
    sg.put("a",4);
    sg.put("b1",2);
    sg.put("b2",2);
    sg.put("b3",2);
    sg.put("b4",2);
    sg.put("b5",2);
    assertEquals(sg.policy().get("b3"), 0.04);
  }

  public void test6(){
    SoftGreedy sg = new SoftGreedy(0.1);
    sg.put("a",-1);
    sg.put("b",-2);
    assertEquals(0.1, sg.policy().get("b"));
  }
}
