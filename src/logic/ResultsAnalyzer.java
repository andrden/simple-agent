package logic;

import mem.Cause;
import mem.Hist;
import mem.DeepState;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 28/5/2008
 * Time: 19:39:56
 */
public abstract class ResultsAnalyzer {
  public abstract Cause test(List<Hist> found, DeepState groupingCond);
}
