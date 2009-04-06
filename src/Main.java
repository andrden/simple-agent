import intf.AlgIntf;
import intf.World;
import logic.Alg;
import com.pmstation.common.utils.MinMaxFinder;
import worlds.Line1;
import worlds.intf.WorldGridView;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 21/1/2008
 * Time: 16:29:14
 * To change this template use File | Settings | File Templates.
 */
public class Main extends JFrame {
  //WorldGridView world = new GridWorld1();
  //WorldGridView world = new GridWorld2();
  //WorldGridView world = new GridWorld3();
  WorldGridView world = new Line1();

  //WorldGridView world = new Rubic2x2World();
  //WorldGridView world = new ComparableSensors1();

  int totalResultPlus = 0;
  int totalResultMinus = 0;

  AlgIntf alg = new Alg((World) world);
  final Object algCmdSync = new Object();
  int step = 0;

  Set<String> algCmdGroups = new HashSet<String>();

  JTextArea logArea = new JTextArea();
  JScrollPane logAreaScrollPane;
  final static int SIZE = 25;

  JPanel cmdPanel = new JPanel();

  JToggleButton autoNext = new JToggleButton("Run>>");
  JLabel resultMark = new JLabel("|", SwingConstants.CENTER);
  JPanel gridPanel = new JPanel();

  static abstract class GridCanvas extends Canvas{
    GridCanvas(){
      setBackground(Color.RED); // to have update() called and flipping suppressed
    }

    public void setPrefSize(){
      setPreferredSize(new Dimension(getW() * SIZE + 1, getH() * SIZE + 1));
    }

    public void update(Graphics g) {
      //super.update(g);
      paint(g);
    }

    abstract int getW();
    abstract int getH();
    abstract Color getColorDisplay(int i, int j);
    abstract String getChar(int i, int j);
    
