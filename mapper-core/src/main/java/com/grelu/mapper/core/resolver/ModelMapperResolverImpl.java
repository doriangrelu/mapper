package com.grelu.mapper.core.resolver;

import org.modelmapper.ModelMapper;


import java.util.Optional;


class ModelMapperResolverImpl implements ModelMapperResolver {

	private final Optional<ModelMapper> resolvedMapper;

	public ModelMapperResolverImpl(Optional<ModelMapper> resolvedMapper) {
		this.resolvedMapper = resolvedMapper;
	}

	public synchronized ModelMapper resolve() {
		return this.resolvedMapper.orElseGet(ModelMapper::new);
	}

}
