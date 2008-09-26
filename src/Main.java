import intf.AlgIntf;
import intf.World;
import logic.Alg;
import worlds.GridWorld1;
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

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 21/1/2008
 * Time: 16:29:14
 * To change this template use File | Settings | File Templates.
 */
public class Main extends JFrame {
  //WorldGridView grid = new Feed0();
  //WorldGridView grid = new SeqReact0();
  //WorldGridView grid = new SeqReact3minus();
  //WorldGridView grid = new SeqReact3();
  //WorldGridView world = new State1();
  WorldGridView world = new GridWorld1();
  int totalResult = 0;
  AlgIntf alg = new Alg((World) world);
  final Object algCmdSync = new Object();
  int step = 0;

  Set<String> algCmdGroups = new HashSet<String>();

  JTextArea logArea = new JTextArea();
  JScrollPane logAreaScrollPane;
  final int SIZE = 30;

  JPanel cmdPanel = new JPanel();

  JToggleButton autoNext = new JToggleButton("Run>>");
  JLabel resultMark = new JLabel();
  JPanel gridPanel = new JPanel();
  Canvas gridCanvas = new Canvas() {
    public void update(Graphics g) {
      //super.update(g);
      paint(g);
    }

    @Override
    public void paint(Graphics g) {
      int x0 = 0;//getInsets().left;
      int y0 = 0;//getInsets().top;
      g.setColor(Color.RED);
      for (int i = 0; i < world.getWidth(); i++) {
        g.drawLine(x0 + SIZE * i, y0, x0 + SIZE * i, y0 + SIZE * world.getHeight());
      }
      for (int i = 0; i < world.getHeight(); i++) {
        g.drawLine(x0, y0 + SIZE * i, x0 + SIZE * world.getWidth(), y0 + SIZE * i);
      }
      for (int i = 0; i < world.getWidth(); i++) {
        for (int j = 0; j < world.getHeight(); j++) {
          Color paint = world.getColorDisplay(i, j);
          g.setColor(paint);
          int left = x0 + i * SIZE + 1;
          int top = y0 + j * SIZE + 1;
          g.fillRect(left, top, SIZE - 1, SIZE - 1);

          String ch = world.getChar(i, j);
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
  };

  public Main() throws HeadlessException {
    setLayout(new FlowLayout());
    gridCanvas.setPreferredSize(new Dimension(world.getWidth() * SIZE + 1, world.getHeight() * SIZE + 1));
    gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
    gridPanel.setLayout(new BorderLayout());
    gridPanel.add(gridCanvas);
    gridCanvas.setBackground(Color.RED); // to have update() called and flipping suppressed

    for (final String cmd : world.commands()) {
      createCmdButton(cmd);
    }
    gridPanel.add(cmdPanel, BorderLayout.SOUTH);

    //JPanel viewPanel = new JPanel();

    //gridPanel.add(viewPanel, BorderLayout.NORTH);

    add(gridPanel);

    JPanel nextPanel = createNextPanel();
    add(nextPanel);

    logAreaScrollPane = new JScrollPane(logArea);
    logAreaScrollPane.setPreferredSize(new Dimension(300, 300));
    add(logAreaScrollPane);

    logView(0);
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

    nextPanel.setBorder(BorderFactory.createEtchedBorder());
    return nextPanel;
  }

  File stateStorage() {
    return new File("/tmp/creature____State.ser");
  }

  void saveState() {
    try {
      stateStorage().mkdirs();
      ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(stateStorage()));
      o.writeObject(world);
      o.writeObject(alg);
      o.writeInt(totalResult);
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
      totalResult = i.readInt();
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
      logView(result);

      refreshAllViews();

      long t1 = System.currentTimeMillis();
      alg.cmdCompleted(result);
      long dt1 = System.currentTimeMillis() - t1;

      logArea.append(cmd + " n" + dt0 + " / r" + dt1 + "ms \n");
    }
  }

  private void refreshAllViews() {
    gridCanvas.repaint();
    String res = "Step #" + step + "   Result: " + totalResult + " of " + world.availableResults()
            + " missed " + (world.availableResults() - totalResult);
    setTitle(res);

    for (String g : alg.cmdGroups()) {
      if (algCmdGroups.add(g)) {
        createCmdButton(g);
        pack();
      }
    }
  }

  void logView(int r) {
    totalResult += r;
    logArea.append(r + "  " + world.view() + "\n");
    logAreaScrollPane.getViewport().setViewPosition(new Point(0, logArea.getHeight()));

    resultMark.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    if (r == 0) {
      resultMark.setText("");
    } else {
      resultMark.setText("Result: " + r);
    }
    if (r < 0) {
      resultMark.setForeground(Color.RED);
    }
    if (r > 0) {
      resultMark.setForeground(Color.BLUE);
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
