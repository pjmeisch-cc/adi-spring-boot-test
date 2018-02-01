package com.adidas.hello;

import com.adidas.hello.security.DefaultUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration
public class ProtectedTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    // Obtains a token from /jwt/login, then uses it to access /jwt/protected. The last request should return a 200.
    public void loggedIn_accessProtected() throws Exception {
        String requestBody = LoginTestCommon.jsonCredentials(DefaultUser.NAME, DefaultUser.PASSWORD);
        MvcResult result = mvc.perform(post("/jwt/login").content(requestBody))
            .andExpect(status().isOk())
            .andExpect(authenticated())
            .andReturn();

        mvc.perform(get("/jwt/protected").header("Authorization", result.getResponse().getHeader("Authorization")))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    // Access protected endpoint without a JWT. The request should return a 401
    public void noToken_accessProtected() throws Exception {
        mvc.perform(get("/jwt/protected"))
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    // Access protected endpoint with a JWT signed with the secret 'secret' (not 'my-secret'). The request should return a 401
    public void invalidToken_accessProtected() throws Exception {
        mvc.perform(get("/jwt/protected").header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSJ9.Sl4IHwFfRqjLT5OfECjtUlRPwked2iu-oPqvtnR68LI"))
            .andExpect(status().isForbidden())
            .andReturn();
    }


}
