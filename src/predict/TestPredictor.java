package predict;

import junit.framework.TestCase;
import mem.OneView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 18:15:26
 */
public class TestPredictor extends TestCase {

  public void test1() {
    LinearPredictor p = new LinearPredictor();
    p.add(new OneView().pt("a", "1"));
    p.add(new OneView().pt("a", "1"));
    assertEquals("1", p.predict().get("a"));
  }

  public void test2() {
    LinearPredictor p = new LinearPredictor();
    p.add(new OneView().pt("a", "1"));
    p.add(new OneView().pt("a", "0"));
    assertEquals(null, p.predict().get("a"));
    p.add(new OneView().pt("a", "1"));
    assertEquals("0", p.predict().get("a"));
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

//  public void test5a() {
//    // this is a complex task - we must treat 'inside rpt group 101' as another sensor
//    // to see difference in situation
//    String task = "0 101 11 101 00 10>1";
//
//    10 -> 1 is not a rule here!!!
//
//    plainSeqProc(task);
//  }

  public void test5() {
    // this is a complex task - we must treat 'inside rpt group 101' as another sensor
    // to see difference in situation
    String task = "0 101 22 101 00 10>1 00 10>1 10>1 11111 10>1 11";
    plainSeqProc(task);
  }

  public void test6() {
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "A0");
    addMulti(p, "B2");
    addMulti(p, "A3");
    addMulti(p, "B2");
    addMulti(p, "C4");
    addMulti(p, "A1");
    assertEquals("2", p.predict().get("b"));
  }

  public void test7() {
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "A0");

    addMulti(p, "B1");
    addMulti(p, "C0");

    addMulti(p, "B2");
    addMulti(p, "A0");

    addMulti(p, "B1");
    addMulti(p, "C5");

