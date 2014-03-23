package org.sdif4j.testing.cdi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sdif4j.Injector;
import org.sdif4j.cdi.CdiInjector;
import org.sdif4j.testing.ITestSingleton;
import org.sdif4j.testing.TestLazySingleton;
import org.sdif4j.testing.TestPrototype;
import org.sdif4j.testing.TestSingleton;
import org.testng.Assert;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.assertEquals;


/**
 * Simple CDI tests for {@link org.sdif4j.cdi.CdiInjector} inside {@code Arquillian} container.
 *
 * @author Pavel Shackih
 */
@RunWith(Arquillian.class)
public class ArquillianTest {

    static final String FOO_BEAN = "fooBean";

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                // we need to build our own jar for container
                .addPackage("org.sdif4j.cdi")
                .addClass(Injector.class)
                .addPackage("org.sdif4j.testing")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    Injector injector;

    @Test
    public void testInjectorInstance() {
        final Injector injector1 = this.injector;
        Assert.assertTrue(injector1 instanceof CdiInjector);
        Assert.assertTrue(this.injector.getInstance(Injector.class) == injector1);
    }

    @Test
    public void testNamedService() {
        IService service = injector.getInstance(IService.class, FOO_BEAN);
        Assert.assertNotNull(service);
        Assert.assertTrue("foo".equals(service.foo()));
    }

    @Test
    public void testNamedString() {
        assertEquals(injector.getInstance(String.class, "key"), "value");
    }

    @Test
    public void testSingleton() {
        /**
         * Todo: Why 1 is expected? This singleton can be created lazy without @Lazy annotation. CDI doc doesn't cover it.
         */
        //assertEquals(TestSingleton.getInstantCount(), 1);
        final ITestSingleton iTestSingleton1 = injector.getInstance(ITestSingleton.class);
        final ITestSingleton iTestSingleton2 = injector.getInstance(ITestSingleton.class);
        final TestSingleton testSingleton1 = injector.getInstance(TestSingleton.class);
        final TestSingleton testSingleton2 = injector.getInstance(TestSingleton.class);
        assertEquals(TestSingleton.getInstantCount(), 1);

        Assert.assertNotNull(iTestSingleton1);
        Assert.assertTrue(iTestSingleton1 == iTestSingleton2);
        Assert.assertTrue(iTestSingleton2 == testSingleton1);
        Assert.assertTrue(testSingleton1 == testSingleton2);
    }

    @Test
    public void testLazySingleton() {
        assertEquals(TestLazySingleton.getInstantCount(), 0);
        final TestLazySingleton s1 = injector.getInstance(TestLazySingleton.class);
        final TestLazySingleton s2 = injector.getInstance(TestLazySingleton.class);
        assertEquals(TestLazySingleton.getInstantCount(), 1);
        Assert.assertNotNull(s1);
        Assert.assertTrue(s1 == s2);
    }

    @Test
    public void testPrototype() {
        final TestPrototype testPrototype1 = injector.getInstance(TestPrototype.class);
        final TestPrototype testPrototype2 = injector.getInstance(TestPrototype.class);
        Assert.assertNotNull(testPrototype1);
        Assert.assertNotNull(testPrototype2);
        Assert.assertTrue(testPrototype1 != testPrototype2);
    }

    @Test
    public void testInjectMembers() {
        class TestInjectable {
            @Inject
            @Named("key")
            String testKey;
        }
        final TestInjectable testInjectable = new TestInjectable();
        injector.injectMembers(testInjectable);
        assertEquals(testInjectable.testKey, "value");
    }

    interface IService {
        String foo();
    }

    @Named(FOO_BEAN)
    @SuppressWarnings("unused")
    static class IServiceImpl implements IService {

        public String foo() {
            return "foo";
        }
    }

    @Produces
    @Named("key")
    @SuppressWarnings("unused")
    String keyProduce() {
        return "value";
    }
}
