package reinforcement;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jun 24, 2009
 * Time: 5:13:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RWorld {
  boolean isTerminal();

  String getS();

  double action(String a);

  List<String> actions();

  void println();
}
