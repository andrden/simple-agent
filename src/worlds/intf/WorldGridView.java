package worlds.intf;

import intf.World;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/1/2008
 * Time: 13:04:16
 * To change this template use File | Settings | File Templates.
 */
public interface WorldGridView extends World, GridView{
  int availableResults();
  boolean commandWrong(String cmd);
}
