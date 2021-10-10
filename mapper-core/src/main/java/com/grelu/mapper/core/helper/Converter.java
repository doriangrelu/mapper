package com.grelu.mapper.core.helper;


import com.grelu.mapper.core.builder.WrapperContext;


@FunctionalInterface
public interface Converter<F, T> {

	T convert(WrapperContext<F, T> context) throws Exception;

}
