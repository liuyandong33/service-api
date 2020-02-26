package build.dream.api.controllers;

import build.dream.api.models.weixin.AuthorizeModel;
import build.dream.api.services.WeiXinService;
import build.dream.common.beans.ComponentAccessToken;
import build.dream.common.beans.WeiXinOAuthToken;
import build.dream.common.beans.WeiXinUserInfo;
import build.dream.common.constants.Constants;
import build.dream.common.domains.saas.WeiXinPublicAccount;
import build.dream.common.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/weiXin")
public class WeiXinController {
    @Autowired
    private WeiXinService weiXinService;

    @RequestMapping(value = "/authorize")
    public String authorize() throws Exception {
        AuthorizeModel authorizeModel = ApplicationHandler.instantiateObject(AuthorizeModel.class, ApplicationHandler.getRequestParameters());
        String appId = authorizeModel.getAppId();
        String redirectUri = authorizeModel.getRedirectUri();
        String scope = authorizeModel.getScope();
        String state = authorizeModel.getState();
        String authorizeType = authorizeModel.getAuthorizeType();

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("redirectUri", redirectUri);
        queryParams.put("authorizeType", authorizeType);
        queryParams.put("scope", scope);

        String viewName = null;
        if ("1".equals(authorizeType)) {
            queryParams.put("appid", appId);
            viewName = "redirect:" + WeiXinUtils.generateAuthorizeUrl(appId, scope, UrlUtils.encode(CommonUtils.getOutsideUrl(ConfigurationUtils.getConfiguration(Constants.SERVICE_NAME), "weiXin", "authorizeCallback") + "?" + WebUtils.buildQueryString(queryParams)), state);
        } else if ("2".equals(authorizeType)) {
            String componentAppId = ConfigurationUtils.getConfiguration("");
            viewName = "redirect:" + WeiXinUtils.generateAuthorizeUrl(appId, scope, UrlUtils.encode(CommonUtils.getOutsideUrl(ConfigurationUtils.getConfiguration(Constants.SERVICE_NAME), "weiXin", "authorizeCallback") + "?" + WebUtils.buildQueryString(queryParams)), state, componentAppId);
        }
        return viewName;
    }

    @RequestMapping(value = "/authorizeCallback")
    public String authorizeCallback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String appId = requestParameters.get("appid");
        String code = requestParameters.get("code");
        String redirectUri = requestParameters.get("redirectUri");
        String state = requestParameters.get("state");
        String authorizeType = requestParameters.get("authorizeType");
        String scope = requestParameters.get("scope");

        WeiXinOAuthToken weiXinOAuthToken = null;
        if ("1".equals(authorizeType)) {
            WeiXinPublicAccount weiXinPublicAccount = weiXinService.obtainWeiXinPublicAccount(appId);
            weiXinOAuthToken = WeiXinUtils.obtainOAuthToken(appId, weiXinPublicAccount.getAppSecret(), code);
        } else if ("2".equals(authorizeType)) {
            String componentAppId = ConfigurationUtils.getConfiguration("");
            String componentAppSecret = ConfigurationUtils.getConfiguration("");
            ComponentAccessToken componentAccessToken = WeiXinUtils.obtainComponentAccessToken(componentAppId, componentAppSecret);
            weiXinOAuthToken = WeiXinUtils.obtainOAuthToken(appId, code, componentAppId, componentAccessToken.getComponentAccessToken());
        }

        StringBuilder stringBuilder = new StringBuilder(redirectUri);
        if (redirectUri.contains("?")) {
            stringBuilder.append("&");
        } else {
            stringBuilder.append("?");
        }
        stringBuilder.append("state=").append(UrlUtils.encode(state));

        if (Constants.SNSAPI_BASE.equals(scope)) {
            stringBuilder.append("&token=").append(UrlUtils.encode(JacksonUtils.writeValueAsString(weiXinOAuthToken)));
        } else if (Constants.SNSAPI_USERINFO.equals(scope)) {
            WeiXinUserInfo weiXinUserInfo = WeiXinUtils.obtainUserInfo(weiXinOAuthToken.getAccessToken(), weiXinOAuthToken.getOpenId(), null);
            stringBuilder.append("&userInfo=").append(UrlUtils.encode(JacksonUtils.writeValueAsString(weiXinUserInfo)));
        }
        return "redirect:" + stringBuilder.toString();
    }
}
