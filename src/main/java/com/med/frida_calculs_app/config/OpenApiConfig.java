package com.med.frida_calculs_app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Frida Calculs - Héritage Islamique")
                        .version(appVersion)
                        .description("""
                            API REST pour le calcul des parts d'héritage selon la loi islamique (Fiqh).

                            ## Fonctionnalités

                            - Calcul automatique des parts selon les règles de succession islamique
                            - Support de compositions familiales complexes
                            - Validation des données d'entrée
                            - Réponses détaillées avec fractions simplifiées
                            - Gestion d'erreurs standardisée

                            ## Règles de calcul

                            Les calculs sont basés sur les versets du Coran concernant l'héritage:
                            - Sourate An-Nisa (4), versets 11-12 et 176
                            - Les parts sont calculées selon le sexe du défunt
                            - Prise en compte de la présence du conjoint, des parents, des enfants et de la fratrie
                            - Les parts sont réduites au même dénominateur pour faciliter le partage

                            ## Utilisation

                            1. Envoyez une requête POST à `/api/v1/heritage/calculate`
                            2. Fournissez la composition de la famille dans le corps de la requête
                            3. Recevez les parts calculées pour chaque héritier
                            """)
                        .contact(new Contact()
                                .name("Équipe Frida")
                                .email("contact@frida-heritage.com")
                                .url("https://frida-heritage.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Serveur de développement local"),
                        new Server()
                                .url("https://api.frida-heritage.com")
                                .description("Serveur de production")
                ));
    }
}
