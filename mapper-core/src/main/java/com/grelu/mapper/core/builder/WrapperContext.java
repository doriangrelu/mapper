package com.grelu.mapper.core.builder;


import org.modelmapper.ModelMapper;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WrapperContext<F, T> {


	private final ModelMapper modelMapper;
	private final F value;
	private Class<T> clazz;
	private final Map<String, Object> parameters;

	public WrapperContext(final ModelMapper modelMapper, final F value, final Class<T> clazz, Map<String, Object> parameters) {
		this.modelMapper = modelMapper;
		this.value = value;
		this.clazz = clazz;
		this.parameters = new HashMap<>(parameters); // On clone pour avoir une instance isolée
	}


	public T useDefaultModelMapper(Class<T> clazz) {
		if (!this.clazz.equals(clazz)) {
			this.clazz = clazz;
		}
		return this.useDefaultModelMapper();
	}

	public T useDefaultModelMapper() {
		return this.useCustomModelMapper(this.modelMapper);
	}

	public T useCustomModelMapper(final ModelMapper mapper) {
		if (mapper == null) {
			throw new IllegalStateException("Missing required default model mapper");
		}
		return mapper.map(this.getValue(), clazz);
	}

	public Optional<Object> getParameter(String name) {
		return Optional.ofNullable(this.parameters.getOrDefault(name, null));
	}

	public F getValue() {
		return value;
	}
}
