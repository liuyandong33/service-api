package build.dream.api.security;

import build.dream.common.utils.WebSecurityUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashMap;

public class ApiFilterInvocationSecurityMetadataSource extends DefaultFilterInvocationSecurityMetadataSource {
    public ApiFilterInvocationSecurityMetadataSource(LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        super(WebSecurityUtils.processMap(requestMap, new SpelExpressionParser()));
    }
}
