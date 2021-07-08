package com.beer.stock.local;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.beer.stock.local.mapper.BeerMapper;

@SpringBootApplication(scanBasePackages = "com.beer.stock.local")
public class BeerstocklocalApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeerstocklocalApplication.class, args);
	}

	@Bean
	public BeerMapper beerMapper() {
		return BeerMapper.INSTANCE;
	}

}
