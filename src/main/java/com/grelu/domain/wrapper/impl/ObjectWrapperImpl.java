package com.grelu.domain.wrapper.impl;

import com.grelu.domain.wrapper.ObjectWrapper;
import com.grelu.domain.wrapper.PureObject;
import com.grelu.domain.wrapper.builder.WrapperContext;
import com.grelu.domain.wrapper.helper.Converter;
import com.grelu.domain.wrapper.helper.Mapper;
import com.grelu.domain.wrapper.helper.Resolvable;
import org.modelmapper.ModelMapper;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ObjectWrapperImpl<E, D> implements ObjectWrapper<E, D> {

	private ModelMapper modelMapper;
	/**
	 * Business datas
	 */
	private final Converter<D, E> toEntityConverter;
	private final Converter<E, D> toDataConverter;
	private final Deque<Mapper<E>> entityMappers;
	private final Deque<Mapper<D>> dataMappers;
	private final Resolvable supportEntity;
	private final Resolvable supportData;
	private final Class<E> entityClazzType;
	private final Class<D> dataClazzType;
	private final int priority;

	private final Map<String, Object> contextParameters;

	/**
	 * Concurrent management
	 */
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	public ObjectWrapperImpl(ModelMapper modelMapper,
							 Converter<D, E> toEntityConverter,
							 Converter<E, D> toDataConverter,
							 Deque<Mapper<E>> entityMappers,
							 Deque<Mapper<D>> dataMappers, Class<E> entityClazzType,
							 Class<D> dataClazzType, Resolvable supportEntity,
							 Resolvable supportData,
							 int priority
	) {
		this.modelMapper = modelMapper;
		this.toEntityConverter = toEntityConverter;
		this.toDataConverter = toDataConverter;
		this.entityMappers = entityMappers;
		this.dataMappers = dataMappers;
		this.entityClazzType = entityClazzType;
		this.dataClazzType = dataClazzType;
		this.supportEntity = supportEntity;
		this.supportData = supportData;
		this.priority = priority;
		this.contextParameters = new ConcurrentHashMap<>();
	}

	@Override
	public E toEntity(D fromData, Class<E> clazz, boolean triggerMap) {
		return this.to(fromData, this.toEntityConverter, clazz,
				triggerMap ?
						this.entityMappers :
						defaultDeque());
	}

	@Override
	public E toEntity(D fromData, boolean triggerMap) {
		return this.toEntity(fromData, this.entityClazzType, triggerMap);
	}

	@Override
	public List<E> toEntities(List<D> domains, Class<E> clazz, boolean triggerMap) {
		return this.tos(domains, this.toEntityConverter, clazz,
				triggerMap ?
						this.entityMappers :
						defaultDeque());
	}

	@Override
	public List<E> toEntities(List<D> fromDatas, boolean triggerMap) {
		return this.toEntities(fromDatas, this.entityClazzType, triggerMap);
	}

	public D toData(E fromEntity, boolean triggerMap) {
		return this.toData(fromEntity, this.dataClazzType, triggerMap);
	}

	@Override
	public D toData(E fromEntity, Class<D> clazz, boolean triggerMap) {
		return this.to(fromEntity, this.toDataConverter, clazz,
				triggerMap ?
						this.dataMappers :
						defaultDeque());
	}

	@Override
	public List<D> toDatas(List<E> fromEntities, boolean triggerMap) {
		return this.toDatas(fromEntities, this.dataClazzType, triggerMap);
	}

	@Override
	public List<D> toDatas(List<E> fromEntities, Class<D> clazz, boolean triggerMap) {
		return this.tos(fromEntities, this.toDataConverter, clazz,
				triggerMap ?
						this.dataMappers :
						defaultDeque());
	}

	public E mapEntity(E entity) {
		return this.map(entity, this.entityMappers);
	}

	@Override
	public List<E> mapEntities(List<E> entity) {
		return this.maps(entity, this.entityMappers);
	}

	@Override
	public D mapData(D data) {
		return this.map(data, this.dataMappers);
	}

	@Override
	public List<D> mapDatas(List<D> datas) {
		return this.maps(datas, this.dataMappers);
	}

	@Override
	public boolean supportData(Class<?> clazz, String option) {
		return this.support(clazz, this.dataClazzType, option, this.supportData);
	}

	@Override
	public boolean supportEntity(Class<?> clazz, String option) {
		return this.support(clazz, this.entityClazzType, option, this.supportEntity);
	}

	@SuppressWarnings("unchecked")
	public <F, T> List<T> tos(List<F> o, Converter<F, T> converterDelegate, Class<T> clazz, Deque<Mapper<T>> mapperDelegate) {
		return o.parallelStream().map(f -> this.to(f, converterDelegate, clazz, mapperDelegate)).toList();
	}

	public <F, T> T to(F o, Converter<F, T> converterDelegate, Class<T> clazz, Deque<Mapper<T>> mapperDelegate) {
		try {
			this.readWriteLock.readLock().lock();
			if (converterDelegate == null) {
				return this.createContext(o, clazz).useDefaultModelMapper(clazz);
			}
			return this.map(converterDelegate.convert(this.createContext(o, clazz)), new ArrayDeque<>(mapperDelegate));
		} catch (Exception e) {
			throw new IllegalStateException("Unexpected exception during conversion", e);
		} finally {
			this.readWriteLock.readLock().lock();
		}
	}

	public <T> List<T> maps(List<T> os, Deque<Mapper<T>> mapperDelegates) {
		return os.parallelStream().map(t -> this.map(t, new ArrayDeque<>(mapperDelegates))).toList();
	}

	private static <T> Deque<Mapper<T>> defaultDeque() {
		return new ArrayDeque<>();
	}

	private <T, P> boolean support(Class<T> targetClazz, Class<P> compareClazz, String option, Resolvable delegate) {
		Assert.notNull(option, "Required option is missing");
		if (delegate != null) {
			return delegate.support(targetClazz, option);
		}
		return compareClazz.equals(targetClazz);
	}

	private <F, T> WrapperContext<F, T> createContext(F value, Class<T> clazz) {
		try { // On vérouille car on accède au state des paramètres de context qui peuvent changer
			return new WrapperContext<>(this.modelMapper, value, clazz, this.contextParameters);
		} finally {
			this.readWriteLock.readLock().unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T map(T o, Deque<Mapper<T>> mapperDelegates) {
		return Mapper.recursiveMap(o, mapperDelegates);
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public ObjectWrapper<E, D> setContextParameters(Map<String, Object> parameters) {
		try {
			this.readWriteLock.writeLock().lock();
			this.contextParameters.clear();
			this.contextParameters.putAll(parameters);
		} finally {
			this.readWriteLock.writeLock().unlock();
		}
		return this;
	}


}
