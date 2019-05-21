package build.dream.api.configurations;

import build.dream.api.constants.Constants;
import build.dream.api.matchers.MethodRequestMatcher;
import build.dream.api.security.ApiFilterInvocationSecurityMetadataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("api");
        resources.tokenStore(tokenStore());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/api/**")
                .and()
                .authorizeRequests()
                .antMatchers("/api/**").authenticated();
    }

    @Bean
    public TokenStore tokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
    }

    private RequestMatcher buildMethodRequestMatcher(String method) {
        return new MethodRequestMatcher(method);
    }

    private List<ConfigAttribute> buildAuthenticatedConfigAttributes() {
        List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>();
        configAttributes.add(new SecurityConfig(Constants.AUTHENTICATED));
        return configAttributes;
    }

    private List<ConfigAttribute> buildHasAuthorityConfigAttributes(String privilegeCode) {
        List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>();
        configAttributes.add(new SecurityConfig(String.format(Constants.HAS_AUTHORITY_FORMAT, privilegeCode)));
        return configAttributes;
    }

    @Bean
    public ApiFilterInvocationSecurityMetadataSource webFilterInvocationSecurityMetadataSource() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        String[] methods = new String[]{"eleme.order.getOrder", "alipay.trade.fastpay.refund.query"};
        for (String method : methods) {
            requestMap.put(buildMethodRequestMatcher(method), buildHasAuthorityConfigAttributes(method));
        }

        requestMap.put(AnyRequestMatcher.INSTANCE, buildAuthenticatedConfigAttributes());
        return new ApiFilterInvocationSecurityMetadataSource(requestMap);
    }
}
