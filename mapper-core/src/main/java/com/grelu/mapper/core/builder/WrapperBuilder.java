package com.grelu.mapper.core.builder;


import com.grelu.mapper.core.CustomModelMapper;
import com.grelu.mapper.core.ObjectWrapper;
import com.grelu.mapper.core.helper.Mapper;
import com.grelu.mapper.core.helper.Resolvable;
import com.grelu.mapper.core.helper.ToDataConverter;
import com.grelu.mapper.core.helper.ToEntityConverter;
import com.grelu.mapper.core.impl.ObjectWrapperImpl;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WrapperBuilder<E, D> {

	private static final Logger logger = LoggerFactory.getLogger(WrapperBuilder.class);

	private final ModelMapper mapper;

	private final Deque<Mapper<E>> entitiesMapper;

	private final Deque<Mapper<D>> datasMapper;

	private ToDataConverter<E, D> toDataConverter = null;

	private ToEntityConverter<E, D> toEntityConverter = null;

	private Class<E> entityClazzType = null;

	private Class<D> dataClazzType = null;

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private boolean hasBuild = false;

	private Resolvable supportEntity = null;

	private Resolvable supportData = null;

	private int priority = -1;


	private WrapperBuilder() {
		this(null, null);
	}

	private WrapperBuilder(Class<E> entityClazzType, Class<D> dataClazzType) {
		this(new CustomModelMapper(), entityClazzType, dataClazzType);
	}

	private WrapperBuilder(ModelMapper modelMapper, Class<E> entityClazzType, Class<D> dataClazzType) {
		logger.trace("Create new builder for entity ({}), domain ({}), and {} mapper", entityClazzType, dataClazzType, modelMapper.getClass());
		this.entityClazzType = entityClazzType;
		this.dataClazzType = dataClazzType;
		this.entitiesMapper = new ArrayDeque<>();
		this.datasMapper = new ArrayDeque<>();
		this.mapper = modelMapper;
	}


	public static <E, D> WrapperBuilder<E, D> getInstance() {
		return new WrapperBuilder<>();
	}

	public static <E, D> WrapperBuilder<E, D> getInstance(ModelMapper mapper) {
		return new WrapperBuilder<>(mapper, null, null);
	}

	public static <E, D> WrapperBuilder<E, D> getInstance(ModelMapper mapper, Class<E> entityClazz, Class<D> dataClazz) {
		return new WrapperBuilder<>(mapper, entityClazz, dataClazz);
	}

	public static <E, D> WrapperBuilder<E, D> getInstance(Class<E> entityClazz, Class<D> dataClazz) {
		return new WrapperBuilder<>(entityClazz, dataClazz);
	}

	public WrapperBuilder<E, D> setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	public WrapperBuilder<E, D> setSupportEntity(Resolvable r) {
		logger.trace("{} is support entity", r != null ? "Define" : "Reset");
		this.supportEntity = r;
		return this;
	}

	public WrapperBuilder<E, D> setSupportData(Resolvable r) {
		logger.trace("{} is support domain", r != null ? "Define" : "Reset");
		this.supportData = r;
		return this;
	}

	public WrapperBuilder<E, D> addDataMapper(Mapper<D> mapper) {
		this.checkState();
		logger.trace("Add domain mapper");
		try {
			this.readWriteLock.readLock().lock();
			this.datasMapper.add(mapper);
		} finally {
			this.readWriteLock.readLock().unlock();
		}
		return this;
	}


	public WrapperBuilder<E, D> setEntityClazzType(Class<E> clazz) {
		this.entityClazzType = clazz;
		return this;
	}


	public WrapperBuilder<E, D> setDataClazzType(Class<D> clazz) {
		this.dataClazzType = clazz;
		return this;
	}

	public WrapperBuilder<E, D> addEntityMapper(Mapper<E> mapper) {
		this.checkState();
		logger.trace("Add entity mapper");
		try {
			this.readWriteLock.readLock().lock();
			this.entitiesMapper.add(mapper);
		} finally {
			this.readWriteLock.readLock().unlock();
		}
		return this;
	}

	public WrapperBuilder<E, D> setDataConverter(ToDataConverter<E, D> converter) {
		this.checkState();
		logger.trace("{} domain converter", converter != null ? "Define" : "Unset");
		try {
			this.readWriteLock.writeLock().lock();
			this.toDataConverter = converter;
		} finally {
			this.readWriteLock.writeLock().unlock();
		}
		return this;
	}

	public WrapperBuilder<E, D> setEntityConverter(ToEntityConverter<E, D> converter) {
		this.checkState();
		logger.trace("{} entity converter", converter != null ? "Define" : "Unset");
		try {
			this.readWriteLock.writeLock().lock();
			this.toEntityConverter = converter;
		} finally {
			this.readWriteLock.writeLock().unlock();
		}
		return this;
	}


	public ObjectWrapper<E, D> build() {
		this.checkState(true);
		logger.trace("Trigger build");
		try {
			this.readWriteLock.writeLock().lock();
			this.hasBuild = true;
			return new ObjectWrapperImpl<>(this.mapper,
					this.toEntityConverter,
					this.toDataConverter,
					this.entitiesMapper,
					this.datasMapper,
					this.entityClazzType,
					this.dataClazzType,
					this.supportEntity,
					this.supportData,
					this.priority);
		} finally {
			this.readWriteLock.writeLock().unlock();
		}
	}

	private void checkState() {
		this.checkState(false);
	}


	private void checkState(boolean checkIntegrity) {
		try {
			logger.trace("Trigger build check");
			this.readWriteLock.readLock().lock();
			if (this.hasBuild) {
				throw new IllegalStateException("The wrapper has already builded");
			}
			logger.trace("Check integrity");
			if (checkIntegrity && (this.entityClazzType == null || this.dataClazzType == null)) {
				throw new IllegalStateException("Cannot build Wrapper without class informations. Please use setEntityClazz() and setDomainClazz() methods.");
			}

		} finally {
			this.readWriteLock.readLock().unlock();
		}
	}

}
