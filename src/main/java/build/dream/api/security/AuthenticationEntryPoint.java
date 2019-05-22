package build.dream.api.security;

import build.dream.api.constants.Constants;
import build.dream.common.api.ApiRest;
import build.dream.common.exceptions.Error;
import build.dream.common.utils.ConfigurationUtils;
import build.dream.common.utils.GsonUtils;
import build.dream.common.utils.ObjectUtils;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.DefaultThrowableAnalyzer;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {
    private static final String PLATFORM_PRIVATE_KEY = ConfigurationUtils.getConfiguration(Constants.PLATFORM_PRIVATE_KEY);
    private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String code = translateException(authException);
        ApiRest apiRest = ApiRest.builder()
                .error(new Error(code, authException.getMessage()))
                .successful(false)
                .build();
        apiRest.sign(PLATFORM_PRIVATE_KEY);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(GsonUtils.toJson(apiRest));
    }

    public String translateException(AuthenticationException authException) {
        Throwable[] causeChain = throwableAnalyzer.determineCauseChain(authException);
        OAuth2Exception oAuth2Exception = (OAuth2Exception) throwableAnalyzer.getFirstThrowableOfType(OAuth2Exception.class, causeChain);
        if (ObjectUtils.isNotNull(oAuth2Exception)) {
            return oAuth2Exception.getOAuth2ErrorCode();
        }

        AuthenticationException authenticationException = (AuthenticationException) throwableAnalyzer.getFirstThrowableOfType(AuthenticationException.class, causeChain);
        if (ObjectUtils.isNotNull(authenticationException)) {
            return "unauthorized";
        }

        AccessDeniedException accessDeniedException = (AccessDeniedException) throwableAnalyzer.getFirstThrowableOfType(AccessDeniedException.class, causeChain);
        if (ObjectUtils.isNotNull(accessDeniedException)) {
            return "access_denied";
        }

        HttpRequestMethodNotSupportedException requestMethodNotSupportedException = (HttpRequestMethodNotSupportedException) throwableAnalyzer.getFirstThrowableOfType(HttpRequestMethodNotSupportedException.class, causeChain);
        if (ObjectUtils.isNotNull(requestMethodNotSupportedException)) {
            return "method_not_allowed";
        }
        return "server_error";
    }
}
