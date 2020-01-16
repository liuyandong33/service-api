package build.dream.api.controllers;

import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.UrlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping(value = "/sso")
public class SSOController {
    @RequestMapping(value = "/login")
    public String login(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        String accessToken = requestParameters.get("accessToken");
        String redirectUrl = requestParameters.get("redirectUrl");
        String originalRedirectUrl = requestParameters.get("originalRedirectUrl");
        String index = requestParameters.get("index");

        httpServletResponse.addCookie(new Cookie("ACCESS_TOKEN", accessToken));
        return "redirect:" + redirectUrl + "?token=" + accessToken + "&index=" + index + "&redirectUrl=" + UrlUtils.encode(originalRedirectUrl);
    }
}
