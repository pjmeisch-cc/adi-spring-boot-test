package com.adidas.hello;

import com.adidas.hello.security.WithMockOAuth2User;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class OAuth2LoginTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test(expected = UserRedirectRequiredException.class)
    public void shouldRedirectWhenUserIsNotLoggedIn() throws Exception {
        mockMvc.perform(get("/login/adidas-dev", String.class)).andReturn();
    }

    @Test
    @WithMockOAuth2User
    public void shouldRedirectToSpecifiedOAuthProvider() throws Exception {
        try {
            mockMvc.perform(get("/login/adidas-dev", String.class)).andReturn();
            fail("should raise a UserRedirectRequiredException");
        } catch (UserRedirectRequiredException redirectRequired) {
            assertEquals("https://dev.pf.adidas.com/as/authorization.oauth2?access_token_manager_id=jwt", redirectRequired.getRedirectUri());
        }
    }

}
