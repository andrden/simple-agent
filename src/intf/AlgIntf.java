package intf;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 13/3/2008
 * Time: 18:04:27
 */
public interface AlgIntf {
  String nextCmd(String forcedCmd);


  Set<String> cmdGroups();
  boolean hasPlans();
  void setByCausesOnly(boolean byCausesOnly);
}