    addMulti(p, "B3");
    addMulti(p, "B1");
    assertEquals("C", p.predict().get("a"));
  }

  public void test8() {
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "An1");
    addMulti(p, "Ak1");
    addMulti(p, "Ap0");
    addMulti(p, "Ap1");
    addMulti(p, "Ak1");
    addMulti(p, "Bk0");
    addMulti(p, "Ak0");
    assertEquals("0", p.predict().get("c")); // b=k => c=0
  }

  public void test9() { // test for rule auto-extension
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "Abz0");
    addMulti(p, "Njq1");
    addMulti(p, "Mjq0");
    addMulti(p, "Abw0");
    addMulti(p, "Nhq1");
    addMulti(p, "Yhp0");
    addMulti(p, "Avk0");
    assertEquals("1", p.predict().get("d")); // A => d=1 (don't require Ab)
  }

  public void testFastLearn1(){
    LinearPredictor p = new LinearPredictor();
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=WHITE, !=R, fl=GRAY, r=YELLOW, ff=BLACK, $=1, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");
    assertEquals("1", p.predict().get("$")); // f=ORANGE, !=E is the cause
  }

  public void test10(){
    LinearPredictor p = new LinearPredictor();
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=WHITE, !=R, fl=GRAY, r=YELLOW, ff=BLACK, $=1, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");

    addMultiMap(p, "{f=WHITE, !=Fb, fl=WHITE, r=BLACK, ff=BLACK, $=1, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=BLACK, !=R, fl=BLACK, r=YELLOW, ff=BLACK, $=0, fr=BLACK, l=WHITE}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}");

    assertEquals("1", p.predict().get("$")); // f=ORANGE, !=E is the cause
  }

  public void testTree1(){
    LinearPredictor p = new LinearPredictor();
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=WHITE, !=R, fl=GRAY, r=YELLOW, ff=BLACK, $=1, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");

    addMultiMap(p, "{f=WHITE, !=Fb, fl=WHITE, r=BLACK, ff=BLACK, $=1, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=BLACK, !=R, fl=BLACK, r=YELLOW, ff=BLACK, $=0, fr=BLACK, l=WHITE}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}");
    addMultiMap(p, "{f=ORANGE, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}");

    // !=E is reasonable here
    List<String> cmds = Arrays.asList("L", "R", "N", "Fa", "Fb", "E", "Ep");
    CmdPredictionTree tree = new PredictionTreeBuilder(p.getPredictor(), cmds).build(p.getLast());
    System.currentTimeMillis();
    //assertEquals("1", p.predict().get("$")); // f=ORANGE, !=E is the cause
    assertTrue(false);
  }

  public void test11(){ // inverted test10() to test for ordering dependent bugs
    LinearPredictor p = new LinearPredictor();
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=GRAY, r=YELLOW, ff=BLACK, $=1, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=GRAY, r=YELLOW, ff=BLACK, $=1, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=WHITE, !=R, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=WHITE, r=BLACK, ff=BLACK, $=1, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=WHITE, r=BLACK, ff=BLACK, $=1, fr=YELLOW, l=WHITE}");

    addMultiMap(p, "{f=WHITE, !=Fb, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=BLACK, !=R, fl=BLACK, r=YELLOW, ff=BLACK, $=1, fr=BLACK, l=WHITE}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=BLACK, r=WHITE, ff=WHITE, $=1, fr=BLACK, l=BLACK}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=BLACK, r=WHITE, ff=WHITE, $=1, fr=BLACK, l=BLACK}");

    assertEquals("0", p.predict().get("$")); // f=ORANGE, !=E is the cause
  }


  public void testFastLearn2(){
    LinearPredictor p = new LinearPredictor();
    addMultiMapAll(p,
    "{f=YELLOW, !=Ep, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}",
    "{f=ORANGE, !=E, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}",
    "{f=WHITE, !=R, fl=GRAY, r=YELLOW, ff=BLACK, $=1, fr=WHITE, l=GRAY}",
    "{f=YELLOW, !=Ep, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}",
    "{f=ORANGE, !=E, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}",
    "{f=WHITE, !=Fb, fl=WHITE, r=BLACK, ff=BLACK, $=1, fr=YELLOW, l=WHITE}",
    "{f=BLACK, !=R, fl=BLACK, r=YELLOW, ff=BLACK, $=0, fr=BLACK, l=WHITE}",
    "{f=YELLOW, !=L, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}",
    "{f=BLACK, !=R, fl=BLACK, r=YELLOW, ff=BLACK, $=0, fr=BLACK, l=WHITE}",
    "{f=YELLOW, !=Ep, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}",
    "{f=ORANGE, !=E, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}"
    );
    //{f=WHITE, !=N, fl=BLACK, r=WHITE, ff=WHITE, $=1, fr=BLACK, l=BLACK}
    assertEquals("1", p.predict().get("$")); // f=ORANGE, !=E is the cause

  }

  void addMultiMapAll(LinearPredictor p, String... views) {
    for( String v : views ){
      addMultiMap(p, v);
    }
  }

  void addMultiMap(LinearPredictor p, String view) {
    OneView v = new OneView();
    String[] elems = view.split("[{}, ]+");
    for( String e : elems ){
      if( e.length()>0 ){
        String[] pair = e.split("=");
        v.pt(pair[0], pair[1]);
      }
    }
    p.add(v);
  }

  void addMulti(LinearPredictor p, String view) {
    OneView v = new OneView();
    for (int i = 0; i < view.length(); i++) {
      v.pt("" + (char) ('a' + i), "" + new Character(view.charAt(i)));
    }
    p.add(v);
  }

  private void plainSeqProc(String task) {
    LinearPredictor p = new LinearPredictor();
    StringBuilder hist = new StringBuilder();
    for (int i = 0; i < task.length(); i++) {
      char c = task.charAt(i);
      if (c == ' ') {
        continue;
      }
      if (c == '>') {
        assertEquals("hist="+hist,  "" + task.charAt(i + 1), p.predict().get("a"));
      } else {
        p.add(new OneView().pt("a", "" + c));
        hist.append(c);
      }
    }
  }

}
