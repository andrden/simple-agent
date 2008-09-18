package logic;

import mem.Hist;
import mem.OneView;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/8/2008
 * Time: 18:00:40
 */
public interface Approach {
  CmdSet suggestCmd(Hist next, List<String> possibleCommands);
  String predictionInfo(Hist curr);
  void appendValsToLastView(Map<String, Object> sensors);
  public void add(Hist next);
}
