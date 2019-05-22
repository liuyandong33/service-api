package build.dream.api.security;

import build.dream.api.constants.Constants;
import build.dream.common.api.ApiRest;
import build.dream.common.constants.ErrorCodeConstants;
import build.dream.common.exceptions.Error;
import build.dream.common.utils.ConfigurationUtils;
import build.dream.common.utils.GsonUtils;
import build.dream.common.utils.ObjectUtils;
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
    private static final String PLATFORM_PRIVATE_KEY = ConfigurationUtils.getConfiguration(Constants.PLATFORM_PRIVATE_KEY);
    private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
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
        return GsonUtils.toJson(apiRest);
    }

    public String translateErrorCode(String errorCode) {
        switch (errorCode) {
            case ErrorCodeConstants.INVALID_REQUEST:
                return ErrorCodeConstants.ERROR_CODE_INVALID_REQUEST;
            case ErrorCodeConstants.INVALID_CLIENT:
                return ErrorCodeConstants.ERROR_CODE_INVALID_CLIENT;
            case ErrorCodeConstants.INVALID_GRANT:
                return ErrorCodeConstants.ERROR_CODE_INVALID_GRANT;
            case ErrorCodeConstants.UNAUTHORIZED_CLIENT:
                return ErrorCodeConstants.ERROR_CODE_UNAUTHORIZED_CLIENT;
            case ErrorCodeConstants.UNSUPPORTED_GRANT_TYPE:
                return ErrorCodeConstants.ERROR_CODE_UNSUPPORTED_GRANT_TYPE;
            case ErrorCodeConstants.INVALID_SCOPE:
                return ErrorCodeConstants.ERROR_CODE_INVALID_SCOPE;
            case ErrorCodeConstants.INSUFFICIENT_SCOPE:
                return ErrorCodeConstants.ERROR_CODE_INSUFFICIENT_SCOPE;
            case ErrorCodeConstants.INVALID_TOKEN:
                return ErrorCodeConstants.ERROR_CODE_INVALID_TOKEN;
            case ErrorCodeConstants.REDIRECT_URI_MISMATCH:
                return ErrorCodeConstants.ERROR_CODE_REDIRECT_URI_MISMATCH;
            case ErrorCodeConstants.UNSUPPORTED_RESPONSE_TYPE:
                return ErrorCodeConstants.ERROR_CODE_UNSUPPORTED_RESPONSE_TYPE;
            case ErrorCodeConstants.ACCESS_DENIED:
                return ErrorCodeConstants.ERROR_CODE_ACCESS_DENIED;
            case ErrorCodeConstants.UNAUTHORIZED:
                return ErrorCodeConstants.ERROR_CODE_UNAUTHORIZED;
            default:
                return ErrorCodeConstants.ERROR_CODE_UNKNOWN_ERROR;
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
            return ErrorCodeConstants.UNAUTHORIZED;
        }

        AccessDeniedException accessDeniedException = (AccessDeniedException) throwableAnalyzer.getFirstThrowableOfType(AccessDeniedException.class, causeChain);
        if (ObjectUtils.isNotNull(accessDeniedException)) {
            return ErrorCodeConstants.ACCESS_DENIED;
        }
        return ErrorCodeConstants.ERROR_CODE_UNKNOWN_ERROR;
    }
}
