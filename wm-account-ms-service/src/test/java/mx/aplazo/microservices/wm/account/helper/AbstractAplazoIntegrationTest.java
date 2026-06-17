package mx.aplazo.microservices.wm.account.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;



@Testcontainers
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class AbstractAplazoIntegrationTest {


    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    public TestRestTemplate restTemplate;

    public static PodamFactory podamFactory=new PodamFactoryImpl();

    public static ObjectMapper objectMapper = new ObjectMapper();




    @BeforeAll
    public static void setUp(){
        objectMapper.registerModule(new JavaTimeModule());
        podamFactory.getStrategy().setDefaultNumberOfCollectionElements(5);
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {

    }
}
