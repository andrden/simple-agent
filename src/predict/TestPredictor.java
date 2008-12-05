package predict;

import junit.framework.TestCase;
import mem.OneView;
import mem.Hist;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import predict.singletarget.SensorHist;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 18:15:26
 */
public class TestPredictor extends TestCase {

  public void testTimebased1() {
    LinearPredictor p = new LinearPredictor();
    p.add(new OneView().pt("a", "1"));
    p.add(new OneView().pt("a", "1"));
    assertEquals("1", p.predict().get("a"));
  }

  public void testTimebasedFastLearn3() {
    LinearPredictor p = new LinearPredictor();
    p.add(new OneView().pt("a", "1"));
    p.add(new OneView().pt("a", "0"));
    //assertEquals(null, p.predict().get("a"));
    p.add(new OneView().pt("a", "1"));
    assertEquals("0", p.predict().get("a"));
  }

  public void testTimebased3() {
    String task = "110011>0";
    plainSeqProc(task);
  }

  public void testTimebased4() {
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

  public void testTimebased5() {
    // this is a complex task - we must treat 'inside rpt group 101' as another sensor
    // to see difference in situation
    String task = "0 101 22 101 00 10>1 00 10>1 10>1 11111 10>1 11";
    plainSeqProc(task);
  }

  public void test6() {
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "A0");p.printRules("b");  //2
    addMulti(p, "B2");p.printRules("b");  //3
    addMulti(p, "A3");p.printRules("b");  //2
    addMulti(p, "B2");p.printRules("b");  //4
    addMulti(p, "C4");p.printRules("b");  //1
    addMulti(p, "A1");p.printRules("b");  //?
    assertEquals("2", p.predict().get("b"));  // a=A => b=2
//@data
//2,B,3
//0,A,2
//3,A,2
//4,C,1
//2,B,4

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
    addMulti(p, "zAn1");
    addMulti(p, "zAk1");
    addMulti(p, "xAp0");
    addMulti(p, "xAp1");
    addMulti(p, "zAk1");
    addMulti(p, "zBk0");
    addMulti(p, "xAk0");
    assertEquals("0", p.predict().get("d")); // c=k => d=0
  }

  public void test9() { // test for rule auto-extension
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "1Abz0");
    addMulti(p, "2Njq1");
    addMulti(p, "1Mjq0");
    addMulti(p, "2Abw0");
    addMulti(p, "1Nhq1");
    addMulti(p, "2Yhp0");
    addMulti(p, "1Nbp0");
    addMulti(p, "2Avk0");

    // J48 chooses 'c' as best describing attribute here, why???
    // DecisionStump is correct here!

    Object pred = p.predict().get("e");
    assertEquals("1", pred); // b=A => e=1 (don't require Ab)
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
    CmdPredictionTree tree = new PredictionTreeBuilder(p.getPredictor(), cmds, 2).build(p.getLast());
    assertNotNull( tree.findPositiveResultOrSmacks() );
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

  public void testTooManyRules1() throws Exception{
    LinearPredictor p = new LinearPredictor();
    Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            getClass().getResourceAsStream("data.xml"));
    String txt = d.getElementsByTagName("testTooManyRules1").item(0).getTextContent();
    for( String s : txt.split("\n") ){
      s = s.trim();
      if( s.length()==0 ){
        continue;
      }
      addMultiMap(p, s);
    }
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

  public void testRuleGeneralization() throws Exception{
    SensorHist sensor = new SensorHist("E");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.CMD_KEY));
    examplesForSensorHist(sensor, "testRuleGeneralization");
  }

  public void testRulesFor2Categories() throws Exception{
    SensorHist sensor = new SensorHist("$");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.CMD_KEY));
    examplesForSensorHist(sensor, "testRulesFor2Categories");
  }

  public void testOverDecisionStump() throws Exception{
    SensorHist sensor = new SensorHist("$");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testOverDecisionStump");
  }

  public void testSimple2atrr() throws Exception{
    SensorHist sensor = new SensorHist("$");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testSimple2atrr");
  }

  public void testPredictRepeated() throws Exception{
    SensorHist sensor = new SensorHist("fr");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testPredictRepeated");
  }

  public void testPredictRepeated2() throws Exception{
    SensorHist sensor = new SensorHist("fr");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testPredictRepeated2");
  }

  public void testPredictRepeated3() throws Exception{
    SensorHist sensor = new SensorHist("fr");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testPredictRepeated3");
  }

  private void examplesForSensorHist(SensorHist sensor, String xmlElem) throws Exception {
    //LinearPredictor p = new LinearPredictor();
    Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            getClass().getResourceAsStream("data.xml"));
    String txt = d.getElementsByTagName(xmlElem).item(0).getTextContent();
    for( String s : txt.split("\n") ){
      s = s.trim();
      if( s.length()==0 ){
        continue;
      }

      boolean quest=false;
      if( s.startsWith("?") ){
        quest=true;
        s = s.substring(1);
      }

      String key = "-";
      int posBr = s.indexOf("{");
      if( posBr>0 ){
        key = s.substring(0,posBr);
        s = s.substring(posBr);
      }
//      if( s.startsWith("+") || s.startsWith("0") ){
//        key = s.substring(0,1);
//        s = s.substring(1);
//      }
      //addMultiMap(p, s);
      OneView v = mkOneView(s);
      if( quest ){
        boolean acc = sensor.valAcceptedByRules(v, key);
        Object wekaKey = sensor.predictWithWeka(v);
        Object pred = sensor.predict(v);
        assertTrue( "key="+key+" wekaKey="+wekaKey+" pred="+pred, acc);
      }else{
        sensor.addAsCurrent(key, v);
      }
    }
  }

  void addMultiMapAll(LinearPredictor p, String... views) {
    for( String v : views ){
      addMultiMap(p, v);
    }
  }

  void addMultiMap(LinearPredictor p, String view) {
    OneView v = mkOneView(view);
    p.add(v);
  }

  private OneView mkOneView(String view) {
    OneView v = new OneView();
    String[] elems = view.split("[{}, ]+");
    for( String e : elems ){
      if( e.length()>0 ){
        String[] pair = e.split("=");
        v.pt(pair[0], pair[1]);
      }
    }
    return v;
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
    StringBuilder compleated = new StringBuilder();
    for (int i = 0; i < task.length(); i++) {
      char c = task.charAt(i);
      compleated.append(c);
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
