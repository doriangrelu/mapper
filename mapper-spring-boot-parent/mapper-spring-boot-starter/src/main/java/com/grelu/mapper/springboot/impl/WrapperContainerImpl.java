package com.grelu.mapper.springboot.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grelu.mapper.core.ObjectWrapper;
import com.grelu.mapper.core.builder.WrapperBuilder;

import com.grelu.mapper.springboot.WrapperContainer;
import org.modelmapper.internal.Pair;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Component
class WrapperContainerImpl implements WrapperContainer {

	private final List<ObjectWrapper<?, ?>> wrappers;

	private final ObjectMapper defaultObjectMapper;

	private ObjectWrapper<?, ?> defaultWrapper = null;

	public WrapperContainerImpl(List<ObjectWrapper<?, ?>> wrappersComponents) {
		this.wrappers = Collections.synchronizedList(wrappersComponents);
		this.defaultObjectMapper = new ObjectMapper();
	}

	@Override
	public WrapperContainer registerWrapper(ObjectWrapper<?, ?> wrapper) {
		this.wrappers.add(wrapper);
		return this;
	}

	@Override
	public WrapperContainer registerWrappers(ObjectWrapper<?, ?>... wrapper) {
		this.wrappers.addAll(List.of(wrapper));
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E, D> E toEntity(Class<?> clazz, D fromData, boolean triggerMap, String option) {
		return this.<E, D>resolveEntityWrapper(clazz, option).toEntity(fromData, (Class<E>) clazz, triggerMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E, D> D toData(Class<?> clazz, E fromEntity, boolean triggerMap, String option) {
		return this.<E, D>resolveDataWrapper(clazz, option).toData(fromEntity, (Class<D>) clazz, triggerMap);
	}

	@Override
	public <E> E mapEntity(Class<?> clazz, E entity, String option) {
		return this.<E, Object>resolveEntityWrapper(clazz, option).mapEntity(entity);
	}

	@Override
	public <D> D mapData(Class<?> clazz, D data, String option) {
		return this.<Object, D>resolveDataWrapper(clazz, option).mapData(data);
	}

	@Override
	public <E, D> ObjectWrapper<? super E, D> resolveDataWrapper(Class<?> target, String option) {
		return this.resolveWrapper(
				entityDomainWrapper -> entityDomainWrapper.supportData(target, option),
				"Missing required mapper for " + target + " with options {" + option + "}"
		);
	}

	@Override
	public <E, D> ObjectWrapper<E, ? super D> resolveEntityWrapper(Class<?> target, String option) {
		return this.resolveWrapper(
				entityDomainWrapper -> entityDomainWrapper.supportEntity(target, option),
				"Missing required mapper for " + target + " with options {" + option + "}"
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, D> List<D> toDataObjects(List<E> fromEntities, Pair<Class<? extends E>, Class<? extends D>>... clazz) {
		return this.tosObject(fromEntities, this::toData, Arrays.asList(clazz));
	}

	@SafeVarargs
	@Override
	public final <E, D> List<Map<String, Object>> toDataObjectsFlatten(List<E> fromEntities, Pair<Class<? extends E>, Class<? extends D>>... clazz) {
		return this.flatMap(this.toDataObjects(fromEntities, clazz));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, D> List<E> toEntityObjects(List<D> fromDatas, Pair<Class<? extends D>, Class<? extends E>>... clazz) {
		return this.tosObject(fromDatas, this::toEntity, Arrays.asList(clazz));
	}

	@SafeVarargs
	@Override
	public final <E, D> List<Map<String, Object>> toEntityObjectsFlatten(List<D> fromDatas, Pair<Class<? extends D>, Class<? extends E>>... clazz) {
		return this.flatMap(this.toEntityObjects(fromDatas, clazz));
	}

	@Override
	public List<Map<String, Object>> flatMap(List<?> objects, boolean addTypeCharacteristic) {
		return objects.stream().map(o -> { //NOSONAR
					Map<String, Object> flatten = this.defaultObjectMapper.convertValue(o, Map.class);
					if (addTypeCharacteristic) {
						String flattenType = o.getClass().toString();
						flatten.putIfAbsent("_type", flattenType.substring(flattenType.lastIndexOf('.') + 1));
					}
					return flatten;
				})
				.toList();
	}

	private <F, T> List<T> tosObject(List<F> objects, BiFunction<Class<? extends T>, F, T> converterDelegate, List<Pair<Class<? extends F>, Class<? extends T>>> mapping) {
		return
				objects //NOSONAR
						.stream()
						.map(entity -> {
							final Pair<Class<? extends F>, Class<? extends T>> type = this.extractMapping(entity.getClass(), mapping);
							return converterDelegate.apply(type.getRight(), entity);
						})
						.toList();
	}

	private <F, T> Pair<Class<? extends F>, Class<? extends T>> extractMapping(Class<?> targetClazz, List<Pair<Class<? extends F>, Class<? extends T>>> mapping) {
		return mapping.stream().filter(pairs -> pairs.getLeft().equals(targetClazz)).findFirst().orElseThrow();
	}

	@SuppressWarnings("unchecked")
	private <E, D> ObjectWrapper<E, D> resolveWrapper(Predicate<ObjectWrapper<?, ?>> predicate, String exceptionMessage) {
		return (ObjectWrapper<E, D>) this.wrappers.stream()
				.sorted((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()))
				.filter(predicate)
				.findFirst()
				.orElseGet(this::defaultWrapper);
	}

	@SuppressWarnings("unchecked")
	private <E, D> ObjectWrapper<E, D> defaultWrapper() {
		if (null == this.defaultWrapper) {
			this.defaultWrapper = WrapperBuilder.getInstance()
					.setDataClazzType(Object.class)
					.setEntityClazzType(Object.class)
					.build();
		}
		return (ObjectWrapper<E, D>) this.defaultWrapper;
	}

}
