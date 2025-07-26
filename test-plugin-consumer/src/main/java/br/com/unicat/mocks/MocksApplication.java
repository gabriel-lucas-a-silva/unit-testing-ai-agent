package br.com.unicat.mocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MocksApplication {

	public static void main(String[] args) {
		SpringApplication.run(MocksApplication.class, args);
	}

}
