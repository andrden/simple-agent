package contin;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: May 18, 2009
 * Time: 7:15:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Command {
  String command;
  double force;

  public Command(String command, double force) {
    if( force<-1 || force>1 ){
      throw new RuntimeException(command+" force="+force);
    }

    this.command = command;
    this.force = force;
  }

  public String getCommand() {
    return command;
  }

  public double getForce() {
    return force;
  }
}
