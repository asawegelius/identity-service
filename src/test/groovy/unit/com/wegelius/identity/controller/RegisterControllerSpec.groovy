package unit.com.wegelius.identity.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.wegelius.identity.IdentityServiceApplication
import com.wegelius.identity.config.SecurityConfig
import com.wegelius.identity.controller.GlobalExceptionHandler
import com.wegelius.identity.controller.RegisterController
import com.wegelius.identity.exception.EmailAlreadyExistsException
import com.wegelius.identity.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.doThrow
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = RegisterController)
@ContextConfiguration(classes = IdentityServiceApplication)
@Import([GlobalExceptionHandler, SecurityConfig])
class RegisterControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @MockBean
    UserService userService

    def "register returns no content for valid request"() {
        given:
        def payload = [
                email      : "valid@example.com",
                password   : "secret123",
                displayName: "Valid User"
        ]
        doNothing().when(userService).registerUser(any())

        expect:
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNoContent())
    }

    def "register returns validation errors for invalid request"() {
        given:
        def payload = [
                email      : "not-an-email",
                password   : "short",
                displayName: "Invalid User"
        ]

        expect:
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.message').value('Validation failed'))
                .andExpect(jsonPath('$.errors.email').exists())
                .andExpect(jsonPath('$.errors.password').exists())
    }

    def "register returns conflict for duplicate email"() {
        given:
        def payload = [
                email      : "duplicate@example.com",
                password   : "secret123",
                displayName: "Duplicate User"
        ]
        doThrow(new EmailAlreadyExistsException("duplicate@example.com")).when(userService).registerUser(any())

        expect:
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath('$.message').value('Email already registered: duplicate@example.com'))
                .andExpect(jsonPath('$.errors').isMap())
    }
}
