package fr.miage.amc_clients;


import fr.miage.amc_clients.entities.Client;
import fr.miage.amc_clients.repositories.ClientRepository;
import com.okta.spring.boot.oauth.Okta;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMethodSecurity(prePostEnabled = true)
public class AmcClientsApplication implements CommandLineRunner {

    public AmcClientsApplication(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(AmcClientsApplication.class, args);
    }


    private final ClientRepository clientRepository;

    @Override
    public void run(String... args) throws Exception {
        assert(clientRepository != null);

        if(clientRepository.findByPrenomAndNom("Jean", "Dupond").isEmpty()) {
            Client client = new Client();
            client.setPrenom("Jean");
            client.setNom("Dupond");
            clientRepository.save(client);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    ObservationRegistry observationRegistry() {
        PathMatcher pathMatcher = new AntPathMatcher("/");
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        observationRegistry.observationConfig().observationPredicate((name, context) -> {
            if(context instanceof ServerRequestObservationContext) {
                return !pathMatcher.match("/actuator/**", ((ServerRequestObservationContext) context).getCarrier().getRequestURI());
            } else {
                return true;
            }
        });
        return observationRegistry;
    }

    // ET LA
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));

        // process CORS annotations
        http.cors(Customizer.withDefaults());

        // force a non-empty response body for 401's to make the response more browser friendly
        Okta.configureResourceServer401ResponseBody(http);

        return http.build();
    }
}
