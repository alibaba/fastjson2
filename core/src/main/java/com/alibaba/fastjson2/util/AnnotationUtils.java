package com.alibaba.fastjson2.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * Annotation utils transformed from {@code org.junit.platform.commons.util.AnnotationUtils}
 *
 * @author lzhpo
 */
public final class AnnotationUtils {
    private AnnotationUtils() {
    }

    /**
     * Find the first annotation of {@code annotationType} that is either
     * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
     * present</em> on the supplied {@code element}.
     *
     * <p>If the element is a class and the annotation is neither <em>directly
     * present</em> nor <em>meta-present</em> on the class, this method will additionally search on
     * interfaces implemented by the class before finding an annotation that is <em>indirectly
     * present</em> on the class.
     *
     * @param element the element on which to search for the annotation
     * @param annotationType the annotation type of need to search
     * @param <A> the annotation
     * @return the searched annotation type
     */
    public static <A extends Annotation> A findAnnotation(AnnotatedElement element, Class<A> annotationType) {
        Objects.requireNonNull(annotationType, "annotationType must not be null");
        boolean inherited = annotationType.isAnnotationPresent(Inherited.class);
        return findAnnotation(element, annotationType, inherited, new HashSet<>());
    }

    /**
     * If the {@code annotation}'s annotationType is not {@code annotationType}, then to find the
     * first annotation of {@code annotationType} that is either
     * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
     * present</em> on the supplied {@code element}.
     *
     * @param annotation annotation
     * @param annotationType the annotation type of need to search
     * @param <A> the searched annotation type
     * @return the searched annotation
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A findAnnotation(Annotation annotation, Class<A> annotationType) {
        Objects.requireNonNull(annotation, "annotation must not be null");
        Objects.requireNonNull(annotationType, "annotationType must not be null");

        Class<? extends Annotation> annotationTypeClass = annotation.annotationType();
        if (annotationTypeClass == annotationType) {
            return (A) annotation;
        }

        boolean inherited = annotationType.isAnnotationPresent(Inherited.class);
        return findAnnotation(annotationTypeClass, annotationType, inherited, new HashSet<>());
    }

    /**
     * Find the first annotation of {@code annotationType} that is either
     * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
     * present</em> on the supplied {@code element}.
     *
     * <p>If the element is a class and the annotation is neither <em>directly
     * present</em> nor <em>meta-present</em> on the class, this method will additionally search on
     * interfaces implemented by the class before finding an annotation that is <em>indirectly
     * present</em> on the class.
     *
     * @param element the element on which to search for the annotation
     * @param annotationType the annotation type of need to search
     * @param inherited whether has {@link Inherited}
     * @param visited this annotation whether visited
     * @param <A> the annotation type
     * @return the searched annotation
     */
    private static <A extends Annotation> A findAnnotation(AnnotatedElement element,
            Class<A> annotationType, boolean inherited, Set<Annotation> visited) {
        if (Objects.isNull(element) || Objects.isNull(annotationType)) {
            return null;
        }

        A annotation = element.getDeclaredAnnotation(annotationType);
        if (Objects.nonNull(annotation)) {
            return annotation;
        }

        Annotation[] declaredAnnotations = element.getDeclaredAnnotations();
        A directMetaAnnotation = findMetaAnnotation(annotationType, declaredAnnotations, inherited, visited);
        if (Objects.nonNull(directMetaAnnotation)) {
            return directMetaAnnotation;
        }

        if (element instanceof Class) {
            Class<?> clazz = (Class<?>) element;

            for (Class<?> ifc : clazz.getInterfaces()) {
                if (ifc != Annotation.class) {
                    A annotationOnInterface = findAnnotation(ifc, annotationType, inherited, visited);
                    if (Objects.nonNull(annotationOnInterface)) {
                        return annotationOnInterface;
                    }
                }
            }

            if (inherited) {
                Class<?> superclass = clazz.getSuperclass();
                if (Objects.nonNull(superclass) && superclass != Object.class) {
                    A annotationOnSuperclass = findAnnotation(superclass, annotationType, inherited, visited);
                    if (Objects.nonNull(annotationOnSuperclass)) {
                        return annotationOnSuperclass;
                    }
                }
            }
        }

        return findMetaAnnotation(annotationType, element.getAnnotations(), inherited, visited);
    }

    /**
     * Find meta-present on indirectly present annotations.
     *
     * @param annotationType the annotation type of need to search
     * @param candidates annotations for candidates
     * @param inherited whether has {@link Inherited}
     * @param visited this annotation whether visited
     * @param <A> the annotation type
     * @return the searched annotation
     */
    private static <A extends Annotation> A findMetaAnnotation(Class<A> annotationType,
            Annotation[] candidates, boolean inherited, Set<Annotation> visited) {
        for (Annotation candidateAnnotation : candidates) {
            Class<? extends Annotation> candidateAnnotationType = candidateAnnotation.annotationType();
            boolean isInJavaLangAnnotationPackage = candidateAnnotationType.getName().startsWith("java.lang.annotation");
            if (!isInJavaLangAnnotationPackage && visited.add(candidateAnnotation)) {
                A metaAnnotation = findAnnotation(candidateAnnotationType, annotationType, inherited, visited);
                if (Objects.nonNull(metaAnnotation)) {
                    return metaAnnotation;
                }
            }
        }
        return null;
    }
}
