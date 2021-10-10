package com.grelu.mapper.springboot;

import com.grelu.mapper.core.ObjectWrapper;
import org.modelmapper.internal.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Dorian GRELU
 * Conteneur de wrapper
 */
public interface WrapperContainer {

	default <E, D> CompletableFuture<D> toDataAsync(Class<D> clazz, E fromEntity) {
		return (CompletableFuture<D>) this.<E, D>resolveEntityWrapper(clazz, ObjectWrapper.DEFAULT_OPTION)
				.toDataAsync(fromEntity);
	}

	default <E, D> CompletableFuture<D> toEntityAsync(Class<E> clazz, D fromData) {
		return (CompletableFuture<D>) this.<E, D>resolveEntityWrapper(clazz, ObjectWrapper.DEFAULT_OPTION)
				.toEntityAsync(fromData);
	}


	default <E, D> Optional<E> toSafeEntity(Class<E> clazz, D fromData) {
		return this.<E, D>resolveEntityWrapper(clazz, ObjectWrapper.DEFAULT_OPTION)
				.toSafeEntity(fromData);
	}

	default <E, D> Optional<D> toSafeData(Class<D> clazz, E fromEntity) {
		return (Optional<D>) this.<E, D>resolveEntityWrapper(clazz, ObjectWrapper.DEFAULT_OPTION)
				.toSafeData(fromEntity);
	}

	WrapperContainer registerWrapper(ObjectWrapper<?, ?> wrapper);

	WrapperContainer registerWrappers(ObjectWrapper<?, ?>... wrapper);

	default <E> Optional<? extends E> to(Class<E> clazz, Object from) {
		List<CompletableFuture<E>> futures = Arrays.asList(
				CompletableFuture.supplyAsync(() -> this.toEntity(clazz, from)),
				CompletableFuture.supplyAsync(() -> this.toData(clazz, from))
		);

		try {
			return CompletableFuture.allOf(futures.get(0), futures.get(1)).thenApply(unused -> {
				return futures.stream()
						.map(eCompletableFuture -> {
							try {
								return eCompletableFuture.get();
							} catch (InterruptedException | ExecutionException e) {
								throw new IllegalStateException(e);
							}
						})
						.filter(Objects::nonNull)
						.findAny();
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new IllegalStateException(e);
		}
	}

	default <E, D> E toEntity(Class<?> clazz, D fromData) {
		return this.toEntity(clazz, fromData, true, ObjectWrapper.DEFAULT_OPTION);
	}

	<E, D> E toEntity(Class<?> clazz, D fromSecond, boolean triggerMap, String option);

	default <E, D> List<E> toEntities(Class<?> clazz, List<D> fromDatas) {
		return this.toEntities(clazz, fromDatas, true, ObjectWrapper.DEFAULT_OPTION);
	}

	@SuppressWarnings("unchecked")
	default <E, D> List<E> toEntities(Class<?> clazz, List<D> fromSeconds, boolean triggerMap, String option) {
		return (List<E>) fromSeconds.parallelStream()
				.map(domain -> this.toEntity(clazz, domain, triggerMap, option))
				.toList();
	}

	default <E, D> D toData(Class<?> clazz, E fromEntity) {
		return this.toData(clazz, fromEntity, true, ObjectWrapper.DEFAULT_OPTION);
	}

	<E, D> D toData(Class<?> clazz, E fromEntity, boolean triggerMap, String option);

	@SuppressWarnings("unchecked")
	<E, D> List<D> toDataObjects(List<E> fromEntities, Pair<Class<? extends E>, Class<? extends D>>... clazz);

	@SuppressWarnings("unchecked")
	<E, D> List<Map<String, Object>> toDataObjectsFlatten(List<E> fromEntities, Pair<Class<? extends E>, Class<? extends D>>... clazz);

	@SuppressWarnings("unchecked")
	<E, D> List<E> toEntityObjects(List<D> fromDatas, Pair<Class<? extends D>, Class<? extends E>>... clazz);

	@SuppressWarnings("unchecked")
	<E, D> List<Map<String, Object>> toEntityObjectsFlatten(List<D> fromDatas, Pair<Class<? extends D>, Class<? extends E>>... clazz);

	default List<Map<String, Object>> flatMap(List<?> objects) {
		return this.flatMap(objects, true);
	}

	List<Map<String, Object>> flatMap(List<?> objects, boolean addTypeCharacteristic);

	default <E, D> List<D> toDatas(Class<?> clazz, List<E> fromEntities) {
		return this.toDatas(clazz, fromEntities, ObjectWrapper.DEFAULT_OPTION);
	}

	default <E, D> List<D> toDatas(Class<?> clazz, List<E> fromEntities, String option) {
		return this.toDatas(clazz, fromEntities, true, ObjectWrapper.DEFAULT_OPTION);
	}

	@SuppressWarnings("unchecked")
	default <E, D> List<D> toDatas(Class<?> clazz, List<E> fromEntities, boolean triggerMap, String option) {
		return (List<D>) fromEntities.parallelStream().map(entity -> this.toData(clazz, entity, triggerMap, option)).toList();
	}

	<E> E mapEntity(Class<?> clazz, E entity, String option);

	default <E> E mapEntity(Class<?> clazz, E entity) {
		return this.mapEntity(clazz, entity, ObjectWrapper.DEFAULT_OPTION);
	}

	default <E> List<E> mapEntities(Class<?> clazz, List<E> entities, String option) {
		return entities.parallelStream()
				.map(entity -> this.mapEntity(clazz, entity, option))
				.toList();
	}

	<D> D mapData(Class<?> clazz, D data, String option);

	default <D> List<D> mapDatas(Class<?> clazz, List<D> datas, String option) {
		return datas.parallelStream()
				.map(domain -> this.mapData(clazz, domain, option))
				.toList();
	}

	<E, D> ObjectWrapper<? super E, D> resolveDataWrapper(Class<?> target, String option);

	<E, D> ObjectWrapper<E, ? super D> resolveEntityWrapper(Class<?> target, String option);

}
