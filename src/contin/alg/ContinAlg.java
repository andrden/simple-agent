package contin.alg;

import contin.Command;

import java.util.List;
import java.util.Map;

import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: May 18, 2009
 * Time: 7:11:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContinAlg {
  List<String> commands;

  public ContinAlg(List<String> commands) {
    this.commands = commands;
  }

  public Command suggestCmd(){
    return new Command(Utils.rnd(commands), 1);
  }

  public void cmdExecuted(Command cmd, Map<String, Object> newView,
      Map<String, Double> newSenses){

  }


}
