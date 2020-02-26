package build.dream.api.controllers;

import build.dream.api.constants.Constants;
import build.dream.api.models.lightapp.V1Model;
import build.dream.api.utils.ApiUtils;
import build.dream.common.api.ApiRest;
import build.dream.common.constants.ErrorConstants;
import build.dream.common.utils.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/lightApp")
public class LightAppController {
    private static final String PLATFORM_PRIVATE_KEY = ConfigurationUtils.getConfiguration(Constants.PLATFORM_PRIVATE_KEY);

    @RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String v1(HttpServletRequest httpServletRequest) {
        ApiRest apiRest = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        String requestBody = null;
        try {
            String contentType = httpServletRequest.getContentType();
            ValidateUtils.isTrue(Constants.CONTENT_TYPE_APPLICATION_JSON_UTF8.equals(contentType), ErrorConstants.INVALID_CONTENT_TYPE_ERROR);

            String requestMethod = httpServletRequest.getMethod();
            ValidateUtils.isTrue(Constants.REQUEST_METHOD_POST.equals(requestMethod), ErrorConstants.INVALID_REQUEST_METHOD_ERROR);

            V1Model v1Model = ApplicationHandler.instantiateObject(V1Model.class, requestParameters);
            v1Model.validateAndThrow();

            String method = v1Model.getMethod();
            String[] array = method.split("\\.");

            String serviceName = array[0];
            String controllerName = array[1];
            String actionName = array[2];


            requestBody = ApplicationHandler.getRequestBody(httpServletRequest, Constants.CHARSET_NAME_UTF_8);
            ApiUtils.verifySign(v1Model.toString() + requestBody, v1Model.getSignature(), TenantUtils.obtainPrivateKey(), TenantUtils.obtainPublicKey());

            String ticket = v1Model.getTicket();
            byte[] data = RSAUtils.decryptByPrivateKey(ticket.getBytes(Constants.CHARSET_UTF_8), Base64.decodeBase64(PLATFORM_PRIVATE_KEY), RSAUtils.PADDING_MODE_RSA_ECB_PKCS1PADDING);
            String ticketPlaintext = new String(data, Constants.CHARSET_UTF_8);
            Map<String, Object> ticketPlaintextMap = JacksonUtils.readValueAsMap(ticketPlaintext, String.class, Object.class);
            long timestamp = MapUtils.getLongValue(ticketPlaintextMap, "timestamp");
            ValidateUtils.isTrue(System.currentTimeMillis() - timestamp < 2 * 60 * 60 * 1000, "ticket 已过期");

            String partitionCode = TenantUtils.obtainPartitionCode();

            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("access_token", MapUtils.getString(ticketPlaintextMap, "accessToken"));
            queryParams.put("timestamp", requestParameters.get("timestamp"));
            queryParams.put("id", v1Model.getId());
            String result = ProxyUtils.doPostOriginalWithJsonRequestBody(partitionCode, serviceName, controllerName, actionName, queryParams, requestBody);
            apiRest = JacksonUtils.readValue(result, ApiRest.class);
            ValidateUtils.isTrue(apiRest.isSuccessful(), apiRest.getError());

            apiRest.sign(PLATFORM_PRIVATE_KEY, Constants.DEFAULT_DATE_PATTERN);
        } catch (Exception e) {
            apiRest = ApiUtils.transformException(e);
            LogUtils.error("处理失败", this.getClass().getName(), "v1", e, requestParameters, requestBody);
        }
        return JacksonUtils.writeValueAsString(apiRest, Constants.DEFAULT_DATE_PATTERN);
    }
}