    @Override
    public void paint(Graphics g) {
      int x0 = 0;//getInsets().left;
      int y0 = 0;//getInsets().top;
      g.setColor(Color.RED);
      for (int i = 0; i < getW(); i++) {
        g.drawLine(x0 + SIZE * i, y0, x0 + SIZE * i, y0 + SIZE * getH());
      }
      for (int i = 0; i < getH(); i++) {
        g.drawLine(x0, y0 + SIZE * i, x0 + SIZE * getW(), y0 + SIZE * i);
      }
      for (int i = 0; i < getW(); i++) {
        for (int j = 0; j < getH(); j++) {
          Color paint = getColorDisplay(i, j);
          g.setColor(paint);
          int left = x0 + i * SIZE + 1;
          int top = y0 + j * SIZE + 1;
          g.fillRect(left, top, SIZE - 1, SIZE - 1);

          String ch = getChar(i, j);
          if (ch != null) {
            Color charC = new Color(255 - paint.getRed(), 255 - paint.getGreen(), 255 - paint.getBlue());
            if (charC.equals(paint)) {
              charC = Color.WHITE;
            }
            g.setColor(charC);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, SIZE * 2 / 3));
            g.drawString(ch, left + SIZE / 4, top + SIZE * 3 / 4);
          }
        }
      }
    }
  }

  GridCanvas gridCanvas = new GridCanvas() {
    int getW() {
      return world.getWidth();
    }

    int getH() {
      return world.getHeight();
    }

    Color getColorDisplay(int i, int j) {
      return world.getColorDisplay(i, j);
    }

    String getChar(int i, int j) {
      return world.getChar(i, j);
    }
  };

  GridCanvas crViewCanvas = new CrViewCanvas();

  class CrViewCanvas extends GridCanvas {
    Map<Point,String> sensors = new HashMap<Point,String>();
    MinMaxFinder xmm = new MinMaxFinder();
    MinMaxFinder ymm = new MinMaxFinder();

    CrViewCanvas(){
      Map<String, Point> sloc = world.sensorLocations();
      if( sloc==null ){
        return;
      }
      for( Point p : sloc.values() ){
        xmm.add(p.getX(), "");
        ymm.add(p.getY(), "");
      }
      for( String s : sloc.keySet() ){
        Point p = sloc.get(s);
        sensors.put(new Point( (int)ymm.getMaxVal()-(int)p.getY() , (int)xmm.getMaxVal()-(int)p.getX() ) , s);
      }
    }

    int getW() {
      return (int)(ymm.getMaxVal() - ymm.getMinVal() + 1);
    }

    int getH() {
      return (int)(xmm.getMaxVal() - xmm.getMinVal() + 1);
    }

    Color getColorDisplay(int i, int j) {
      if( sensors.containsKey(new Point(i,j)) ){
        String c = (String)world.view().get(sensors.get(new Point(i, j)));
        Color cl;
        try {
          cl = (Color)Color.class.getDeclaredField(c).get(null);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return cl;
      }
      return Color.BLACK;
    }

    String getChar(int i, int j) {
      if( sensors.containsKey(new Point(i,j)) ){
        return "*";
      }
      return null;
    }
  };

  public Main() throws HeadlessException {
    gridCanvas.setPrefSize();
    crViewCanvas.setPrefSize();

    setLayout(new FlowLayout());
    gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
    gridPanel.setLayout(new BorderLayout());
    gridPanel.add(gridCanvas);

    cmdPanel.setLayout(new GridLayout(2,0));
    for (final String cmd : world.commands()) {
      createCmdButton(cmd);
    }
    gridPanel.add(cmdPanel, BorderLayout.SOUTH);

    //JPanel viewPanel = new JPanel();

    //gridPanel.add(viewPanel, BorderLayout.NORTH);

    JPanel grid2Panel = new JPanel();
    grid2Panel.setLayout(new BorderLayout());
    grid2Panel.add(gridPanel);
    grid2Panel.add(crViewCanvas, BorderLayout.SOUTH);

    add(grid2Panel);

    JPanel nextPanel = createNextPanel();
    add(nextPanel);

    logAreaScrollPane = new JScrollPane(logArea);
    logAreaScrollPane.setPreferredSize(new Dimension(300, 300));
    add(logAreaScrollPane);

    logView("", 0);
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.pack();

    new Thread("cr6 ActionNext") {
      public void run() {
        while (true) {
          ssleep(700);
          if (autoNext.isSelected() || alg.hasPlans()) {
            //SwingUtilities.invokeLater(new Runnable(){
            //public void run() {
            execCmd(null);
            //}
            //});
          }
        }
      }
    }.start();
  }

  private JPanel createNextPanel() {
    JPanel nextPanel = new JPanel();
    nextPanel.setLayout(new GridLayout(0, 1));
    JButton next = new JButton("Next");
    next.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execCmd(null);
      }
    });
    nextPanel.add(next);
    nextPanel.add(autoNext);

    final JToggleButton byCauses = new JToggleButton("by Causes");
    byCauses.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        alg.setByCausesOnly(byCauses.isSelected());
      }
    });
    nextPanel.add(byCauses);

    resultMark.setBorder(
            BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5,5,5,5),        
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)));

    nextPanel.add(resultMark);

    JButton save = new JButton("Save*");
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveState();
      }
    });
    nextPanel.add(save);

    JButton load = new JButton("Load*");
    load.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadState();
      }
    });
    nextPanel.add(load);
    nextPanel.add(new JLabel(""));

    JButton prelevant = new JButton("PrnCurCause");
    prelevant.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        alg.printRelavantCauses();
      }
    });
    nextPanel.add(prelevant);

    JButton prnPredictions = new JButton("PrnPredictions");
    prnPredictions.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        alg.printCmdPredictions();
      }
    });
    nextPanel.add(prnPredictions);

    nextPanel.setBorder(BorderFactory.createEtchedBorder());
    return nextPanel;
  }

  File stateStorage() {
    return new File("/tmp/creature____State.ser");
  }

  void saveState() {
    try {
      stateStorage().getParentFile().mkdirs();
      ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(stateStorage()));
      o.writeObject(world);
      o.writeObject(alg);
      o.writeInt(totalResultPlus);
      o.writeInt(totalResultMinus);
      o.writeInt(step);
      o.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  void loadState() {
    try {
      ObjectInputStream i = new ObjectInputStream(new FileInputStream(stateStorage()));
      world = (WorldGridView) i.readObject();
      alg = (AlgIntf) i.readObject();
      totalResultPlus = i.readInt();
      totalResultMinus = i.readInt();
      step = i.readInt();
      i.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    refreshAllViews();
  }

  private void createCmdButton(final String cmd) {
    JButton b = new JButton(cmd /*"*"*/);
    cmdPanel.add(b);
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!alg.hasPlans()) {
          execCmd(cmd);
        }
      }
    });
  }

  private void execCmd(String cmd) {
    synchronized(algCmdSync) {
      long t0 = System.currentTimeMillis();
      cmd = alg.nextCmd(cmd);
      long dt0 = System.currentTimeMillis() - t0;
      step++;

      if (world.commandWrong(cmd)) {
        System.out.println("Main: cmd wrong " + cmd);
      }
      int result = world.command(cmd);
      logView(cmd, result);

      refreshAllViews();

      long t1 = System.currentTimeMillis();
      alg.cmdCompleted(result);
      long dt1 = System.currentTimeMillis() - t1;

      logArea.append(cmd + " n" + dt0 + " / r" + dt1 + "ms \n");
    }
  }

  private void refreshAllViews() {
    gridCanvas.repaint();
    crViewCanvas.repaint();
    String res = "Step #" + step + "   Result: " + totalResultPlus+"/"+totalResultMinus;
    if( world.availableResults()>0 ){
      res += " of " + world.availableResults();
       //+ " missed " + (world.availableResults() - totalResult)

    }
    setTitle(res);

    for (String g : alg.cmdGroups()) {
      if (algCmdGroups.add(g)) {
        createCmdButton(g);
        pack();
      }
    }
  }

  void logView(String cmd, int r) {
    if( r>=0 ){
      totalResultPlus += r;
    }else{
      totalResultMinus += r;
    }
    logArea.append(r + "  " + world.view() + "\n");
    logAreaScrollPane.getViewport().setViewPosition(new Point(0, logArea.getHeight()));

    resultMark.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    if (r == 0) {
      resultMark.setText(cmd);
    } else {
      resultMark.setText(cmd+ " res=" + r);
    }
    if (r < 0) {
      resultMark.setForeground(Color.RED);
    }
    if (r > 0) {
      resultMark.setForeground(Color.BLUE);
    }
    if (r == 0) {
      resultMark.setForeground(Color.BLACK);
    }
  }

  String view2str(Object viewElem) {
    if (viewElem == Color.WHITE) return "w";
    if (viewElem == Color.BLACK) return "b";
    if (viewElem == Color.YELLOW) return "y";
    if (viewElem == Color.GREEN) return "g";
    return "" + viewElem;
  }


  public static void main(String[] args) {
    new Main();
  }

  void ssleep(int msecs) {
    try {
      Thread.sleep(msecs);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
