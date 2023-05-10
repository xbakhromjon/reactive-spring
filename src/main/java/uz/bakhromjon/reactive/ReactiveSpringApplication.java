package uz.bakhromjon.reactive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ReactiveSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveSpringApplication.class, args);
    }

}

@Component
@RequiredArgsConstructor
class Initializer implements CommandLineRunner {
    private final DatabaseClient dbc;
    private final CustomerRepository repository;

    @Override
    public void run(String... args) throws Exception {
        Mono<Long> ddl = dbc.sql(
                """
                                drop table if exists customer;
                                create table if not exists customer(id serial primary key, name varchar(255) not null );
                        """).fetch().rowsUpdated();

        Flux<Customer> customers = Flux.just("Dmitry", "Josh", "Olga", "Violetta")
                .map(name -> new Customer(null, name))
                .flatMap(repository::save);

        Flux<Customer> all = repository.findAll();

        ddl.thenMany(customers)
                .thenMany(all)
                .subscribe(System.out::println);
    }
}

@RestController
@RequiredArgsConstructor
class CustomerController {
    private final CustomerRepository repository;

    @GetMapping("/customers")
    public Flux<Customer> getAll() {
        return repository.findAll();
    }
}

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {
    @Id
    private Integer id;
    private String name;
}
