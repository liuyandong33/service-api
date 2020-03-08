package build.dream.api.security;

import build.dream.common.api.ApiRest;
import build.dream.common.constants.ConfigurationKeys;
import build.dream.common.constants.ErrorConstants;
import build.dream.common.exceptions.Error;
import build.dream.common.utils.ConfigurationUtils;
import build.dream.common.utils.JacksonUtils;
import build.dream.common.utils.ObjectUtils;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.DefaultThrowableAnalyzer;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {
    private static final String PLATFORM_PRIVATE_KEY = ConfigurationUtils.getConfiguration(ConfigurationKeys.PLATFORM_PRIVATE_KEY);
    private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(buildResult(authException));
    }

    private String buildResult(Exception exception) {
        String errorCode = translateErrorCode(obtainErrorCode(exception));
        Error error = new Error(errorCode, exception.getMessage());
        ApiRest apiRest = ApiRest.builder()
                .error(error)
                .successful(true)
                .build();
        apiRest.sign(PLATFORM_PRIVATE_KEY);
        return JacksonUtils.writeValueAsString(apiRest);
    }

    public String translateErrorCode(String errorCode) {
        switch (errorCode) {
            case ErrorConstants.INVALID_REQUEST:
                return ErrorConstants.ERROR_CODE_INVALID_REQUEST;
            case ErrorConstants.INVALID_CLIENT:
                return ErrorConstants.ERROR_CODE_INVALID_CLIENT;
            case ErrorConstants.INVALID_GRANT:
                return ErrorConstants.ERROR_CODE_INVALID_GRANT;
            case ErrorConstants.UNAUTHORIZED_CLIENT:
                return ErrorConstants.ERROR_CODE_UNAUTHORIZED_CLIENT;
            case ErrorConstants.UNSUPPORTED_GRANT_TYPE:
                return ErrorConstants.ERROR_CODE_UNSUPPORTED_GRANT_TYPE;
            case ErrorConstants.INVALID_SCOPE:
                return ErrorConstants.ERROR_CODE_INVALID_SCOPE;
            case ErrorConstants.INSUFFICIENT_SCOPE:
                return ErrorConstants.ERROR_CODE_INSUFFICIENT_SCOPE;
            case ErrorConstants.INVALID_TOKEN:
                return ErrorConstants.ERROR_CODE_INVALID_TOKEN;
            case ErrorConstants.REDIRECT_URI_MISMATCH:
                return ErrorConstants.ERROR_CODE_REDIRECT_URI_MISMATCH;
            case ErrorConstants.UNSUPPORTED_RESPONSE_TYPE:
                return ErrorConstants.ERROR_CODE_UNSUPPORTED_RESPONSE_TYPE;
            case ErrorConstants.ACCESS_DENIED:
                return ErrorConstants.ERROR_CODE_ACCESS_DENIED;
            case ErrorConstants.UNAUTHORIZED:
                return ErrorConstants.ERROR_CODE_UNAUTHORIZED;
            default:
                return ErrorConstants.ERROR_CODE_UNKNOWN_ERROR;
        }
    }

    public String obtainErrorCode(Exception exception) {
        Throwable[] causeChain = throwableAnalyzer.determineCauseChain(exception);
        OAuth2Exception oAuth2Exception = (OAuth2Exception) throwableAnalyzer.getFirstThrowableOfType(OAuth2Exception.class, causeChain);
        if (ObjectUtils.isNotNull(oAuth2Exception)) {
            return oAuth2Exception.getOAuth2ErrorCode();
        }

        AuthenticationException authenticationException = (AuthenticationException) throwableAnalyzer.getFirstThrowableOfType(AuthenticationException.class, causeChain);
        if (ObjectUtils.isNotNull(authenticationException)) {
            return ErrorConstants.UNAUTHORIZED;
        }

        AccessDeniedException accessDeniedException = (AccessDeniedException) throwableAnalyzer.getFirstThrowableOfType(AccessDeniedException.class, causeChain);
        if (ObjectUtils.isNotNull(accessDeniedException)) {
            return ErrorConstants.ACCESS_DENIED;
        }
        return ErrorConstants.ERROR_CODE_UNKNOWN_ERROR;
    }
}
