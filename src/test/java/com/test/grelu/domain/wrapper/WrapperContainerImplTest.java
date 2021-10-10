package com.test.grelu.domain.wrapper;

import com.grelu.domain.wrapper.ObjectWrapper;
import com.grelu.domain.wrapper.WrapperContainer;
import com.grelu.domain.wrapper.builder.WrapperBuilder;
import com.grelu.domain.wrapper.impl.WrapperContainerImpl;
import com.grelu.gsmarts.utility.reflection.ReflectionUtils;
import com.test.grelu.domain.wrapper.mock.DomainMock;
import com.test.grelu.domain.wrapper.mock.EntityMock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class WrapperContainerImplTest {

	@Test
	public void testAddWrapperInContainer() throws NoSuchFieldException, IllegalAccessException {
		ObjectWrapper<EntityMock, DomainMock> firstWrapper = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class).build();
		ObjectWrapper<EntityMock, DomainMock> secondWrapper = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class).build();

		WrapperContainer container = new WrapperContainerImpl(new ArrayList<>());

		container.registerWrappers(firstWrapper)
				.registerWrappers(secondWrapper);

		List<ObjectWrapper<?, ?>> wrappers = ReflectionUtils.get(container, "wrappers");

		assertThat(wrappers).hasSize(2).contains(firstWrapper, secondWrapper);
	}

	@Test
	public void testWrapperEntityWithOptions() throws NoSuchFieldException, IllegalAccessException {
		ObjectWrapper<EntityMock, DomainMock> firstWrapper = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class)
				.setSupportEntity((clazz, option) -> clazz.equals(EntityMock.class) && "light".equals(option))
				.setPriority(99)
				.addEntityMapper(o -> {
					o.lastname = o.lastname.toUpperCase(Locale.ROOT);
					return o;
				})
				.build();

		ObjectWrapper<EntityMock, DomainMock> secondWrapper = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class).build();

		WrapperContainer container = new WrapperContainerImpl(new ArrayList<>());
		container.registerWrappers(secondWrapper, firstWrapper);

		EntityMock en = new EntityMock();
		en.lastname = "jacques";

		EntityMock mapped = container.mapEntity(EntityMock.class, en, "light");

		assertThat(mapped.lastname).isEqualTo("JACQUES");
	}

	@Test
	public void testWrapperEntityWithDefaultWrapper() throws NoSuchFieldException, IllegalAccessException {
		WrapperContainer container = new WrapperContainerImpl(new ArrayList<>());

		EntityMock en = new EntityMock();
		en.lastname = "jacques";

		EntityMock mapped = container.mapEntity(EntityMock.class, en);

		assertThat(mapped.lastname).isEqualTo("jacques");
	}

}
