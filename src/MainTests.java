import junit.framework.TestSuite;
import predict.TestPredictor;
import worlds.TestWorlds;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: May 28, 2009
 * Time: 6:24:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainTests extends TestSuite {
public static TestSuite suite()
    {
        TestSuite suite = new TestSuite();       
        suite.addTestSuite(TestPredictor.class);
        suite.addTestSuite(TestWorlds.class);
        return suite;
    }
}
