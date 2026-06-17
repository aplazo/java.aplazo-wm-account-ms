package mx.aplazo.microservices.wm.account.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractAplazoUnitTest {

    public static ObjectMapper objectMapper = new ObjectMapper();
    public static PodamFactory podamFactory=new PodamFactoryImpl();

    @BeforeAll
    static public void setUp() {
        objectMapper.findAndRegisterModules();
        podamFactory.getStrategy().setDefaultNumberOfCollectionElements(2);
    }
}
