package cn.zzzyuan.config;



import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * @author codesuperman@foxmail.com
 * @date 2021-11-12
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientPassWd}")
    private String clientPassWd;

    @Autowired
    private  AuthenticationManager authenticationManager;

    @Autowired
    private  AuthorizationCodeServices authorizationCodeServices;

    @Autowired
    private  TokenStore tokenStore;

    @Autowired
    private ClientDetailsService clientDetailsService;


    /**
     * ????????????
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {

        security
                // /oauth/token_key??????
                .tokenKeyAccess("permitAll()")
                // /oauth/check_token ??????
                .checkTokenAccess("permitAll()")
                // ???????????????????????????
                .allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        InMemoryClientDetailsServiceBuilder inMemoryClientDetailsServiceBuilder = clients.inMemory();

        if(StringUtils.isNotEmpty(clientId)){
                inMemoryClientDetailsServiceBuilder.withClient(clientId)
                        .secret(new BCryptPasswordEncoder().encode(clientPassWd))
                        // ?????????????????????id
                        .resourceIds("newblog-blog","newblog-forum","newblog-user")
                        // ????????????
                        .authorizedGrantTypes("authorization_code","password","refresh_token")
                        // ???????????? ?????????rw
                        .scopes("all")
                        .autoApprove(false)
                        .redirectUris("http://www.baidu.com");
        }

    }

    /**
     * ????????????
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                // ????????????????????????
                .authenticationManager(authenticationManager)
                // ???????????????????????????
                .authorizationCodeServices(authorizationCodeServices)
                // ??????????????????
                .tokenServices(tokenServices())
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);
    }

    /**
     * ????????????
     *
     * @return
     */
    @Bean
    public AuthorizationServerTokenServices tokenServices(){
        DefaultTokenServices services = new DefaultTokenServices();
        services.setClientDetailsService(clientDetailsService);
        // ???????????????????????????
        services.setRefreshTokenValiditySeconds(60*60*24*3);
        // ?????????????????????
        services.setAccessTokenValiditySeconds(60*60*12);
        // ??????????????????
        services.setSupportRefreshToken(true);
        services.setTokenStore(tokenStore);
        return services;
    }

    @Bean
    public AuthorizationCodeServices authorizationCodeServices() { //??????????????????????????????????????? ?????????????????????????????????
        return new InMemoryAuthorizationCodeServices();
    }


}
