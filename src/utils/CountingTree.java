package utils;

import java.util.*;
import java.io.Serializable;

/**
 * Used for counting most frequent command sequences
 * before positive result.
 * Tree root is result event, deeper into branches -
 * commands before, more distant from result event.
 */
public class CountingTree<T> implements Serializable {
  static class Node<T> implements Serializable{
    long count;
    Map<T,Node> children = new HashMap<T,Node>();

    Node<T> add(T el){
      Node n = children.get(el);
      if( n==null ){
        n = new Node();
        children.put(el, n);
      }
      n.count++;
      return n;
    }


    public String toString() {
      return "c="+count+" ch="+children.size();
    }
  }

  Node<T> root = new Node<T>();

  public void add(List<T> chain){
    Node<T> addTo = root;
    for( T el : chain ){
      addTo = addTo.add(el);
    }
  }

  public static void main(String[] args){
    CountingTree<String> t = new CountingTree<String>();
    t.add(Arrays.asList("a","b","c"));
    t.add(Arrays.asList("a","m","n"));
    t.add(Arrays.asList("a","b","d"));
    t.toString();
  }
}
