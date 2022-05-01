package me.itzg.cahweb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.itzg.cahweb.model.BlackCard;
import me.itzg.cahweb.model.CardsSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@SpringBootTest
class CahWebApplicationTests {

    @Test
    void contextLoads() {
    }

}
