# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.4/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.0.4/maven-plugin/reference/html/#build-image)
* [Eureka Discovery Client](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/#service-discovery-eureka-clients)
* [Validation](https://docs.spring.io/spring-boot/docs/3.0.4/reference/htmlsingle/#io.validation)
* [OpenFeign](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.0.4/reference/htmlsingle/#web)
* [Cloud Bootstrap](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/)

### Guides

The following guides illustrate how to use some features concretely:

* [Service Registration and Discovery with Eureka and Spring Cloud](https://spring.io/guides/gs/service-registration-and-discovery/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Additional Links

These additional references should also help you:

* [Declarative REST calls with Spring Cloud OpenFeign sample](https://github.com/spring-cloud-samples/feign-eureka)


https://springbootlearning.medium.com/using-micrometer-to-trace-your-spring-boot-app-1fe6ff9982ae

### BD et produits à lancer

Commande pour la BD MySQL :

docker run --name monsql_bq -p 3306:3306  -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=test -d mysql:oracle


Commande pour la BD Mongo :

docker run --name mongo_bq -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=root -e MONGO_INITDB_DATABASE=banque_spring -d mongo:latest


Commande pour zipkin :

docker run -d -p 9411:9411 --name zipkin openzipkin/zipkin


Commande pour Prometheus :

docker run --name my-prometheus \
--mount type=bind,source=/config-prometheus/prometheus.yml,destination=/etc/prometheus/prometheus.yml \
-p 9090:9090 \
prom/prometheus

### Sécurisation avec Okta

On va utiliser le produit Auth0 de la société Okta.
C'est un outil qui permet de faire de l'IAM (Identity and Access Management) dans le Cloud.
En gros leur plateforme gère l'essentiel : authentification, identification, autorisation, droits d'accès, utilisateur, rôles...

Première étape : créer un compte développeur gratuit sur https://developer.okta.com/signup/.

Attention : bien choisir "Customer Identity Cloud" qui va vous amener sur l'outil Auth0.

Une fois le compte créé (vous pouvez utiliser votre compte GitHub, Google ou Microsoft) il faut créer une application.
On va choisir une application SPA (Single Page Application).

Une fois l'application créée on vous propose de récupérer un template d'application front écrite avec Angular (ou autre techno).

Choisissons de l'Angular.

Vu que nous n'avons pas encore d'application front on va choisir "I want to explore a sample app".

Attention : il s'agit du deuxième bouton bleu (celui qui vous donne une application pré-configurée).

Comme l'indique la page de téléchargement, il va falloir ajouter l'URL de développement classique de ng serve http://localhost:4200 pour qu'elle puisse :
* Servir d'URL Callback (sur laquelle on va pouvoir re-diriger un utilisateur une fois identifié) ;
* Servir d'URL Logout (sur laquelle on va pouvoir re-diriger un utilisateur une fois déconnecté) ;
* Être référencée comme Origine Web (pour gérer l'authentification multi sites sans qu'on puisse récupérer les données d'authentification sur n'importe quel site)

On fait tout ça dans la page Settings de l'application.

Dans la page Connections, on peut activer ou désactiver les connections OpenID avec GitHub, Google...

Une fois récupéré le projet Web vous y allez trouver un fichier auth_config.json qui est ignoré par GitHub (vu qu'il va contenir des infos confidentielles).
Voilà ce qu'il contient par défaut :

```
{
"domain": "VOTRE DOMAINE.us.auth0.com",
"clientId": "VOTRE ID CLIENT",
"authorizationParams": {
"audience": "{yourApiIdentifier}"
},
"apiUri": "http://localhost:3001",
"appUri": "http://localhost:4200",
"errorPath": "/error"
}
```

Les infos domain et clientId ont été initialisées automatiquement et correspondent à ce qui est affiché sur la page Settings sur le site Auth0.

Il va falloir maintenant modifier audience et apiUri pour y indiquer les infos de votre application Spring.

Attention : ne pas oublier de modifier apiUri !!!

On va indiquer, pour les deux, les infos d'amc_proxy (mon projet correspondant à la Gateway) : http://localhost:10000

Note : pour tester, vous pouvez aussi sécuriser amc_clients (service clients), amc_comptes (service comptes)... mais normalement tout doit passer par la Gateway.

Attention : il ne faut surtout pas inclure le / à la fin de l'URL.

Donc votre fichier doit se finir comme suit :

```
...
"authorizationParams": {
"audience": "http://localhost:10000"
},
"apiUri": "http://localhost:10000",
"appUri": "http://localhost:4200",
"errorPath": "/error"
}
```

Une fois ces modifications faites, on va déclarer l'API sur le serveur Auth0 :
* Sur la gauche vous allez trouver un menu Applications avec un sous-menu API ;
* Cliquez sur API puis "Create API" ;
* Indiquez le nom que vous voulez puis http://localhost:10000
* Laissez le reste tel quel

Ensuite nous allons configurer cette API 

On peut ensuite lancer l'application une première fois en faisant le classique :

```
npm install && npm start

```

Modifications Gateway :

Ajouts de dépendances pour transformer la Gateway en serveur de ressources au sens Oauth et ajouter les classes de config pour Okta :

```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>

        <dependency>
            <groupId>com.okta.spring</groupId>
            <artifactId>okta-spring-boot-starter</artifactId>
            <version>3.0.6</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
```

Ajout de la sécurité sur l'application :
* Ajout de @EnableWebFluxSecurity pour activer la sécurité
* Ajout d'un Bean de type SecurityWebFilterChain pour configurer la sécurité

```

@SpringBootApplication
@EnableDiscoveryClient
@EnableWebFluxSecurity
public class AmcProxyApplication {

    @Bean
    public GlobalFilter customFilter() {
        return new PreFilter();
    }



    public static void main(String[] args) {
        SpringApplication.run(AmcProxyApplication.class, args);
    }
    

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }


}

```

Modification dans application.yml :
* Ajout de CORS (ne marche pas pour le moment - WIP)
* Ajout des URLs qui fournissent les jetons JWT :
  security: oauth2: resourceserver: jwt:
  issuer-uri: https://dev-nj3gclnzfe2tmzvt.us.auth0.com/
  jwk-set-uri: https://dev-nj3gclnzfe2tmzvt.us.auth0.com/.well-known/jwks.json
* Ajout des URLs pour Okta (fournisseur authentification et URL de l'appli elle même pour comparaison dans les jetons) :
  okta: oauth2:
  issuer: https://dev-nj3gclnzfe2tmzvt.us.auth0.com/
  audience: http://localhost:10000



```
# Proprietes de l'application
spring:
  application:
    name: apigateway                                   # nom de l'application
  cloud:
    # Configuration de l'API Gateway
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:4200"
            allowedMethods:
              - GET
              - POST
              - DELETE
              - PUT
              - PATCH
              - OPTIONS
        add-to-simple-url-handler-mapping: true
      discovery:
        locator:
          enabled: true #activation eureka locator
          lowerCaseServiceId: true
          # car le nom des services est en minuscule dans l'URL
      # Configuration des routes de l'API Gateway
      routes:
        #Service CLIENTS-SERVICE
        - id: client-service
          uri: lb://amcclients/ #Attention : lb et pas HTTP. Lb est prêt pour faire du load-balancing
          predicates:
            # On matche tout ce qui commence par /api/clients
            - Path=/api/clients/**
          filters:
            # On va réécrire l'URL pour enlever le /api/client
            - RewritePath=/api/clients(?<segment>/?.*), /$\{segment}
          metadata:
            cors:
              allowedOrigins: '*'
              allowedMethods:
                - GET
                - POST
              allowedHeaders: '*'
              maxAge: 30
        #Service COMPTES-SERVICE
        - id: comptes-service
          uri: lb://amccomptes/
          predicates:
            - Path=/api/comptes/**
          filters:
            - RewritePath=/api/comptes(?<segment>/?.*), /$\{segment}
        #Service CLIENTS-COMPTES
        - id: clients-comptes
          uri: lb://amccomposite/
          predicates:
            - Path=/api/clientscomptes/**
          filters:
            - RewritePath=/api/clientscomptes(?<segment>/?.*), /$\{segment}
      enabled: on # Activation gateway
    # Activation remontée management dans Eureka
    config:
      service-registry:
        auto-registration:
          register-management: on
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://dev-nj3gclnzfe2tmzvt.us.auth0.com/
          jwk-set-uri: https://dev-nj3gclnzfe2tmzvt.us.auth0.com/.well-known/jwks.json

# Activation des endpoints pour le monitoring
management:
  endpoints:
    web:
      exposure:
        include:
          env,health,
          info,metrics,
          loggers,mappings, prometheus
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://banque-zipkin:9411/api/v2/spans
# Configuration client de l'annuaire
# L'API Gateway va s'enregistrer comme un micro-service sur l'annuaire
eureka:
  client:
    serviceUrl:
      defaultZone: http://banque-annuaire:10001/eureka/ # url d'accès à l'annuaire
  instance:
    metadata-map:
      prometheus.scrape: "true"
      prometheus.path: "/actuator/prometheus"
      prometheus.port: "${management.server.port}"
      #    instance:
      #      metadataMap:
      # on va surcharger le nom de l'application si plusieurs instances de l'API Gateway ont même IP et même port
      # on surcharge par une valeur random si le nom de l'instance existe déjà.
#        instanceId: ${spring.application.name}:${spring.application.instance_id:${random.value}}


# Configuration du log.
logging:
  level:
    org.springframework.web: INFO # Choix du niveau de log affiché
#    org.springframework.cloud.gateway: DEBUG # pour avoir plus d'infos sur le gateway
#    reactor.netty.http.client: DEBUG # pour avoir plus d'infos sur les appels HTTP

# Proprietes du serveur d'entreprise
server:
  port: 10000   # HTTP (Tomcat) port

okta:
  oauth2:
    issuer: https://dev-nj3gclnzfe2tmzvt.us.auth0.com/
    audience: http://localhost:10000



```

Fonctionnement :
* S'authentifier avec l'appli Angular
* Avec les outils de dev du navigateur récupérer le jeton : Authorisation: Bearer XXXXXX
* Copier le jeton d'authentification dans le plugin du navigateur et l'utiliser pour vos requêtes

A suivre : 
* modifier l'application Angular pour utiliser l'API Gateway et résoudre le problème de CORS
* faire héberger l'application Angular par docker pour qu'elle se trouve derrière la Gateway (plus de problème de CORS)