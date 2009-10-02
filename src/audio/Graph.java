package audio;

import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 25, 2009
 * Time: 6:20:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class Graph {
  JFrame frame;
  //volatile boolean clicked=false;
  int[] h = new int[1024];
  int MAX_H=500;

  class GridCanvas extends Canvas {
    GridCanvas(){
      setBackground(Color.WHITE); // to have update() called and flipping suppressed
      setPreferredSize(new Dimension(h.length,MAX_H));
    }

    @Override
    public void paint(Graphics g) {
      g.setColor(Color.GREEN.darker());
      for( int i=0; i<h.length; i++ ){
        g.drawLine(i,MAX_H,i,MAX_H-h[i]);
      }
    }
  }

  void mkFrame(){
    frame = new JFrame("Graph");
    frame.setLayout(new BorderLayout());
    frame.add(new GridCanvas());
//    JButton b = new JButton("Ok");
//    b.addActionListener(new ActionListener(){
//      public void actionPerformed(ActionEvent e) {
//        clicked=true;
//      }
//    });
//    frame.add(b, BorderLayout.SOUTH);
    frame.pack();
    frame.setVisible(true);
  }

  double max(double[] arr){
    double m = arr[0];
    for( double d : arr ){
      m = Math.max(d,m);
    }
    return m;
  }

  double min(double[] arr){
    double m = arr[0];
    for( double d : arr ){
      m = Math.min(d,m);
    }
    return m;
  }

  double val(double[] arr, int idx){
    if( arr.length==h.length*2 ){
      return (arr[idx*2]+arr[idx*+2+1])/2;
    }
    if( arr.length==h.length+1 ){
      return arr[idx];
    }
    if( arr.length==h.length/4 ){
      return arr[idx/4];
    }
    throw new UnsupportedOperationException("arr.len="+arr.length);
  }


  void show(double[] arr){
    double min = min(arr);
    double max = max(arr);
    for( int i=0; i<h.length; i++ ){
      h[i] = (int) ((val(arr, i)-min)/(max-min)*MAX_H);
    }

    //clicked=false;
    if( frame==null ){
      mkFrame();
    }
    while(frame.isVisible()){
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
    }
  }

  public static void main(String[] args){
//    Graph g = new Graph();
//    g.show();
    System.exit(0);
  }
}
