package predict;

import mem.Hist;
import mem.OneView;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 18/8/2008
 * Time: 18:56:58
 */
public class PredictionTreeBuilder {
  Predictor predictor;
  List<String> allCommands;
  int maxDepth;

  public PredictionTreeBuilder(Predictor predictor, List<String> allCommands, int maxDepth) {
    this.predictor = predictor;
    this.allCommands = allCommands;
    this.maxDepth = maxDepth;
  }

  public CmdPredictionTree build(OneView startPoint) {
    CmdPredictionTree predictionTree = new CmdPredictionTree(startPoint);

    List<CmdPredictionTree> readyNotes = Arrays.asList(predictionTree);
    for (int i = 0; i < maxDepth && readyNotes.size() > 0; i++) {
      List<CmdPredictionTree> notesToExplore = new ArrayList<CmdPredictionTree>();
      for (CmdPredictionTree pti : readyNotes) {
//        if (pti.noop) {
//          continue;
//        }
        for (String c : allCommands) {
          expandPrediction(pti, c, notesToExplore);

//          Map<String, Object> viewCmd = new HashMap<String, Object>(view);
//          viewCmd.put(Hist.CMD_KEY, c);
//          for (Cause2 cc : causes2.list) {
//            if (cc.isPositiveResult()) {
//              Hist h = cc.unexplainedExamplesIntersect(viewCmd);
//              if (h != null) {
//                String cmd = h.getCommand();
//                CmdPredictionTree child = pti.onCommand.get(cmd);
//                if (child == null) {
//                  Hist hn = new Hist(pti.histOld, pti.viewNext, cmd);
//                  child = pti.addChild(hn, cmd, null);
//                }
//                child.smacksEvent = h;
//                if (child.viewNext == null) {
//                  child.viewNext = new HashMap<String, Object>();
//                }
//                child.viewNext.put(cc.key, cc.val);
//              }
//            }
//          }
        }

//        for (String c : allCommands()) {
//          Hist h = new Hist(pti.histOld, pti.viewNext, c);
//          if (causes.predictsNoop(h)) {
//            PredictionTree2 child = pti.onCommand.get(c);
//            if (child == null) {
//              child = pti.addChild(h, c, null);
//            }
//            child.noop = true;
//          }
//        }


      }
      readyNotes = notesToExplore;
    }
    return predictionTree;
  }

  private void expandPrediction(CmdPredictionTree pti, String c, List<CmdPredictionTree> notesToExplore) {
    OneView v = pti.start.cloneBranch();
    v.pt(Hist.CMD_KEY, c);
    OneView next = predictor.predictNext(v);

    if (!next.isEmpty()) {
      Map vall = next.getViewAll();
      CmdPredictionTree node = pti.addChild(c, next);
      boolean val = vall.get(Hist.RES_KEY) != null && resultNotZero(vall.get(Hist.RES_KEY));
      if (!val) { // if result of this branch not yet known
        notesToExplore.add(node);
      }
    }
  }

  boolean resultNotZero(Object res){
    return  !"0".equals(""+res);
  }

}
