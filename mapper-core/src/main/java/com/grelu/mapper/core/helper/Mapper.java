package com.grelu.mapper.core.helper;

import com.grelu.mapper.core.PureObject;
import org.slf4j.LoggerFactory;

import java.util.Deque;

@FunctionalInterface
public interface Mapper<E> {


	/**
	 * Permets d'appliquer un mapping sur les objets retournés
	 *
	 * @param o
	 * @return
	 */
	E map(E o) throws Exception;

	static <T> T recursiveMap(T o, Deque<Mapper<T>> queue) {
		T target = o instanceof PureObject ?
				(T) ((PureObject) o).clone() :
				o;

		Mapper<T> delegate = queue.poll();

		if (delegate == null) {
			return target;
		}

		try {
			return recursiveMap(delegate.map(target), queue);
		} catch (Exception e) { // En cas d'erreur du converter, on ignore, et on log
			LoggerFactory.getLogger(Mapper.class).error("Illegal exception in mapping state. Ignore mapper [{}]", delegate, e);
			return recursiveMap(target, queue);
		}
	}
}
