package intf;

import mem.Hist;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 13/3/2008
 * Time: 18:04:27
 */
public interface AlgIntf {
  String nextCmd(String forcedCmd);

  /**
   * Container notification - triggers result analysis
   * @param result
   */
  void cmdCompleted(int result);
  //Hist prediction(String command)

  Set<String> cmdGroups();
  boolean hasPlans();
  void printRelavantCauses();
  void setByCausesOnly(boolean byCausesOnly);
}
