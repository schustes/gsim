package de.s2.gsim.core;

import java.util.List;

public abstract class GSimCoreFactory {

    public abstract GSimCore createCore();

    public static GSimCoreFactory defaultFactory() {

        try {
            List<Class<?>> classes = ClassSearchUtils.searchClassPath();

            GSimCoreFactory factory = null;
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(CoreFactory.class)) {
                    if (clazz.getAnnotation(CoreFactory.class).isDefault()) {
                        return (GSimCoreFactory) clazz.newInstance();
                    }
                }
            }
            return factory;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new GSimException(e);
        }

    }

    public static GSimCoreFactory customFactory(String name) {

        try {
            List<Class<?>> classes = ClassSearchUtils.searchClassPath();
            GSimCoreFactory factory = null;

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(CoreFactory.class)) {
                    CoreFactory ann = clazz.getAnnotation(CoreFactory.class);
                    if (ann.isDefault() && ann.name().equals(name)) {
                        return (GSimCoreFactory) clazz.newInstance();
                    }
                }
            }
            return factory;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new GSimException(e);
        }

    }

}
