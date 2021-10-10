package com.test.grelu.mapper.sapstarter;

import com.grelu.mapper.core.ObjectWrapper;
import com.grelu.mapper.core.builder.WrapperBuilder;
import com.grelu.gsmarts.utility.reflection.ReflectionUtils;


import com.grelu.mapper.springboot.WrapperContainer;
import com.test.grelu.mapper.sapstarter.mock.DomainMock;
import com.test.grelu.mapper.sapstarter.mock.EntityMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Locale;


@SpringBootTest(classes = SpringBootApplicationMapperTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WrapperContainerImplTest {

	@Autowired
	private WrapperContainer container;

	@Test
	public void testAddWrapperInContainer() throws NoSuchFieldException, IllegalAccessException {
		ObjectWrapper<EntityMock, DomainMock> firstWrapper = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class).build();
		ObjectWrapper<EntityMock, DomainMock> secondWrapper = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class).build();

		container.registerWrappers(firstWrapper)
				.registerWrappers(secondWrapper);

		List<ObjectWrapper<?, ?>> wrappers = ReflectionUtils.get(container, "wrappers");

		Assertions.assertThat(wrappers).hasSize(2).contains(firstWrapper, secondWrapper);
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


		container.registerWrappers(secondWrapper, firstWrapper);

		EntityMock en = new EntityMock();
		en.lastname = "jacques";

		EntityMock mapped = container.mapEntity(EntityMock.class, en, "light");

		Assertions.assertThat(mapped.lastname).isEqualTo("JACQUES");
	}

	@Test
	public void testWrapperEntityWithDefaultWrapper() throws NoSuchFieldException, IllegalAccessException {

		EntityMock en = new EntityMock();
		en.lastname = "jacques";

		EntityMock mapped = container.mapEntity(EntityMock.class, en);

		Assertions.assertThat(mapped.lastname).isEqualTo("jacques");
	}

}
