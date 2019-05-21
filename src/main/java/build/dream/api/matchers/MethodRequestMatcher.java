package build.dream.api.matchers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

public class MethodRequestMatcher implements RequestMatcher {
    private String method;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public MethodRequestMatcher() {
    }

    public MethodRequestMatcher(String method) {
        this.method = method;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String methodInParameter = request.getParameter("method");
        if (StringUtils.isNotBlank(methodInParameter)) {
            return methodInParameter.equals(method);
        }
        return false;
    }
}
