package com.github.t1.problemdetails.jaxrs.lib;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * A {@link Field} or no-arg {@link Method} but with a uniform API
 */
@RequiredArgsConstructor
class Property implements Comparable<Property> {
    static Stream<Property> allIn(Object container) {
        return Stream.concat(
            Stream.of(container.getClass().getDeclaredFields()),
            Stream.of(container.getClass().getDeclaredMethods()))
            .map(annotatedElement -> new Property(container, annotatedElement))
            .sorted();
    }

    private final Object container;
    private final AnnotatedElement annotatedElement;

    @Override public String toString() {
        return "Element[" + getName() + " of " + container + "]";
    }

    @Override public int compareTo(Property that) {
        return this.getName().compareTo(that.getName());
    }

    public String getName() {
        return ((Member) annotatedElement).getName();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return annotatedElement.isAnnotationPresent(annotation);
    }

    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return annotatedElement.getAnnotation(type);
    }

    public boolean notNull() {
        return get() != null;
    }

    public Object get() {
        if (annotatedElement instanceof Field) {
            return get((Field) annotatedElement);
        } else {
            return invoke((Method) annotatedElement);
        }
    }

    private Object get(Field field) {
        try {
            field.setAccessible(true);
            return field.get(container);
        } catch (IllegalAccessException e) {
            return "could not get " + field;
        }
    }

    private Object invoke(Method method) {
        try {
            if (method.getParameterCount() != 0)
                return invocationFailed(method, "expected no args but got " + method.getParameterCount());
            method.setAccessible(true);
            return method.invoke(container);
        } catch (IllegalAccessException e) {
            return invocationFailed(method, e);
        } catch (InvocationTargetException e) {
            return invocationFailed(method, e.getTargetException());
        }
    }

    private String invocationFailed(Method method, Object detail) {
        return "could not invoke " + method.getDeclaringClass().getSimpleName()
            + "." + method.getName() + ": " + detail;
    }
}
