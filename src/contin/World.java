package contin;

import java.util.List;
import java.util.Map;

public interface World {
  List<String> commands();

  /**
   * @param cmd
   * @param force -1..+1
   */
  void command(String cmd, double force);

  Map<String, Object> view();
  Map<String, Double> senses();
}
