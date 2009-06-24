import junit.framework.TestSuite;
import predict.TestPredictor;
import worlds.TestWorlds;
import contin.tests.TestContin;
import reinforcement.TestSoftGreedy;

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
        suite.addTestSuite(TestContin.class);
        suite.addTestSuite(TestSoftGreedy.class);
        return suite;
    }
}
