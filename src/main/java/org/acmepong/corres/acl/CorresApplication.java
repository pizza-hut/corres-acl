package org.acmepong.corres.acl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan ({"org.acmepong.corres.acl.api"})
public class CorresApplication {

	public static void main(String[] args) {
		SpringApplication.run(CorresApplication.class, args);
	}
}
