package logic;

import mem.*;

import java.io.Serializable;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 12/6/2008
 * Time: 16:21:11
 */
public class CmdCompletionAnalyzer  implements Serializable {
  Alg alg;

  Causes2 causes2;
  Causes causes;
  History history;

  public CmdCompletionAnalyzer(Alg alg) {
    this.alg = alg;
    causes=alg.causes;
    causes2=alg.causes2;
    history=alg.history;
  }
  void log(String s){
    alg.log("@@@ compl: "+s);
  }


  void findNoop(Map<String, Object> view){
    Hist thisH = new Hist(history.last, view, null);
    Hist cmp = thisH.prev;
    for( int level=1; level<=4; level++){
      if( cmp==null ){
        return;
      }
      boolean noop = cmp.viewMatch( thisH, false );
      if(noop){
        ResultsAnalyzer a = new NoopResultsAnalyzer(history, causes, level);
        findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
        break; // no use looking for deeper noops
      }
      cmp = cmp.prev;
    }
  }

  void resultAnalyse2(int res, Map<String, Object> view) {
    Map<String,Object> prediction = causes.predictAllViewByCauses(history.last);

    PredictionTree2 predictionTree = alg.buildPredictionTree(history.last, view);
    PredictionTree2 predictionTreeOld=null;
    PredictionTree2.PositiveResultOrSmack currSmack = predictionTree.findPositiveResultOrSmacks();
    if(res!=0 || currSmack!=null){
      predictionTreeOld = alg.buildPredictionTree(history.last.prev, history.last.getViewOnly());
      if( predictionTreeOld.findPositiveResultOrSmacks()==null ){
        if( res!=0 ){
          Cause2 cau = causes2.find(Hist.RES_KEY, new Integer(res));
          if( cau==null ){
            cau = new Cause2(Hist.RES_KEY, new Integer(res));
            causes2.add(cau);
          }
          cau.addExample(history.getNextHist());
//          ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, null);
//          findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
        }else{
          System.out.println("tree now smacks, tree old not: "+currSmack.description);
          Map<String, Object> smack0 = predictionTree.getResultOrSmacksKeyView();
          if( !Utils.containsAll(prediction, smack0) ){
            // @todo State1 world: at every result +1 it looks from where f=RED appeared
            // and obviously finds no cause - it's random - how to avoid repeated search?
            // remember last steps to see they are the same? no! random 'r' and random 'f' colors
            // make that impossible. Statistically only?
            ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, smack0.keySet());
            findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
          }
        }
      }
//      }

  }

  }

  void resultAnalyse(int res, Map<String, Object> view) {
    resultAnalyse2(res, view);

    findNoop(view);

    Hist hnext = history.getNextHist();
    causes.verifyAll(hnext);

    Map<String,Object> prediction = causes.predictAllViewByCauses(history.last);
    if( prediction==null ){
      prediction = new HashMap<String,Object>();
    }
    //Map<String,Object> nextViewAll = history.last.next.getViewAll();
//    Map<String,Object> notPredicted = Utils.difference(nextViewAll, prediction);
//
//    if( notPredicted.size()>0 ){
//      System.getProperties();
//    }

    if( res>0 ){
      Hist h = history.last;
      List<String> lastCommands = new ArrayList<String>();
      for( int i=0; i<4; i++ ){
        if( h==null ){
          break;
        }
        lastCommands.add((String)h.getViewAll().get(Hist.CMD_KEY));
        h = h.prev;
      }
      if( lastCommands.size()==4 ){
        alg.lastSuccessfulCommands.add(lastCommands);
      }
    }

//    if( false && res!=0 ){
//      //TODO unite res!=0 and res==0 steps
//
//      // try to generate causes
//      // if not already explained by other causes
//      CauseMaxStruct cms = alg.causePredict(history.last);
//      if( cms.active.isEmpty() || cms.promisedMax()!=res ){
//        ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, null);
//        findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
//      }
//
//      // @todo if we have result - analyze not only last step, but also some steps before
//    }else{
      // The essence of this prediction tree is so that we could smell the smack
      // of expected result as long before it occurs as only possible.
      // If we can smack it now and weren't able one step before -
      // try to find new relations to be able to track result
      // earlier.

      PredictionTree predictionTree = alg.buildPredictionTreeOld(history.last, view);
      PredictionTree predictionTreeOld=null;
      PredictionTree.PositiveResultOrSmack currSmack = predictionTree.findPositiveResultOrSmacks();
      if(res!=0 || currSmack!=null){
        predictionTreeOld = alg.buildPredictionTreeOld(history.last.prev, history.last.getViewOnly());
        if( predictionTreeOld.findPositiveResultOrSmacks()==null ){
          if( res!=0 ){
            ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, null);
            findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
          }else{
            System.out.println("tree now smacks, tree old not: "+currSmack.description);
            Map<String, Object> smack0 = predictionTree.getResultOrSmacksKeyView();
            if( !Utils.containsAll(prediction, smack0) ){
              // @todo State1 world: at every result +1 it looks from where f=RED appeared
              // and obviously finds no cause - it's random - how to avoid repeated search?
              // remember last steps to see they are the same? no! random 'r' and random 'f' colors
              // make that impossible. Statistically only?
              ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, smack0.keySet());
              findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
            }
          }
        }
//      }

    }
  }

  /**
   * Explores direct results of situations similar to histVariant,
   * if results are consistent and interesting (useful), new cause is created
   * @param vdi
   * @param histVariant
   * @return
   */
  private void findRelations(ViewDepthIterator vdi, Hist histVariant, ResultsAnalyzer analyzer) {
    HistoryFinder hf = new HistoryFinder();
    int maxFound=0;
    Cause bestCause=null;

    //Map<ViewDepth,List<Hist>> basicSearches = new HashMap<ViewDepth,List<Hist>>();

    for(;;){
      ViewDepth vd = vdi.next();
      if( vd==null ){
        break;
      }
      if( !vd.canUse(history) ){
        continue;
      }
      List<DeepState> list = DeepState.lookBehind(vd, histVariant);
      if( list.isEmpty() ){
        continue;
      }
      DeepState ds = list.get(0);
      //List<Hist> found = history.find(ds);
//      if( vd.size()==1 ){
//        basicSearches.put(vd, found);
//      }
      List<Hist> found = hf.findNext(history, ds, vd);
      //log("vd="+vd);
//      if( found.size()!=foundFast.size() ){
//        throw new IllegalStateException();
//      }

      if(!found.isEmpty()){
        Cause newc = analyzer.test(found, ds);
        if( newc!=null && found.size()>maxFound ){
          maxFound = found.size();
          bestCause = newc;
        }
      }else{
        //svdi.addSkipPattern(vd); // nothing found, no longer try any similar
      }
    }

    if( bestCause!=null ){ // bestcause likely exists and will be accepted as it explains new data
      if( causes.newCause(bestCause) ){
        log("new cause "+bestCause);
        causes.generalize(history);
      }
    }

  }

}
