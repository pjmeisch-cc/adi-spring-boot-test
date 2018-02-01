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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration
public class LoginTest {

    @Autowired
    private WebApplicationContext context;

    // A mocked MVC server - does not actually receive HTTP messages, handlers are being called directly instead.
    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    // Given a valid login (username/password), the server should return an Authorization header
    // containing a Bearer token.
    public void validLogin_AuthorizationHeaderPresent() throws Exception {
        String requestBody = LoginTestCommon.jsonCredentials(DefaultUser.NAME, DefaultUser.PASSWORD);
        MvcResult result = mvc.perform(post("/jwt/login").content(requestBody))
            .andExpect(status().isOk())
            .andExpect(authenticated())
            .andReturn();
        assertNotNull(result.getResponse().getHeader("Authorization"));
        assertTrue(result.getResponse().getHeader("Authorization").startsWith("Bearer"));
    }

    @Test
    // Given an invalid login (username/NOT password), the server should return 401 (Unauthorized)
    public void invalidLogin_401Unauthorized() throws Exception {
        String requestBody = LoginTestCommon.jsonCredentials(DefaultUser.NAME, "NOT " + DefaultUser.PASSWORD);
        mvc.perform(post("/jwt/login").content(requestBody))
            .andExpect(status().isUnauthorized())
            .andExpect(unauthenticated())
            .andReturn();
    }

}
