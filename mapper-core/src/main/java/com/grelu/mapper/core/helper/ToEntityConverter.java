package com.grelu.mapper.core.helper;

@FunctionalInterface
public interface ToEntityConverter<E, D> extends Converter<D, E> {
}
