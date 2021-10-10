package com.grelu.domain.wrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @param <E> Type d'entité
 * @param <D> Type de domaine
 * @author Dorian GRELU
 * <p>
 * Permets de mettre à disposition un "wrapper"
 * Ce wrapper permettra convertir des entités, des DTO.
 * Il permettra aussi d'effectuer un map sur les différents objets soit à la demande, soit à la conversion
 */
public interface ObjectWrapper<E, D> {

	public static final String DEFAULT_OPTION = "_";

	/**
	 * Convertis un objet métier en entité
	 * Attention déclenche un Map après la conversion
	 *
	 * @param fromData domaine métier
	 * @return entité
	 */
	default E toEntity(D fromData) {
		return this.toEntity(fromData, true);
	}

	/**
	 * Convertis un objet métier en entité, en forçant le type de retours, sans utiliser celui configuré dans le bean
	 *
	 * @param domain domaine métier
	 * @param clazz  type d'entité
	 * @return entité
	 */
	default E toEntity(D domain, Class<E> clazz) {
		return this.toEntity(domain, clazz, true);
	}

	/**
	 * Convertis un objet métier en entité, en forçant le type de retours, sans utiliser celui configuré dans le bean
	 *
	 * @param fromData   domaine métier
	 * @param clazz      type d'entité
	 * @param triggerMap déclencher un map ?
	 * @return entité
	 */
	E toEntity(D fromData, Class<E> clazz, boolean triggerMap);

	/**
	 * Convertis un objet métier en entité
	 *
	 * @param fromData   domaine métier
	 * @param triggerMap déclenchement du Map ?
	 * @return entité
	 */
	E toEntity(D fromData, boolean triggerMap);

	default List<E> toEntities(List<D> datas, Class<E> clazz) {
		return this.toEntities(datas, clazz, true);
	}

	List<E> toEntities(List<D> domains, Class<E> clazz, boolean triggerMap);

	/**
	 * Convertis une liste d'objets métier en entité
	 * Déclenche un Map automatiquement
	 *
	 * @param datas domaines métiers
	 * @return Liste d'entité
	 */
	default List<E> toEntities(List<D> datas) {
		return this.toEntities(datas, true);
	}

	/**
	 * Convertis une liste d'objets métier en entité
	 *
	 * @param fromDatas  domaines métiers
	 * @param triggerMap Déclenchement du Map ?
	 * @return Liste d'entité
	 */
	List<E> toEntities(List<D> fromDatas, boolean triggerMap);

	/**
	 * Convertis une entité en domaine métier
	 * Déclenche un Map automatiquement
	 *
	 * @param fromEntity entité
	 * @return domaine
	 */
	default D toData(E fromEntity) {
		return this.toData(fromEntity, true);
	}

	/**
	 * Convertis une entité en domaine métier
	 *
	 * @param fromEntity entité
	 * @param triggerMap Déclenchement du Map ?
	 * @return
	 */
	D toData(E fromEntity, boolean triggerMap);

	default D toData(E fromEntity, Class<D> clazz) {
		return this.toData(fromEntity, clazz, true);
	}

	D toData(E fromEntity, Class<D> clazz, boolean triggerMap);

	/**
	 * Convertis une liste d'entité en domaine métier
	 * Déclenche un Map automatiquement
	 *
	 * @param fromEntities liste des entités
	 * @return liste des domaines
	 */
	default List<D> toDatas(List<E> fromEntities) {
		return this.toDatas(fromEntities, true);
	}

	/**
	 * Convertis une liste d'entité en domaine métier
	 *
	 * @param fromEntities iste des entités
	 * @param triggerMap   Déclenchement du Map ?
	 * @return liste des domaines
	 */
	List<D> toDatas(List<E> fromEntities, boolean triggerMap);

	default List<D> toDatas(List<E> fromEntities, Class<D> clazz) {
		return this.toDatas(fromEntities, clazz, true);
	}

	List<D> toDatas(List<E> fromEntities, Class<D> clazz, boolean triggerMap);

	/**
	 * Map une entité
	 * Attention, si l'entité est un Pure Object, alors l'instance retournée sera différente de celle en entrée
	 *
	 * @param entity entité à Map
	 * @return Entité après Map
	 */
	E mapEntity(E entity);

	/**
	 * Applique la même chose que la fonction mapEntity, mais sur une liste
	 *
	 * @param entity liste des entités
	 * @return liste des entités après Map
	 */
	List<E> mapEntities(List<E> entity);

	/**
	 * Même fonctionnement que le mapping des entités
	 *
	 * @param data domaine
	 * @return domaine après map
	 */
	D mapData(D data);

	/**
	 * Même principe que la fonction mapDomain, mais sur une liste
	 *
	 * @param datas liste des domaines
	 * @return liste des domaines après Map
	 */
	List<D> mapDatas(List<D> datas);

	/**
	 * Support un domaine ?
	 *
	 * @param clazz type du domaine
	 * @return Oui / Non ?
	 */
	default boolean supportData(Class<?> clazz) {
		return this.supportData(clazz, DEFAULT_OPTION);
	}

	/**
	 * Support un domaine dépendant d'une spécification ?
	 *
	 * @param clazz  type de domaine
	 * @param option spécification
	 * @return Oui / Non ?
	 */
	boolean supportData(Class<?> clazz, String option);

	/**
	 * Support une entité ?
	 *
	 * @param clazz type d'entité
	 * @return Oui / Non ?
	 */
	default boolean supportEntity(Class<?> clazz) {
		return this.supportEntity(clazz, DEFAULT_OPTION);
	}

	/**
	 * Support une entité dépendant d'une spécification ?
	 *
	 * @param clazz  type d'entité
	 * @param option spécification
	 * @return Oui / Non ?
	 */
	boolean supportEntity(Class<?> clazz, String option);

	/**
	 * Permets de donner un niveau de priorité
	 * Cette option est nécéssaire lors de l'usage en mode "conteneur"
	 * Si deux composants répondent présents à la demande, alors celui avec le plus haut niveau de priorité sera conservé
	 *
	 * @return niveau de priorité
	 */
	default int getPriority() {
		return -1;
	}

	/**
	 * Permets de définir le context
	 * Ce context est un état, il peut être modifié
	 *
	 * @param parameters
	 * @return
	 */
	ObjectWrapper<E, D> setContextParameters(Map<String, Object> parameters);

	default CompletableFuture<D> toDataAsync(E fromEntity) {
		return CompletableFuture.supplyAsync(() -> this.toData(fromEntity));
	}

	default List<CompletableFuture<D>> toDatasAsync(List<E> fromEntities) {
		return fromEntities.stream().map(this::toDataAsync).toList();
	}

	default CompletableFuture<E> toEntityAsync(D fromData) {
		return CompletableFuture.supplyAsync(() -> this.toEntity(fromData));
	}

	default List<CompletableFuture<E>> toEntitiesAsync(List<D> fromDatas) {
		return fromDatas.stream().map(this::toEntityAsync).toList();
	}
	
	default Optional<E> toSafeEntity(D fromData) {
		if (null == fromData) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.toEntity(fromData));
	}


	default Optional<D> toSafeData(E fromEntity) {
		if (null == fromEntity) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.toData(fromEntity));
	}

}
