package jpabook.jpashop2;

import com.fasterxml.jackson.datatype.hibernate7.Hibernate7Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static com.fasterxml.jackson.datatype.hibernate7.Hibernate7Module.Feature.FORCE_LAZY_LOADING;

@SpringBootApplication
public class Jpashop2Application {

  public static void main(String[] args) {
    SpringApplication.run(Jpashop2Application.class, args);
  }

  @Bean
  public Hibernate7Module hibernate7Module() {
    Hibernate7Module hibernate7Module = new Hibernate7Module();
//    hibernate7Module.configure(FORCE_LAZY_LOADING, false);
    return hibernate7Module;
  }
}
