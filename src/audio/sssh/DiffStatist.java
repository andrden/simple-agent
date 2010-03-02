package audio.sssh;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: Mar 2, 2010
 * Time: 10:03:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class DiffStatist {
    List<Double> all = new ArrayList();

    void add(double val){
        all.add(val);
    }

    void prepare(){
        Collections.sort(all);
    }

    double diff(double a, double b){
        int posa = insPos(a);
        int posb = insPos(b);
        return Math.abs(posa - posb)/(double)all.size();
    }

    private int insPos(double a) {
        int posa = Collections.binarySearch(all, a);
        if( posa<0 ){
            // (-(insertion point) - 1)
            posa = -(posa+1);
        }
        return posa;
    }

    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
    List<ScriptEngineFactory> factoryList = manager.getEngineFactories();
    for (ScriptEngineFactory factory : factoryList) {
      System.out.println(factory.getEngineName());
      System.out.println(factory.getLanguageName());
    }

        DiffStatist d = new DiffStatist();
        for( int i=0; i<100; i++ ){
            d.add(Math.sqrt(i));
        }
        d.prepare();
        System.out.println(d.diff(1,5)); // 0.24
    }
}
