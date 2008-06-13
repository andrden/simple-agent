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

  Causes causes;
  History history;

  public CmdCompletionAnalyzer(Alg alg) {
    this.alg = alg;
    causes=alg.causes;
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

  void resultAnalyse(int res, Map<String, Object> view) {
    findNoop(view);

    Map<String,Object> nextViewAll = history.getNextViewAll(history.last);
    Hist hnext = new Hist(history.last, nextViewAll, null);
    causes.verifyAll(hnext);

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

    if( res!=0 ){
      // try to generate causes
      // if not already explained by other causes
      CauseMaxStruct cms = alg.causePredict(history.last);
      if( cms.active.isEmpty() || cms.promisedMax()!=res ){
        ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, null);
        findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
      }

      // @todo if we have result - analyze not only last step, but also some steps before
    }else{
      // The essence of this prediction tree is so that we could smell the smack
      // of expected result as long before it occurs as only possible.
      // If we can smack it now and weren't able one step before -
      // try to find new relations to be able to track result
      // earlier.

      PredictionTree predictionTree = alg.buildPredictionTree(history.last, view);
      PredictionTree predictionTreeOld=null;
      PredictionTree.PositiveResultOrSmack currSmack = predictionTree.findPositiveResultOrSmacks();
      if(currSmack!=null){
        predictionTreeOld = alg.buildPredictionTree(history.last.prev, history.last.getViewOnly());
        if( predictionTreeOld.findPositiveResultOrSmacks()==null ){
          System.out.println("tree now smacks, tree old not: "+currSmack.description);
          Map<String,Object> prediction = causes.predictAllViewByCauses(history.last);
          if( prediction==null ){
            prediction = new HashMap<String,Object>();
          }
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

//      Causes.SmacksOfResult smacks = causes.smacksOfResult(new Hist(history.last, view, null));
//      if( smacks !=null ){
//        Causes.SmacksOfResult smacksPrev = causes.smacksOfResult(history.last);
//        if( smacksPrev==null || !smacks.ds.equalsDS(smacksPrev.ds) ){
//          // there has just appeared new smack of result:
//          Map<String,Object> prediction = causes.predictAllViewByCauses(history.last);
//          if( prediction==null ){
//            prediction = new HashMap<String,Object>();
//          }
//          Map<String, Object> smack0 = smacks.ds.getElemsAtDepth(0);
//          if( !Utils.containsAll(prediction, smack0) ){
//            // @todo State1 world: at every result +1 it looks from where f=RED appeared
//            // and obviously finds no cause - it's random - how to avoid repeated search?
//            // remember last steps to see they are the same? no! random 'r' and random 'f' colors
//            // make that impossible. Statistically only?
//            ResultsAnalyzer a = new MainResultsAnalyzer(history, causes, smack0.keySet());
//            findRelations(new ViewDepthGenerator(view.keySet()), history.last, a);
//          }
//        }
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
