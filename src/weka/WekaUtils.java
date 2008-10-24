package weka;

import weka.classifiers.trees.J48;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.FastVector;
import weka.core.Attribute;
import weka.core.Instance;

import java.util.Random;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 8 жовт 2008
 * Time: 18:00:04
 */
public class WekaUtils {
  void cmd() throws Exception{
    //Instances ins = new Instances(new FileReader("C:\\prg\\Weka-3-5\\data\\weather.arff"));

    long t0=System.currentTimeMillis();
    for( int i=0; i<1000; i++ ){ // 2.7 millisecond 
      classif();
    }
    System.out.println( "dt=" + (System.currentTimeMillis() - t0) );

  }

  public Instances makeInstances(LinkedHashMap<String, LinkedHashSet<Object>> attrsMap){
    FastVector attrs = new FastVector();
    for( String aname : attrsMap.keySet() ){
      LinkedHashSet<Object> vs = attrsMap.get(aname);
      FastVector my_nominal_values = new FastVector(vs.size());
      for( Object s : vs ){
        my_nominal_values.addElement(s.toString());
      }
      Attribute att = new Attribute(aname, my_nominal_values);
      attrs.addElement(att);
    }
    Instances ins = new Instances("test", attrs, 100);
    ins.setClassIndex(ins.numAttributes() - 1);
    return ins;
  }

  private void classif() throws Exception {
    FastVector attrs = new FastVector();
    Attribute atA = bldAttr("A", "a", "b", "c");
    attrs.addElement(atA);
    Attribute atA1 = bldAttr("A1", "a", "b", "c");
    attrs.addElement(atA1);
    Attribute atB = bldAttr("B", "m", "n", "k");
    attrs.addElement(atB);
    Attribute atR = bldAttr("R", "0", "1");
    attrs.addElement(atR);
    Instances ins = new Instances("test", attrs, 5);
    ins.setClassIndex(ins.numAttributes() - 1);


    Random rnd = new Random();
    for( int i=0; i<100; i++ ){
      Instance in = new Instance(4);
      int r1 = rnd.nextInt(2);
      int r2 = rnd.nextInt(2);
      in.setValue(atA,  ""+("ab".charAt(r1)));
      in.setValue(atA1,  ""+("ab".charAt(r2)));
      in.setValue(atB,  ""+("mnk".charAt(rnd.nextInt(3))));
      in.setValue(atR, r1==r2 ? "1" : "0" );
      ins.add(in);
    }

    Instance in = new Instance(3);
    in.setValue(atA,  "b");
    in.setValue(atA1,  "a");
    in.setValue(atB,  ""+("mnk".charAt(rnd.nextInt(3))));
    //in.setValue(atR, r1==r2 ? "1" : "0" );
    in.setDataset(ins);

    Classifier classif = new J48();
    //Classifier classif = new NaiveBayes();
    //Classifier classif = new NaiveBayesSimple();
    //Classifier classif = new NaiveBayesMultinomial();
    classif.buildClassifier(ins);
    double[] d = classif.distributionForInstance(in);
    d=d;
    //classif.buildClassifier();
  }

  Attribute bldAttr(String name, String ... vals ){
    FastVector my_nominal_values = new FastVector(vals.length);
    for( String s : vals ){
      my_nominal_values.addElement(s);
    }
    return new Attribute(name, my_nominal_values);
  }

  public static void main(String[] args) throws Exception{
    new WekaUtils().cmd();
  }
}
