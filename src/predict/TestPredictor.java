package predict;

import junit.framework.TestCase;
import mem.OneView;
import mem.Hist;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.io.InputStreamReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import predict.singletarget.*;
import utils.Utils;

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

  public void testFaTimebasedFastLearn3() {
    LinearPredictor p = new LinearPredictor();
    p.add(new OneView().pt("a", "1"));
    p.add(new OneView().pt("a", "0"));
    //assertEquals(null, p.predict().get("a"));
    p.add(new OneView().pt("a", "1"));
    assertEquals("0", p.predict().get("a"));
  }

  public void testFaTimebased3() {
    String task = "110011>0";
    plainSeqProc(task);
  }

  public void testFaTimebased4() {
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

  public void testFaTimebased5() {
    // this is a complex task - we must treat 'inside rpt group 101' as another sensor
    // to see difference in situation
    String task = "0 101 22 101 00 10>1 00 10>1 10>1 11111 10>1 11";
    plainSeqProc(task);
  }

  public void test6Suggest() {
    HistSuggest sg = new HistSuggest();
    OneView v1 = addMulti(null, "A0", sg, "2");
    OneView v2 = addMulti(null, "B2", sg, "3");
    OneView v3 = addMulti(null, "A3", sg, "2");
    OneView v4 = addMulti(null, "B2", sg, "4");
    OneView v5 = addMulti(null, "C4", sg, "1");
    OneView v6 = addMulti(null, "A2", sg, "2");

    RuleCond r = sg.ruleByDecisionStump(Arrays.asList(v1,v2,v3,v4,v5,v6), null);
    assertEquals(r.toString(),"{a=A} neg {}");
  }

  public void test6b() {
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "A0");p.printRules("b");  //2
    addMulti(p, "B2");p.printRules("b");  //3
    addMulti(p, "A3");p.printRules("b");  //2
    addMulti(p, "B2");p.printRules("b");  //4
    addMulti(p, "C4");p.printRules("b");  //4
    addMulti(p, "A4");p.printRules("b");  //2
    addMulti(p, "C2");p.printRules("b");  //1
    addMulti(p, "A1");p.printRules("b");  //?
    assertEquals("2", p.predict().get("b"));  // a=A => b=2
//@data
//2,B,3
//0,A,2
//3,A,2
//4,C,1
//2,B,4

  }

  public void test6() {
//@todo - 1) don't add same cut (PRule) 2) add simpler if possible (b!=2 is really extra)    

//[0] = {predict.singletarget.PRule@652}"{} neg {} => {2=2, 3=1, 1=1, 4=1}"
//[1] = {predict.singletarget.PRule@653}"{a=A} neg {b=2} => {2=2}"
//[2] = {predict.singletarget.PRule@654}"{a=A} neg {b=2} => {2=2}"

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
    addMulti(p, "zAn1"); //1
    addMulti(p, "zAk1"); //0
    addMulti(p, "xAp0"); //1
    addMulti(p, "xAp1"); //1
    addMulti(p, "zAk1"); //0
    addMulti(p, "zBk0"); //0
    addMulti(p, "xAk0");
    assertEquals(null, p.predict().get("d")); // must be unbiased
    // c=k => d=0
    // conflict with xA => 1
    addMulti(p, "xBk0");
    assertEquals("0", p.predict().get("d"));
    addMulti(p, "xAv0");
    assertEquals("1", p.predict().get("d"));
  }

  public void test8disambig() { // disambiguation
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "zAn1"); //1
    addMulti(p, "zAk1"); //0
    addMulti(p, "yAp0"); //1
    addMulti(p, "xAp1"); //1
    addMulti(p, "zAk1"); //0
    addMulti(p, "zBk0"); //0
    addMulti(p, "xAk0");
    assertEquals(null, p.predict().get("d")); // must be unbiased
    // c=k => d=0 , no! z.k - z is in all k examples!
  }

  public void test8disambig2() { // disambiguation
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "zAn1"); //1
    addMulti(p, "zAk1"); //0
    addMulti(p, "yAp0"); //1
    addMulti(p, "xAp1"); //1
    addMulti(p, "mAk1"); //0
    addMulti(p, "zBk0"); //0
    addMulti(p, "xAk0");
    assertEquals("0", p.predict().get("d")); // must be unbiased
    // c=k => d=0 , no! z.k - z is in all k examples!
  }

  public void test8WrongWideOnAdd() { // disambiguation
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "zAn1q"); //1
    addMulti(p, "zAk1q"); //0
    addMulti(p, "yAp0q"); //1
    addMulti(p, "xAp1q"); //1
    addMulti(p, "vAk1q"); //0
    addMulti(p, "pBk0q"); //0
    addMulti(p, "xAk0q");
    assertEquals("0", p.predict().get("d"));
    // c=k,e=q => d=0
    addMulti(p, "xAk0s");
    assertEquals(null, p.predict().get("d")); // mustn't make too wide assumptions
  }

  public void testRefSame1() {
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "zAn1");
    addMulti(p, "zAk4"); // zAk, z.k
    addMulti(p, "xAp4");
    addMulti(p, "xAp1");
    addMulti(p, "zAk3"); // zAk, z.k
    addMulti(p, "zBk3"); //      z.k
    addMulti(p, "mCt3");
    addMulti(p, "mCt0");
    addMulti(p, "mCk5"); //      m.k --- ???
    assertEquals(null, p.predict().get("d")); // c=k => d=d(prev)
    // we really only had the opportunity to suggest "z.k => d=d(prev)",
    // not "m.k"
    // So our predictor is strict now.
    //
    // BUT! maybe there should also be method to suggest possible
    // (likely) predictions based on current rules - to choose
    // next steps as likely good or likely bringing new rules.
  }

  public void testRefSame1b() {
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "zAn1");
    addMulti(p, "zAk4"); // zAk, z.k
    addMulti(p, "xAp4");
    addMulti(p, "xAp1");
    addMulti(p, "yAk3"); // yAk, y.k
    addMulti(p, "zBk3"); //      z.k
    addMulti(p, "mCt3");
    addMulti(p, "mCt0");
    addMulti(p, "mCk5"); //      k --- ???
    assertEquals("5", p.predict().get("d")); // c=k => d=d(prev)
  }

  public void testRefSame2() {
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "zAn1");
    addMulti(p, "zAk4"); // zAk, z.k
    addMulti(p, "xAp4");
    addMulti(p, "xAp1");
    addMulti(p, "rAk3"); // rAk, r.k
    addMulti(p, "zBk3"); //      z.k
    addMulti(p, "mCt3");
    addMulti(p, "mCt0");
    addMulti(p, "mCk5"); //      m.k --- ???
    assertEquals("5", p.predict().get("d")); // c=k => d=d(prev)
  }

  public void testDoubtExtraWidening9() { // test for rule auto-extension
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "1Abz0"); //1
    addMulti(p, "2Njq1"); //0
    addMulti(p, "1Mjq0"); //0
    addMulti(p, "2Abw0"); //1
    addMulti(p, "1Nhq1"); //0
    addMulti(p, "2Yhp0"); //0
    addMulti(p, "1Nbp0"); //0
    addMulti(p, "2Avk0");

    // J48 chooses 'c' as best describing attribute here, why???
    // DecisionStump is correct here!

    Object pred = p.predict().get("e");
    assertEquals(null, pred); // b=A => e=1 (require Ab)
  }

  public void testDoubtExtraWidening9b() { // test for rule auto-extension
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "1Abz0"); //1
    addMulti(p, "2Njq1"); //0
    addMulti(p, "1Mjq0"); //0
    addMulti(p, "2Abw0"); //1
    addMulti(p, "1Nhq1"); //0
    addMulti(p, "2Yhp0"); //0
    addMulti(p, "1Nbp0"); //0
    addMulti(p, "2Abk0");

    // J48 chooses 'c' as best describing attribute here, why???
    // DecisionStump is correct here!

    Object pred = p.predict().get("e");
    assertEquals("1", pred); // b=A => e=1 (require Ab)
  }

  public void testLongTime() { // test for rule auto-extension
    LinearPredictor p = new LinearPredictor();
    addMulti(p, "1Abz0"); //1
    addMulti(p, "2Njq1"); //0
    for( int i=0; i<100; i++ ){
      addMulti(p, "1Mjq0"); //0
    }
    addMulti(p, "2Abw0"); //1
    addMulti(p, "1Nhq1"); //0
    addMulti(p, "2Yhp0"); //0
    addMulti(p, "1Nbp0"); //0
    addMulti(p, "2Abk0");

    Object pred = p.predict().get("e");
    assertEquals("1", pred); // b=A, c=b => e=1 (require Ab)
  }

  public void testFaFastLearn1(){
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

    assertEquals("0", p.predict().get("$")); // f=ORANGE, !=E is the cause
    // [1] = {predict.singletarget.PRule@717}"{fl=BLACK, $=0, fr=BLACK} neg {} => {0=2}"
  }
  public void test10b(){
    LinearPredictor p = new LinearPredictor();
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=WHITE, !=R, fl=GRAY, r=YELLOW, ff=BLACK, $=1, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");

    addMultiMap(p, "{f=WHITE, !=Fb, fl=WHITE, r=BLACK, ff=BLACK, $=1, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=BLACK, !=R, fl=BLACK, r=YELLOW, ff=BLACK, $=0, fr=BLACK, l=WHITE}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=BLACK, r=WHITE, ff=BLACK, $=0, fr=BLACK, l=BLACK}");

    assertEquals(null, p.predict().get("$")); // f=ORANGE, !=E is the cause

//    [1] = {predict.singletarget.PRule@672}"{f=ORANGE, !=E, ff=BLACK, $=0} neg {} => {1=2}"
//        vs
//    [2] = {predict.singletarget.PRule@673}"{fl=BLACK, $=0, fr=BLACK} neg {} => {0=2}"
//    maybe assess by probability?
  }
  public void test10c(){
    LinearPredictor p = new LinearPredictor();
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=WHITE, !=R, fl=GRAY, r=YELLOW, ff=BLACK, $=1, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");

    addMultiMap(p, "{f=WHITE, !=Fb, fl=WHITE, r=BLACK, ff=BLACK, $=1, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=BLACK, !=R, fl=BLACK, r=YELLOW, ff=BLACK, $=0, fr=BLACK, l=WHITE}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=WHITE, r=WHITE, ff=BLACK, $=0, fr=BLACK, l=BLACK}");

    assertEquals("1", p.predict().get("$")); // f=ORANGE, !=E is the cause
  }

  public void testTree1a(){
    LinearPredictor p = new LinearPredictor();
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=GRAY, r=YELLOW, ff=BLACK, $=0, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=WHITE, !=R, fl=GRAY, r=YELLOW, ff=BLACK, $=1, fr=WHITE, l=GRAY}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=ORANGE, !=E, fl=WHITE, r=BLACK, ff=BLACK, $=0, fr=YELLOW, l=WHITE}");

    addMultiMap(p, "{f=WHITE, !=Fb, fl=WHITE, r=BLACK, ff=BLACK, $=1, fr=YELLOW, l=WHITE}");
    addMultiMap(p, "{f=BLACK, !=R, fl=BLACK, r=YELLOW, ff=BLACK, $=0, fr=BLACK, l=WHITE}");
    addMultiMap(p, "{f=YELLOW, !=Ep, fl=BLACK, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}");
    addMultiMap(p, "{f=ORANGE, fl=WHITE, r=WHITE, ff=WHITE, $=0, fr=BLACK, l=BLACK}");

    // !=E is reasonable here
    List<String> cmds = Arrays.asList("L", "R", "N", "Fa", "Fb", "E", "Ep");
    CmdPredictionTree tree = new PredictionTreeBuilder(p.getPredictor(), cmds, 2).build(p.getLast());
    assertNull( tree.findPositiveResultOrSmacks() );
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
    addMultiMap(p, "{f=ORANGE, fl=WHITE, r=WHITE, ff=BLACK, $=0, fr=BLACK, l=BLACK}");

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
    addMultiMap(p, "{f=ORANGE, !=E, fl=WHITE, r=WHITE, ff=BLACK, $=1, fr=BLACK, l=BLACK}");

    assertEquals("0", p.predict().get("$")); // f=ORANGE, !=E is the cause
  }

  public void testTooManyRules1() throws Exception{
    buildPredictor("testTooManyRules1");
  }

  public void testFaFastLearn2(){
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

    public void testAreaStrip() throws Exception{
      SensorHist sensor = new SensorHist("E");
      sensor.setSkippedViewKeys(Collections.singleton(Hist.CMD_KEY));
      examplesForSensorHist(sensor, "testAreaStrip");
      Utils.breakPoint(); // y=='D'
    }
    public void testAreaIsle() throws Exception{
      SensorHist sensor = new SensorHist("E");
      sensor.setSkippedViewKeys(Collections.singleton(Hist.CMD_KEY));
      examplesForSensorHist(sensor, "testAreaIsle");
      Utils.breakPoint(); // y=='D' && x=='B'
    }
    public void testArea2Islands() throws Exception{
      SensorHist sensor = new SensorHist("E");
      sensor.setSkippedViewKeys(Collections.singleton(Hist.CMD_KEY));
      examplesForSensorHist(sensor, "testArea2Islands");
      Utils.breakPoint(); // y=='D' && x=='B' || x=='C' && y=='C'
    }
    public void testArea2Strips() throws Exception{
      SensorHist sensor = new SensorHist("E");
      sensor.setSkippedViewKeys(Collections.singleton(Hist.CMD_KEY));
      examplesForSensorHist(sensor, "testArea2Strips");
      Utils.breakPoint();
/*
            String res="0";
            if( y=='D' || y=='E' ){
                res="1";
            }else if( x=='C'){
                res="2";
            }

         */
    }

  public void testFaRulesFor2Categories() throws Exception{
    //@todo what is the right condition in this test?
    SensorHist sensor = new SensorHist("$");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.CMD_KEY));
    examplesForSensorHist(sensor, "testRulesFor2Categories");
  }

  public void testFaResultEqPrevNull() throws Exception{
    SensorHist sensor = new SensorHist("f");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.CMD_KEY));
    examplesForSensorHist(sensor, "testResultEqPrevNull");
  }

  public void testFaResultEqPrevNullTree() throws Exception{
    LinearPredictor p = buildPredictor("testResultEqPrevNullTree");
    CmdPredictionTree tree = new PredictionTreeBuilder(p.p,
        Arrays.asList("L", "R", "N", "Fb", "A1","A2F","A2B","B1","B2"), 4)
            .build(p.last);
    assertTrue( tree.branchOnCommand("N").noopDetected() );
  }

  private LinearPredictor buildPredictor(String tag) throws SAXException, IOException, ParserConfigurationException {
    LinearPredictor p = new LinearPredictor();
    Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            getClass().getResourceAsStream("data.xml"));
    String txt = d.getElementsByTagName(tag).item(0).getTextContent();
    for( String s : txt.split("\n") ){
      s = s.trim();
      if( s.length()==0 ){
        continue;
      }
      addMultiMap(p, s);
    }
    return p;
  }


  public void testReorderedOverDecisionStump() throws Exception{
    SensorHist sensor = new SensorHist("$");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testReorderedOverDecisionStump");
  }

  public void testFrScatteredRules() throws Exception{
    SensorHist sensor = new SensorHist("fr");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testFrScatteredRules");
  }

  public void testMoveRef() throws Exception{
    //need test for moves - references to prev row
    SensorHist sensor = new SensorHist("mmm");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testMoveRef");
    //usefulPrules empty here!
    Utils.breakPoint();
  }
  
  public void testMoveRef2() throws Exception{
    //need test for moves - references to prev row
    SensorHist sensor = new SensorHist("mmm");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testMoveRef2");
    //usefulPrules doesn't contain MOVE here!!!
    Utils.breakPoint();
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

  public void testFaRecencyPredictRepeated3() throws Exception{
    SensorHist sensor = new SensorHist("fr");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHist(sensor, "testRecencyPredictRepeated3");
  }

  public void testRecencySpread() throws Exception{
    SensorHist sensor = new SensorHist("f");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHistFile(sensor, "testRecencySpread.properties");
  }

//  public void testPessimRule() throws Exception{
//    SensorHist sensor = new SensorHist("$");
//    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
//    examplesForSensorHistFile(sensor, "testPessimRule.properties");
//    Utils.breakPoint();
//  }

  public void testNmisPred_fl() throws Exception{
    SensorHist sensor = new SensorHist("fl");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHistFile(sensor, "testNmisPred_fl.properties");
  }


  public void testNmisPred_rr() throws Exception{
    // N command after 120 steps must be able to predict rr=rr(prev)

    // step 300 - rule [86] = {predict.singletarget.PRule@1763}"{!=N, $=0, r=BLUE} neg {} => {WHITE=7, BLACK=5, YELLOW=2} eqPrev"
    // is observed so in fact this is kind of 'fast learn' 
    SensorHist sensor = new SensorHist("rr");
    sensor.setSkippedViewKeys(Collections.singleton(Hist.RES_KEY));
    examplesForSensorHistFile(sensor, "testNmisPred_rr.properties");
    //again it can't generalize conditions!
    //maybe too few examples are in 'recent' test list? - no, checked!
  }


  private void examplesForSensorHist(SensorHist sensor, String xmlElem) throws Exception {
    //LinearPredictor p = new LinearPredictor();
    Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            getClass().getResourceAsStream("data.xml"));
    String txt = d.getElementsByTagName(xmlElem).item(0).getTextContent();
    examplesForSensorHistStr(sensor, txt);
  }

  private void examplesForSensorHistFile(SensorHist sensor, String file) throws Exception {
    String txt = Utils.readAll(new InputStreamReader(getClass().getResourceAsStream(file), "utf-8"));
    examplesForSensorHistStr(sensor, txt);
  }


  private void examplesForSensorHistStr(SensorHist sensor, String txt) {
    OneView vprev=null;
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
      OneView v = mkOneView(s);
      v.prev=vprev;
      vprev=v;
//      if( "N".equals(v.get("!")) && !key.equals(v.get("fl")) ){
//        Utils.breakPoint();
//      }
      if( quest ){
        boolean acc = sensor.valAcceptedByRules(v, key);
        Object wekaKey = sensor.predictWithWeka(v);

//        OneViewToVal v2v = new OneViewToVal(){
//          public Object val(OneView v) {
//            return v.get("fr");
//          }
//        };


        PredictionResult pred1 = sensor.predictState(v, sensor.getViewToValStatic());
        Object pred = pred1.val(sensor.getSensorName());
        assertTrue( "key="+key+" wekaKey="+wekaKey+" pred="+pred, acc);
      }else{
        //sensor.predict(v); //......
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

  OneView addMulti(OneView prev, String view, HistSuggest analyzer, Object res) {
    OneView v = new OneView();
    for (int i = 0; i < view.length(); i++) {
      v.pt("" + (char) ('a' + i), "" + new Character(view.charAt(i)));
    }
    v.chain(prev);
    analyzer.addAsCurrent(res, v);
    return v;
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
