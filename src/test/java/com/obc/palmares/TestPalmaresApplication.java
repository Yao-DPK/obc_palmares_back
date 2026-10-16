package com.obc.palmares;

import org.springframework.boot.SpringApplication;

public class TestPalmaresApplication {

	public static void main(String[] args) {
		SpringApplication.from(PalmaresApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
