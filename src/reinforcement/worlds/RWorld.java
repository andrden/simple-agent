package reinforcement.worlds;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jun 24, 2009
 * Time: 5:13:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RWorld<T extends RState> {
  boolean isTerminal();

  T getS();

  double action(String a);

  double initStateValue(T s);

  List<String> actions();

  void println();

  void printStateMap(Map<T, String> m);

  JPanel visualizer();
}
