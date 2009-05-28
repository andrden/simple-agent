package intf;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

public interface World {
  List<String> commands();
  Collection<String> targetSensors();

  /**
   * View, part of which are targetSensors - results we are persuing
   * @return
   */
  Map<String, Object> view();


  /**
   * Returns result
   *
   * @param cmd
   * @return
   */
  void command(String cmd);
}
