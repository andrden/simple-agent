package intf;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 21/1/2008
 * Time: 16:28:37
 * To change this template use File | Settings | File Templates.
 */
public interface World {
  List<String> commands();
  Map<String,Object> view();


  /**
   * Returns result
   * @param cmd
   * @return
   */
  int command(String cmd);
}
