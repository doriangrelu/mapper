package com.test.grelu.mapper.sapstarter;

import com.grelu.mapper.springboot.annotation.EnableMapperContainer;
import com.test.grelu.mapper.sapstarter.mock.ConfigurationTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ConfigurationTest.class)
public class SpringBootApplicationMapperTest {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootApplicationMapperTest.class, args);
	}


}
