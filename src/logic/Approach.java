package logic;

import mem.Hist;
import mem.OneView;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/8/2008
 * Time: 18:00:40
 */
public interface Approach extends Serializable {
  CmdSet suggestCmd(Hist next, List<String> possibleCommands);

  /**
   *
   * @param next
   * @param cmd
   * @return true if cmd acceptable, false if should be avoided
   */
  boolean assessCmd(Hist next, String cmd);

  String predictionInfo(Hist curr);
  void appendValsToLastView(Map<String, Object> sensors);
  public void add(Hist next);
}
