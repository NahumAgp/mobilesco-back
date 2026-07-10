package com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort;

import org.springframework.data.core.PropertyReference;
import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.domain.Sort;

public final class TypeSafeSorts {

    private TypeSafeSorts() {
    }

    public static <T, S> Sort asc(Class<T> type, TypedPropertyPath<T, S> property) {
        return Sort.by(Sort.Order.asc(property));
    }

    public static <T, S> Sort desc(Class<T> type, TypedPropertyPath<T, S> property) {
        return Sort.by(Sort.Order.desc(property));
    }

    public static <T, I> Sort ascById(Class<T> type, TypedPropertyPath<T, I> idProperty) {
        return asc(type, idProperty);
    }

    public static <T, I> Sort descById(Class<T> type, TypedPropertyPath<T, I> idProperty) {
        return desc(type, idProperty);
    }

    public static <T, S, I> Sort ascWithId(Class<T> type, TypedPropertyPath<T, S> property, TypedPropertyPath<T, I> idProperty) {
        return asc(type, property).and(asc(type, idProperty));
    }

    public static <T, S, I> Sort descWithId(Class<T> type, TypedPropertyPath<T, S> property, TypedPropertyPath<T, I> idProperty) {
        return desc(type, property).and(asc(type, idProperty));
    }

    public static <T, U, V, I> Sort ascNestedWithId(
            Class<T> type,
            TypedPropertyPath<T, U> property,
            PropertyReference<U, V> nestedProperty,
            TypedPropertyPath<T, I> idProperty) {
        return Sort.by(Sort.Order.asc(property.then(nestedProperty)))
                .and(asc(type, idProperty));
    }

    public static <T, U, V, I> Sort descNestedWithId(
            Class<T> type,
            TypedPropertyPath<T, U> property,
            PropertyReference<U, V> nestedProperty,
            TypedPropertyPath<T, I> idProperty) {
        return Sort.by(Sort.Order.desc(property.then(nestedProperty)))
                .and(asc(type, idProperty));
    }

    public static <T, U, V, W, I> Sort ascNestedWithId(
            Class<T> type,
            TypedPropertyPath<T, U> property,
            PropertyReference<U, V> nestedProperty,
            PropertyReference<V, W> nestedNestedProperty,
            TypedPropertyPath<T, I> idProperty) {
        return Sort.by(Sort.Order.asc(property.then(nestedProperty).then(nestedNestedProperty)))
                .and(asc(type, idProperty));
    }

    public static <T, U, V, W, I> Sort descNestedWithId(
            Class<T> type,
            TypedPropertyPath<T, U> property,
            PropertyReference<U, V> nestedProperty,
            PropertyReference<V, W> nestedNestedProperty,
            TypedPropertyPath<T, I> idProperty) {
        return Sort.by(Sort.Order.desc(property.then(nestedProperty).then(nestedNestedProperty)))
                .and(asc(type, idProperty));
    }

    public static <T, U, V, W, X, I> Sort ascNestedWithId(
            Class<T> type,
            TypedPropertyPath<T, U> property,
            PropertyReference<U, V> nestedProperty,
            PropertyReference<V, W> nestedNestedProperty,
            PropertyReference<W, X> thirdNestedProperty,
            TypedPropertyPath<T, I> idProperty) {
        return Sort.by(Sort.Order.asc(property.then(nestedProperty).then(nestedNestedProperty).then(thirdNestedProperty)))
                .and(asc(type, idProperty));
    }

    public static <T, U, V, W, X, I> Sort descNestedWithId(
            Class<T> type,
            TypedPropertyPath<T, U> property,
            PropertyReference<U, V> nestedProperty,
            PropertyReference<V, W> nestedNestedProperty,
            PropertyReference<W, X> thirdNestedProperty,
            TypedPropertyPath<T, I> idProperty) {
        return Sort.by(Sort.Order.desc(property.then(nestedProperty).then(nestedNestedProperty).then(thirdNestedProperty)))
                .and(asc(type, idProperty));
    }
}
