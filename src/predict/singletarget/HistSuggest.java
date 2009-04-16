package predict.singletarget;

import weka.classifiers.Classifier;
import weka.classifiers.trees.DecisionStump;
import weka.core.Attribute;
import weka.core.Instances;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.LinkedHashMap;

import mem.OneView;
import com.pmstation.common.utils.PrivateFieldGetter;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Mar 30, 2009
 * Time: 5:28:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistSuggest implements java.io.Serializable{
  Set<Object> vals = new HashSet<Object>();
  Set<String> skippedViewKeys;
  Classifier lastUsedClassifier;
  Set<String> decisiveAttrs = new HashSet<String>();
  LinkedHashMap<OneView, Object> exampleVals = new LinkedHashMap<OneView, Object>();

  public void setSkippedViewKeys(Set<String> skippedViewKeys) {
    this.skippedViewKeys = skippedViewKeys;
  }

  /**
   * v was the current view, and then val was obtained, so v caused val, v ==> val
   * @param val
   * @param v
   */
  public void addAsCurrent(Object val, OneView v) {
    vals.add(val);
    exampleVals.put(v, val);
  }

  public RuleCond ruleByDecisionStump(Collection<OneView> views, OneViewToVal backRef){
    DecisionStump myClassif  = new DecisionStump();
    WekaBuilder wf = buildClassifier(myClassif, views, backRef);

    Attribute splitAttr = wf.getInstances().attribute((Integer) PrivateFieldGetter.evalNoEx(myClassif,"m_AttIndex"));
    String attName = splitAttr.name();
    decisiveAttrs.add(attName);
    Object attVal = wf.attVal(attName, ((Double)PrivateFieldGetter.evalNoEx(myClassif,"m_SplitPoint")).intValue() );
    RuleCond r = new RuleCond(attName, attVal, true);
    return r;
  }

  public WekaBuilder buildClassifier(Classifier myClassif, Collection<OneView> views,
                                     OneViewToVal backRef){
    WekaBuilder wf = new WekaBuilder(myClassif);
    for( OneView v : views ){
      wf.collectAttrs(v, skippedViewKeys);
    }

    if( backRef!=null ){
      wf.addForRes("0");
      wf.addForRes("1");
    }else{
      for( Object o : vals ){
        wf.addForRes(o);
      }
    }
    wf.mkInstances();


    for( OneView v : views ){
      String vval = exampleVals.get(v).toString();
      if( backRef!=null ){
        vval = "0";
        if( v.prev!=null /*&& exampleVals.get(v.prev)!=null*/){
          Object backRefVal = backRef.val(v);
          //if( exampleVals.get(v.prev).equals(exampleVals.get(v)) ){
          if( backRefVal.equals(exampleVals.get(v)) ){
            vval = "1";
          }
        }
      }
      wf.addInstance(v, vval);
    }

    Instances ins = wf.getInstances();
    if( ins.numInstances()<1 ){
      return null;
    }


    try {
      wf.getClassifier().buildClassifier(ins);
    } catch (Exception e) {
      throw new RuntimeException("",e);
    }
    return wf;
  }

}
