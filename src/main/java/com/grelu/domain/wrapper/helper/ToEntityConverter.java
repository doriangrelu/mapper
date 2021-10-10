package com.grelu.domain.wrapper.helper;

@FunctionalInterface
public interface ToEntityConverter<E, D> extends Converter<D, E> {
}
