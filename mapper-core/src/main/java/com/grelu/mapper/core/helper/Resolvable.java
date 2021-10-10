package com.grelu.mapper.core.helper;

@FunctionalInterface
public interface Resolvable {

	boolean support(Class<?> clazz, String option);

}
