package build.dream.api.configurations;

import build.dream.api.constants.Constants;
import build.dream.api.matchers.MethodRequestMatcher;
import build.dream.api.security.AccessDeniedHandler;
import build.dream.api.security.ApiFilterInvocationSecurityMetadataSource;
import build.dream.api.services.PrivilegeService;
import build.dream.common.domains.saas.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.AuthenticationEntryPoint;
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
    @Value(value = "${service.name}")
    private String serviceName;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private AccessDeniedHandler accessDeniedHandler;
    @Autowired
    private PrivilegeService privilegeService;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId("api");
        resources.tokenStore(tokenStore());
        resources.authenticationEntryPoint(authenticationEntryPoint);
        resources.accessDeniedHandler(accessDeniedHandler);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/api/**", "/data/**")
                .and()
                .authorizeRequests()
                .antMatchers("/api/**", "/data/**").authenticated();
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

    private LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> buildPosPrivilegeMap() {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> posPrivilegeMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        List<PosPrivilege> posPrivileges = privilegeService.obtainAllPosPrivileges();
        for (PosPrivilege posPrivilege : posPrivileges) {
            String method = posPrivilege.getServiceName() + "." + posPrivilege.getControllerName() + "." + posPrivilege.getActionName();
            posPrivilegeMap.put(buildMethodRequestMatcher(method), buildHasAuthorityConfigAttributes(method));
        }
        return posPrivilegeMap;
    }

    private LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> buildAppPrivilegeMap() {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> appPrivilegeMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        List<AppPrivilege> appPrivileges = privilegeService.obtainAllAppPrivileges();
        for (AppPrivilege appPrivilege : appPrivileges) {
            String method = appPrivilege.getServiceName() + "." + appPrivilege.getControllerName() + "." + appPrivilege.getActionName();
            appPrivilegeMap.put(buildMethodRequestMatcher(method), buildHasAuthorityConfigAttributes(method));
        }
        return appPrivilegeMap;
    }

    private LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> buildBackgroundPrivilegeMap() {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> appPrivilegeMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        List<BackgroundPrivilege> backgroundPrivileges = privilegeService.obtainAllBackgroundPrivileges();
        for (BackgroundPrivilege backgroundPrivilege : backgroundPrivileges) {
            String method = backgroundPrivilege.getServiceName() + "." + backgroundPrivilege.getControllerName() + "." + backgroundPrivilege.getActionName();
            appPrivilegeMap.put(buildMethodRequestMatcher(method), buildHasAuthorityConfigAttributes(method));
        }
        return appPrivilegeMap;
    }

    private LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> buildDevOpsPrivilegeMap() {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> devOpsPrivilegeMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        List<DevOpsPrivilege> devOpsPrivileges = privilegeService.obtainDevOpsPrivileges();
        for (DevOpsPrivilege devOpsPrivilege : devOpsPrivileges) {
            String method = devOpsPrivilege.getServiceName() + "." + devOpsPrivilege.getControllerName() + "." + devOpsPrivilege.getActionName();
            devOpsPrivilegeMap.put(buildMethodRequestMatcher(method), buildHasAuthorityConfigAttributes(method));
        }
        return devOpsPrivilegeMap;
    }

    private LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> buildOpPrivilegeMap() {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> opPrivilegeMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        List<OpPrivilege> opPrivileges = privilegeService.obtainAllOpPrivileges();
        for (OpPrivilege opPrivilege : opPrivileges) {
            String method = opPrivilege.getServiceName() + "." + opPrivilege.getControllerName() + "." + opPrivilege.getActionName();
            opPrivilegeMap.put(buildMethodRequestMatcher(method), buildHasAuthorityConfigAttributes(method));
        }
        return opPrivilegeMap;
    }

    private LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> buildPrivilegeMap() {
        if (Constants.SERVICE_NAME_POSAPI.equals(serviceName)) {
            return buildPosPrivilegeMap();
        }

        if (Constants.SERVICE_NAME_APPAPI.equals(serviceName)) {
            return buildAppPrivilegeMap();
        }

        if (Constants.SERVICE_NAME_WEBAPI.equals(serviceName)) {
            return buildBackgroundPrivilegeMap();
        }

        if (Constants.SERVICE_NAME_O2OAPI.equals(serviceName)) {
            return new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        }

        if (Constants.SERVICE_NAME_AGENT_API.equals(serviceName)) {
            return new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        }

        if (Constants.SERVICE_NAME_OP_API.equals(serviceName)) {
            return buildOpPrivilegeMap();
        }

        if (Constants.SERVICE_NAME_DEV_OPS_API.equals(serviceName)) {
            return buildDevOpsPrivilegeMap();
        }

        return null;
    }

    @Bean
    public ApiFilterInvocationSecurityMetadataSource webFilterInvocationSecurityMetadataSource() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        requestMap.putAll(buildPrivilegeMap());
        requestMap.put(AnyRequestMatcher.INSTANCE, buildAuthenticatedConfigAttributes());
        return new ApiFilterInvocationSecurityMetadataSource(requestMap);
    }
}
