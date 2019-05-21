package build.dream.api.processors;

import build.dream.common.utils.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Component;

/**
 * Created by liuyandong on 2017/6/21.
 */
@Component
public class BeanPostProcessor implements org.springframework.beans.factory.config.BeanPostProcessor {
    private FilterSecurityInterceptor filterSecurityInterceptor;
    private FilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource;
    private boolean isSetFinished = false;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FilterSecurityInterceptor) {
            filterSecurityInterceptor = (FilterSecurityInterceptor) bean;
        }

        if (bean instanceof FilterInvocationSecurityMetadataSource) {
            filterInvocationSecurityMetadataSource = (FilterInvocationSecurityMetadataSource) bean;
        }

        if (ObjectUtils.isNotNull(filterSecurityInterceptor) && ObjectUtils.isNotNull(filterInvocationSecurityMetadataSource) && !isSetFinished) {
            filterSecurityInterceptor.setSecurityMetadataSource(filterInvocationSecurityMetadataSource);
            isSetFinished = true;
        }
        return bean;
    }
}
