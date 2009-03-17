package predict;

import mem.Hist;
import mem.OneView;

import java.util.*;

import predict.singletarget.PredictionResult;

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

    List<CmdPredictionTree> readyNodes = Arrays.asList(predictionTree);
    for (int i = 0; i < maxDepth && readyNodes.size() > 0; i++) {
      List<CmdPredictionTree> notesToExplore = new ArrayList<CmdPredictionTree>();
      for (CmdPredictionTree pti : readyNodes) {
        for (String c : allCommands) {
          expandPrediction(pti, c, notesToExplore);
        }

        if for all commands some kind of noop detected, mark this as loop,
        proceed to parent, maybe it's a loop too
      }
      readyNodes = notesToExplore;
    }
    return predictionTree;
  }

  private void expandPrediction(CmdPredictionTree pti, String c, List<CmdPredictionTree> notesToExplore) {
    OneView v = pti.start.cloneBranch();
    v.pt(Hist.CMD_KEY, c);

    OneView next = new OneView();
    next.prev = v;
    PredictionResult pr = predictor.predictNextState(v);
    next.mergeByAddNew(pr.view());

    //OneView next = predictor.predictNext(v);

    if (!next.isEmpty()) {
      //Map vall = next.getViewAll();
      CmdPredictionTree node = pti.addChild(c, next, pr.isWithRuleConflicts());
      if (!node.resultNotZero() && !node.noopDetected()) { // if result of this branch not yet known
        notesToExplore.add(node);
      }
    }
  }


}
