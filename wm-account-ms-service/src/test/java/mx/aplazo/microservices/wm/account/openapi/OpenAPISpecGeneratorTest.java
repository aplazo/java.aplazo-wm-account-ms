package mx.aplazo.microservices.wm.account.openapi;


import mx.aplazo.microservices.wm.account.helper.AbstractAplazoIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.FileOutputStream;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnabledIfEnvironmentVariable(named = "APLAZO_GENERATE_OPENAPI", matches = "true", disabledReason = "This test is only enabled if the APLAZO_GENERATE_OPENAPI environment variable is set to true")
class OpenAPISpecGeneratorTest extends AbstractAplazoIntegrationTest {
    private static final String API_DOCS_PATH = "/v3/api-docs.yaml";

    @Test
    @DisplayName("Generate YAML file with OpenAPI definitions")
    void test_generateOpenAPIDefinitions() throws Exception {

        final var response = mockMvc.perform(get(API_DOCS_PATH))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResponse());
        final byte[] file = response.getResponse().getContentAsByteArray();
        Assertions.assertNotEquals(0, file.length);
        var openapiSpecPath = Paths.get("../openapi.yml");
        System.out.println(openapiSpecPath.toAbsolutePath());
        try (final FileOutputStream fos = new FileOutputStream(openapiSpecPath.toFile())) {
            fos.write(file);
        }
    }

}
