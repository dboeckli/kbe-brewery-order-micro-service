package ch.dboeckli.springframeworkguru.kbe.order.services.test.config;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

public class TestClassOrderer implements ClassOrderer {
    private static final String THIS_PACKAGE = TestClassOrderer.class.getPackageName();

    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(TestClassOrderer::getOrder));
    }

    private static int getOrder(ClassDescriptor classDescriptor) {
        Class<?> testClass = classDescriptor.getTestClass();
        String className = classDescriptor.getDisplayName();
        if (testClass.getPackageName().equals(THIS_PACKAGE)) {
            return 0;
        }

        if (className.endsWith("WiremockTest")) {
            return 2;
        } else if (className.endsWith("Test")) {
            return 1;
        } else if (className.endsWith("BreweryOrderServiceIT")) {
            return 3;
        } else if (className.endsWith("IT")) {
            return 4;
        } else {
            throw new IllegalArgumentException("Test class " + className + " does not end with 'Test', 'IT'");
        }
    }
}
