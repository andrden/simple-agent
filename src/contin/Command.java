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


    public String toString() {
        return command+" "+force;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Command command1 = (Command) o;

        if (Double.compare(command1.force, force) != 0) return false;
        if (command != null ? !command.equals(command1.command) : command1.command != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        long temp;
        result = (command != null ? command.hashCode() : 0);
        temp = force != +0.0d ? Double.doubleToLongBits(force) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
