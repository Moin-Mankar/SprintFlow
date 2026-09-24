package com.sprintflow.backend.security;

import com.sprintflow.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;



import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Characterises the container session that OAuth2Login leaves behind next to the JWT.
 */
class OAuth2SuccessHandlerSessionTest {

    private static final String SUBJECT = "108526727629538249134";
    private static final String EMAIL = "moinmankar3@gmail.com";

    private OAuth2SuccessHandler handler() {
        UserRepository userRepository = mock(UserRepository.class);
        com.sprintflow.backend.entity.User user = new com.sprintflow.backend.entity.User();
        user.setEmail(EMAIL);
        user.setName("Moin Mankar");
        user.setPassword("encoded");
        user.setEnabled(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        JwtService jwtService = mock(JwtService.class);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        return new OAuth2SuccessHandler(userRepository, mock(PasswordEncoder.class), jwtService);
    }

    private Authentication oauthAuthentication() {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("email")).thenReturn(EMAIL);
        when(principal.getAttribute("name")).thenReturn("Moin Mankar");
        AbstractAuthenticationToken authentication = mock(AbstractAuthenticationToken.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getName()).thenReturn(SUBJECT);
        
        return authentication;
    }

    /** The session that exists at the moment the success handler runs. */
    private MockHttpServletRequest requestWithSavedOAuthContext(SecurityContextRepository repository) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        repository.saveContext(
                new org.springframework.security.core.context.SecurityContextImpl(oauthAuthentication()),
                request,
                new MockHttpServletResponse());
        return request;
    }

    @Test
    void handlerDoesNotLeaveAnAuthenticatedOAuthSession() throws Exception {
        SecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        MockHttpServletRequest request = requestWithSavedOAuthContext(repository);

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler().onAuthenticationSuccess(request, response, oauthAuthentication());

        HttpSession session = request.getSession(false);
        assertThat(session).isNull();
        assertThat(response.getCookie(SESSION_COOKIE)).isNull();
    }

    @Test
    void outerContextHolderFilterDoesNotResurrectTheSession() throws Exception {
        runOuterFilter(new SecurityContextHolderFilter(
                new DelegatingSecurityContextRepository(
                        new RequestAttributeSecurityContextRepository(),
                        new HttpSessionSecurityContextRepository())));
    }

    @Test
    void outerPersistenceFilterDoesNotResurrectTheSession() throws Exception {
        runOuterFilter(new SecurityContextPersistenceFilter(new HttpSessionSecurityContextRepository()));
    }

    /**
     * Reproduces the callback request: the session already holds the OAuth SecurityContext, the
     * success handler runs inside the chain, and the outer context filter saves afterwards.
     */
    private void runOuterFilter(jakarta.servlet.Filter outer) throws Exception {
        MockHttpServletRequest request =
                requestWithSavedOAuthContext(new HttpSessionSecurityContextRepository());
        MockHttpServletResponse response = new MockHttpServletResponse();

        jakarta.servlet.FilterChain endOfChain = new jakarta.servlet.FilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                // end of chain
            }
        };
        jakarta.servlet.FilterChain runHandler = new jakarta.servlet.FilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                handler().onAuthenticationSuccess(
                        (HttpServletRequest) req, (HttpServletResponse) res, oauthAuthentication());
                endOfChain.doFilter(req, res);
            }
        };

        outer.doFilter(request, response, runHandler);

        assertThat(request.getSession(false))
                .as("%s must not recreate the session the handler invalidated",
                        outer.getClass().getSimpleName())
                .isNull();
        assertThat(response.getCookie(SESSION_COOKIE))
                .as("no JSESSIONID should be handed to the browser after the JWT is issued")
                .isNull();
        assertThat(response.getContentAsString()).contains("jwt-token");
    }

    /** Cookie name used by the servlet container for the session. */
    private static final String SESSION_COOKIE = "JSESSIONID";

    @Test
    void sessionPrincipalNameIsNotAnEmail() {
        assertThat(oauthAuthentication().getName()).isEqualTo(SUBJECT).isNotEqualTo(EMAIL);
    }

    /**
     * JwtAuthenticationFilter only claims a request when nothing has authenticated it yet, so a
     * session context outranks a valid bearer JWT. Once the handler drops the session the filter
     * sees the bearer alone and the email principal wins.
     */
    @Test
    void sessionContextOutranksBearerJwtButIsGoneOnceHandlerRuns() throws Exception {
        JwtService jwtService = new JwtService();
        java.lang.reflect.Field secret = JwtService.class.getDeclaredField("secretKey");
        secret.setAccessible(true);
        secret.set(jwtService, java.util.Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef0123456789abcdef".getBytes()));

        UserRepository userRepository = mock(UserRepository.class);
        com.sprintflow.backend.entity.User stored = new com.sprintflow.backend.entity.User();
        stored.setEmail(EMAIL);
        stored.setName("Moin Mankar");
        stored.setPassword("encoded");
        stored.setEnabled(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(stored));
        com.sprintflow.backend.service.CustomUserDetailsService uds =
                new com.sprintflow.backend.service.CustomUserDetailsService(userRepository);

        String jwt = jwtService.generateToken(uds.loadUserByUsername(EMAIL));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, uds);

        // A session context is what SecurityContextHolderFilter installs from JSESSIONID.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(SUBJECT, null, List.of()));
        MockHttpServletRequest withSession = new MockHttpServletRequest("GET", "/api/workspaces");
        withSession.addHeader("Authorization", "Bearer " + jwt);
        filter.doFilter(withSession, new MockHttpServletResponse(),
                new org.springframework.mock.web.MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .as("session principal wins, so findByEmail(<subject>) 404s")
                .isEqualTo(SUBJECT);

        SecurityContextHolder.clearContext();
        MockHttpServletRequest jwtOnly = new MockHttpServletRequest("GET", "/api/workspaces");
        jwtOnly.addHeader("Authorization", "Bearer " + jwt);
        filter.doFilter(jwtOnly, new MockHttpServletResponse(),
                new org.springframework.mock.web.MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .as("with no OAuth session left behind the bearer token authenticates as the email")
                .isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(EMAIL);
        SecurityContextHolder.clearContext();
    }
}
