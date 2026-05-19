package com.med.frida_calculs_app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.med.frida_calculs_app.model.FamilyRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests d'intégration du controller Heritage")
class CalculsPartsControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("GET /status devrait retourner 200 OK")
        void testStatusEndpoint() throws Exception {
                mockMvc.perform(get("/api/v1/heritage/status"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Opérationnelle")));
        }

        @Test
        @DisplayName("POST /calculate avec données valides devrait retourner 200")
        void testCalculateWithValidData() throws Exception {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(1)
                                .pereVivant(false)
                                .mereVivante(false)
                                .nbFilles(1)
                                .nbGarcons(1)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/heritage/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.calculId").exists())
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.heritiers").isArray())
                                .andExpect(jsonPath("$.heritiers", hasSize(greaterThan(0))))
                                .andExpect(jsonPath("$.nombreHeritiers").isNumber())
                                .andExpect(jsonPath("$.denominateurCommun").isNumber())
                                .andExpect(jsonPath("$.message")
                                                .value("Calcul des parts d'héritage effectué avec succès"))
                                .andExpect(jsonPath("$.composition.sexeDefunt").value("M"))
                                .andExpect(jsonPath("$.composition.nbConjoints").value(1));
        }

        @Test
        @DisplayName("POST /calculate avec sexe invalide devrait retourner 400")
        void testCalculateWithInvalidSex() throws Exception {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("X") // Invalide
                                .nbConjoints(1)
                                .nbFilles(1)
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/heritage/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("POST /calculate avec nombre négatif devrait retourner 400")
        void testCalculateWithNegativeNumber() throws Exception {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(1)
                                .nbFilles(-1) // Invalide
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/heritage/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.validationErrors").isArray())
                                .andExpect(jsonPath("$.validationErrors[0].field").value("nbFilles"))
                                .andExpect(jsonPath("$.validationErrors[0].message")
                                                .value("Le nombre de filles ne peut pas être négatif"));
        }

        @Test
        @DisplayName("POST /calculate sans sexe devrait retourner 400")
        void testCalculateWithoutSex() throws Exception {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .nbConjoints(1)
                                .nbFilles(1)
                                // sexeDefunt manquant
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/heritage/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.validationErrors").isArray());
        }

        @Test
        @DisplayName("POST /calculate avec aucun héritier devrait retourner 400")
        void testCalculateWithNoHeirs() throws Exception {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(0)
                                .pereVivant(false)
                                .mereVivante(false)
                                .nbFilles(0)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/heritage/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message")
                                                .value("Aucun héritier n'a été spécifié. Au moins un héritier doit être présent."));
        }

        @Test
        @DisplayName("POST /calculate avec défunte féminine et parents")
        void testCalculateWithFemaleDeceasedAndParents() throws Exception {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("F")
                                .nbConjoints(0)
                                .pereVivant(true)
                                .mereVivante(true)
                                .nbFilles(0)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/heritage/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.heritiers").isArray())
                                .andExpect(jsonPath("$.composition.sexeDefunt").value("F"))
                                .andExpect(jsonPath("$.composition.pereVivant").value(true))
                                .andExpect(jsonPath("$.composition.mereVivante").value(true));
        }

        @Test
        @DisplayName("POST /calculate devrait retourner le même dénominateur pour toutes les parts")
        void testCalculateReturnsCommonDenominator() throws Exception {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(1)
                                .pereVivant(true)
                                .mereVivante(true)
                                .nbFilles(2)
                                .nbGarcons(1)
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/heritage/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.denominateurCommun").isNumber())
                                .andExpect(jsonPath("$.heritiers[0].part.denominateur").isNumber())
                                .andExpect(jsonPath("$.heritiers[1].part.denominateur").isNumber());
        }
}
