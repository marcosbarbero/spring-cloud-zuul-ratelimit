package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import org.junit.Ignore;

@Ignore
//@RunWith(SpringRunner.class)
//@TestExecutionListeners(
//        mergeMode = MERGE_WITH_DEFAULTS
//        listeners = {
//                WithTokenSecurityContextTestExecutionListener.class
//        })
public class OAuth2SecureContextRateLimitTypeTest {

//    @Mock
//    private HttpServletRequest httpServletRequest;
//    private Route route = new Route("servicea", "/test", "servicea", "/servicea", null, Collections.emptySet());
//    private RateLimitUtils rateLimitUtils;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        RateLimitProperties properties = new RateLimitProperties();
//        rateLimitUtils = new OAuth2SecuredRateLimitUtils(properties);
//    }
//
//    @Test
//    @WithJwtToken(tokenProducerMethod = "createToken", authenticationName = "myclientid")
//    public void applyRole() {
//        boolean apply = RateLimitType.CLIENTID.apply(httpServletRequest, route, rateLimitUtils, "myclientid");
//        assertThat(apply).isTrue();
//    }
//
//    @Test
//    @WithJwtToken(tokenProducerMethod = "createToken", authenticationName = "anotherClientId")
//    public void doNotApplyRole() {
//        boolean apply = RateLimitType.CLIENTID.apply(httpServletRequest, route, rateLimitUtils, "myclientid");
//        assertThat(apply).isFalse();
//    }
//
//    private static Jwt createToken() {
//        return createToken(null);
//    }
//
//    private static Jwt createTokenWithScopes() {
//        return createToken(Arrays.asList("one", "two"));
//    }
//
//    private static Jwt createToken(@Nullable List<String> scopes) {
//        try {
//            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
//                    .issueTime(new Date())
//                    .expirationTime(Date.from(Instant.now().plusSeconds(120L)))
//                    .issuer("https://github.com/spring-projects/spring-security")
//                    .claim("user", "user")
//                    .subject("user");
//
//            Optional.ofNullable(scopes)
//                    .ifPresent(theScopes -> claimsBuilder.claim("scp", theScopes));
//
//            PlainJWT jwt = new PlainJWT(claimsBuilder.build());
//
//            return new Jwt(
//                    jwt.serialize(),
//                    jwt.getJWTClaimsSet().getIssueTime().toInstant(),
//                    jwt.getJWTClaimsSet().getExpirationTime().toInstant(),
//                    jwt.getHeader().toJSONObject(),
//                    jwt.getJWTClaimsSet().getClaims()
//            );
//        } catch (ParseException ex) {
//            return ExceptionUtils.rethrow(ex);
//        }
//    }

}
