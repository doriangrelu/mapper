package com.test.grelu.mapper.core;



import com.grelu.mapper.core.ObjectWrapper;
import com.grelu.mapper.core.builder.WrapperBuilder;
import com.test.grelu.mapper.core.mock.DomainMock;
import com.test.grelu.mapper.core.mock.EntityMock;
import com.test.grelu.mapper.core.mock.InheritedDomainMock;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class WrapperBuilderTest {

	@Test
	public void testGetInstance() {
		WrapperBuilder<EntityMock, DomainMock> wrapperBuilder = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);
		assertThat(wrapperBuilder).isInstanceOf(WrapperBuilder.class);
	}

	@Test
	public void testBuild() {
		WrapperBuilder<EntityMock, DomainMock> wrapperBuilder = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);

		ObjectWrapper<EntityMock, DomainMock> wrapper = wrapperBuilder.build();

		assertThat(wrapper).isInstanceOf(ObjectWrapper.class);

		assertThatThrownBy(() -> {
			wrapperBuilder.addDataMapper(o -> o);
		}).isInstanceOf(IllegalStateException.class);


		assertThatThrownBy(wrapperBuilder::build).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void testEntityMapper() {
		WrapperBuilder<EntityMock, DomainMock> wrapperBuilder = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);

		wrapperBuilder.addEntityMapper(o -> {
			o.lastname = "éric";
			return o;
		}).addEntityMapper(o -> {
			o.lastname = "jean-michel";
			return o;
		});


		ObjectWrapper<EntityMock, DomainMock> wrapper = wrapperBuilder.build();

		EntityMock en = new EntityMock();
		en.lastname = "jean";
		assertThat(en.lastname).isEqualTo("jean");

		EntityMock enm = wrapper.mapEntity(en);
		assertThat(enm.lastname).isEqualTo("jean-michel");
		assertThat(en.lastname).isEqualTo("jean");
	}


	@Test
	public void testEntityMapperToList() {
		WrapperBuilder<EntityMock, DomainMock> wrapperBuilder = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);

		wrapperBuilder.addEntityMapper(o -> {
			if (o.lastname.equals("eric")) {
				o.lastname = "ducon";
			} else {
				o.lastname = "condu";
			}
			return o;
		}).addEntityMapper(o -> {
			o.lastname = o.lastname.toUpperCase();
			return o;
		});


		ObjectWrapper<EntityMock, DomainMock> wrapper = wrapperBuilder.build();

		EntityMock en1 = new EntityMock();
		en1.lastname = "jean";

		EntityMock en2 = new EntityMock();
		en2.lastname = "eric";


		List<EntityMock> enms = wrapper.mapEntities(Arrays.asList(en1, en2));

		assertThat(enms).hasSize(2);

		assertThat(enms.get(0).lastname).isEqualTo("CONDU");
		assertThat(enms.get(1).lastname).isEqualTo("DUCON");
	}


	@Test
	public void testConverterSingleEntityDomain() {
		WrapperBuilder<EntityMock, DomainMock> wrapperBuilder = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);

		wrapperBuilder.setDataConverter(context -> {
			DomainMock domain = new DomainMock();
			domain.firstname = context.getValue().firstname + "_";
			domain.lastname = context.getValue().lastname + "_";
			domain.age = 30;

			return domain;
		});

		EntityMock entity = new EntityMock();
		entity.firstname = "eric";
		entity.lastname = "pierre";

		DomainMock domain = wrapperBuilder
				.build()
				.toData(entity);

		assertThat(domain.firstname).isEqualTo("eric_");
		assertThat(domain.lastname).isEqualTo("pierre_");
		assertThat(domain.age).isEqualTo(30);
	}

	@Test
	public void testConverterSingleEntityInheritedDomain() {
		WrapperBuilder<EntityMock, DomainMock> wrapperBuilder = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);

		wrapperBuilder.setDataConverter(context -> {
			DomainMock domain = new InheritedDomainMock();
			domain.firstname = context.getValue().firstname + "_";
			domain.lastname = context.getValue().lastname + "_";
			domain.age = 30;

			return domain;
		});

		EntityMock entity = new EntityMock();
		entity.firstname = "eric";
		entity.lastname = "pierre";

		DomainMock domain = wrapperBuilder
				.build()
				.toData(entity);

		assertThat(domain.firstname).isEqualTo("eric_");
		assertThat(domain.lastname).isEqualTo("pierre_");
		assertThat(domain.age).isEqualTo(30);
	}


	@Test
	public void testConverterMultipleEntityDomain() {
		WrapperBuilder<EntityMock, DomainMock> wrapperBuilder = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);

		AtomicReference<Date> date = new AtomicReference<>(new Date());

		wrapperBuilder.setDataConverter(context -> {
			DomainMock domain = new DomainMock();
			domain.firstname = "jean";
			domain.lastname = "jean";
			domain.age = 30;

			return domain;
		}).setEntityConverter(context -> {
			EntityMock entity = new EntityMock();
			entity.firstname = "eric";
			entity.lastname = "eric";
			entity.birthday = date.get();
			return entity;
		});

		EntityMock entity1 = new EntityMock();
		entity1.firstname = "eric";
		entity1.lastname = "pierre";

		EntityMock entity2 = new EntityMock();
		entity2.firstname = "luc";
		entity2.lastname = "po";

		ObjectWrapper<EntityMock, DomainMock> wrapper = wrapperBuilder.build();

		List<DomainMock> domains = wrapper.toDatas(Arrays.asList(entity1, entity2));

		assertThat(domains).hasSize(2);
		assertThat(domains.get(0)).satisfies(this::isDomain);
		assertThat(domains.get(1)).satisfies(this::isDomain);

		List<EntityMock> entities = wrapper.toEntities(domains);
		assertThat(entities).hasSize(2);
		assertThat(entities.get(0)).satisfies(this::isEntity);
		assertThat(entities.get(1)).satisfies(this::isEntity);
	}

	@Test
	public void testResolvable() {
		WrapperBuilder<EntityMock, DomainMock> wrapperBuilderWithoutCustom = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);
		ObjectWrapper<EntityMock, DomainMock> wrapper = wrapperBuilderWithoutCustom.build();

		assertThat(wrapper.supportEntity(EntityMock.class)).isTrue();
		assertThat(wrapper.supportEntity(DomainMock.class)).isFalse();
		assertThat(wrapper.supportData(DomainMock.class)).isTrue();
		assertThat(wrapper.supportData(ObjectWrapper.class)).isFalse();

		WrapperBuilder<EntityMock, DomainMock> wrapperBuilderWithCustom = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);

		wrapperBuilderWithCustom.setSupportData((clazz, option) -> {
			return clazz.equals(DomainMock.class) && option != null && option.contains("dorian");
		}).setSupportEntity((clazz, option) -> {
			return clazz.equals(EntityMock.class) && option != null && option.contains("dorian");
		});

		ObjectWrapper<EntityMock, DomainMock> secondWrapper = wrapperBuilderWithCustom.build();

		assertThat(secondWrapper.supportEntity(EntityMock.class, "dorian")).isTrue();
		assertThat(secondWrapper.supportEntity(EntityMock.class, "jean")).isFalse();
		assertThat(secondWrapper.supportEntity(DomainMock.class, "dorian")).isFalse();

		assertThat(secondWrapper.supportData(DomainMock.class, "dorian")).isTrue();
		assertThat(secondWrapper.supportData(DomainMock.class, "jean")).isFalse();
		assertThat(secondWrapper.supportData(ObjectWrapper.class, "dorian")).isFalse();

	}


	@Test
	public void testAsyncConvert() throws ExecutionException, InterruptedException {
		WrapperBuilder<EntityMock, DomainMock> wrapperBuilder = WrapperBuilder.getInstance(EntityMock.class, DomainMock.class);

		AtomicReference<Date> date = new AtomicReference<>(new Date());

		wrapperBuilder.setDataConverter(context -> {
			Thread.sleep(2000);

			DomainMock domain = new DomainMock();
			domain.firstname = "jean";
			domain.lastname = "jean";
			domain.age = 30;

			return domain;
		}).setEntityConverter(context -> {
			Thread.sleep(2000);
			EntityMock entity = new EntityMock();
			entity.firstname = "eric";
			entity.lastname = "eric";
			entity.birthday = date.get();
			return entity;
		});

		EntityMock entity1 = new EntityMock();
		entity1.firstname = "eric";
		entity1.lastname = "pierre";


		ObjectWrapper<EntityMock, DomainMock> wrapper = wrapperBuilder.build();

		CompletableFuture<DomainMock> futureDomain = wrapper.toDataAsync(entity1);
		DomainMock domain = futureDomain.get();

		assertThat(domain).satisfies(this::isDomain);

		CompletableFuture<EntityMock> futureEntity = wrapper.toEntityAsync(domain);
		EntityMock entity = futureEntity.get();

		assertThat(entity).satisfies(this::isEntity);
	}

	private void isDomain(DomainMock domain) {
		assertThat(domain.firstname).isEqualTo("jean");
		assertThat(domain.lastname).isEqualTo("jean");
		assertThat(domain.age).isEqualTo(30);
	}

	private void isEntity(EntityMock entity) {
		assertThat(entity.firstname).isEqualTo("eric");
		assertThat(entity.lastname).isEqualTo("eric");
	}


}
