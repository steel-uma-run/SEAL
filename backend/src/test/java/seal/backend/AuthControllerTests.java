package seal.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seal.backend.config.GlobalConfig;
import seal.openapi.model.LoginRequestPayloadDto;
import seal.openapi.model.RegisterRequestPayloadDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class AuthControllerTests {
  @Autowired private MockMvc mvc;
  private final ObjectMapper objMapper = new ObjectMapper();

  @Test
  @Order(1)
  void registerTest() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post(GlobalConfig.API_BASE + "/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objMapper.writeValueAsString(
                        new RegisterRequestPayloadDto(
                            "nhc@fpt.edu.vn", "hunter2", "NHC", "SE123456", true, ""))))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  @Order(2)
  void loginTest() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post(GlobalConfig.API_BASE + "/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objMapper.writeValueAsString(
                        new LoginRequestPayloadDto("nhc@fpt.edu.vn", "hunter2"))))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.token", Matchers.isA(String.class)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user.email", Matchers.is("nhc@fpt.edu.vn")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user.name", Matchers.is("NHC")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.isA(String.class)));
  }
}
