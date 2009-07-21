package reinforcement.worlds;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jul 20, 2009
 * Time: 11:43:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Visualizer extends JFrame {
  JPanel prev;
  public void setWorld(RWorld w){
    if( prev!=null ){
      remove(prev);
    }
    prev = w.visualizer();
    add(prev);
    pack();
    setVisible(true);
  }
}
