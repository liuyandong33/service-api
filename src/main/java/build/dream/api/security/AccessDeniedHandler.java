package build.dream.api.security;

import build.dream.common.api.ApiRest;
import build.dream.common.constants.ErrorConstants;
import build.dream.common.exceptions.Error;
import build.dream.common.utils.GsonUtils;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ApiRest apiRest = ApiRest.builder()
                .error(new Error(ErrorConstants.ERROR_CODE_ACCESS_DENIED, accessDeniedException.getMessage()))
                .successful(false)
                .build();
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(GsonUtils.toJson(apiRest));
    }
}
