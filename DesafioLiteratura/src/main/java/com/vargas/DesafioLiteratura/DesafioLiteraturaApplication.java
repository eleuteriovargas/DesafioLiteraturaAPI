package com.vargas.DesafioLiteratura;

import com.vargas.DesafioLiteratura.principal.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DesafioLiteraturaApplication implements CommandLineRunner {
	private final Principal principal;

	@Autowired
	public DesafioLiteraturaApplication(Principal principal) {
		this.principal = principal;
	}

	public static void main(String[] args) {
		SpringApplication.run(DesafioLiteraturaApplication.class, args);
	}
	@Override
	public void run(String... args) throws Exception {
		principal.muestraElMenu();

	}
}
