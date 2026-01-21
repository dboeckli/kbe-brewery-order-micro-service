package ch.dboeckli.springframeworkguru.kbe.order.services.test.config;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

public class TestClassOrderer implements ClassOrderer {
    private static int getOrder(ClassDescriptor classDescriptor) {
        String className = classDescriptor.getTestClass().getSimpleName();
        return switch (className) {
            case String name when name.endsWith("BreweryOrderServiceIT") -> 3;
            case String name when name.endsWith("IT") -> 4;
            case String name when name.endsWith("WiremockTest") -> 2;
            case String name when name.endsWith("Test") || name.endsWith("Tests") -> 1;
            default -> Integer.MAX_VALUE;
        };
    }

    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(TestClassOrderer::getOrder));
    }
}
