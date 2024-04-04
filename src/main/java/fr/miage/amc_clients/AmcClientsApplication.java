package fr.miage.amc_clients;

import fr.miage.amc_clients.entities.Client;
import fr.miage.amc_clients.repositories.ClientRepository;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

@SpringBootApplication
@EnableDiscoveryClient
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
}
