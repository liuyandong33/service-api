package build.dream.api.controllers;

import build.dream.api.constants.Constants;
import build.dream.api.models.api.V1Model;
import build.dream.common.api.ApiRest;
import build.dream.common.auth.TenantUserDetails;
import build.dream.common.constants.ErrorConstants;
import build.dream.common.exceptions.CustomException;
import build.dream.common.exceptions.Error;
import build.dream.common.utils.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/api")
public class ApiController {
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

            TenantUserDetails tenantUserDetails = (TenantUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            requestBody = ApplicationHandler.getRequestBody(httpServletRequest, Constants.CHARSET_NAME_UTF_8);
            verifySign(v1Model, requestBody, tenantUserDetails.getPrivateKey(), tenantUserDetails.getPublicKey());

            String partitionCode = tenantUserDetails.getPartitionCode();

            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("access_token", v1Model.getAccessToken());
            queryParams.put("timestamp", requestParameters.get("timestamp"));
            queryParams.put("id", v1Model.getId());
            String result = ProxyUtils.doPostOriginalWithJsonRequestBody(partitionCode, serviceName, controllerName, actionName, queryParams, requestBody);
            apiRest = JacksonUtils.readValue(result, ApiRest.class);
            ValidateUtils.isTrue(apiRest.isSuccessful(), apiRest.getError());

            apiRest.sign(PLATFORM_PRIVATE_KEY, Constants.DEFAULT_DATE_PATTERN);
        } catch (Exception e) {
            apiRest = transformException(e);
            LogUtils.error("处理失败", this.getClass().getName(), "v1", e, requestParameters, requestBody);
        }
        return JacksonUtils.writeValueAsString(apiRest, Constants.DEFAULT_DATE_PATTERN);
    }

    /**
     * 转换返回异常
     *
     * @param exception
     * @return
     */
    private ApiRest transformException(Exception exception) {
        String code = null;
        String message = null;
        if (exception instanceof CustomException) {
            CustomException customException = (CustomException) exception;
            code = customException.getCode();
            message = customException.getMessage();
        } else {
            code = ErrorConstants.ERROR_CODE_UNKNOWN_ERROR;
            message = exception.getMessage();
        }
        ApiRest apiRest = ApiRest.builder().error(new Error(code, message)).successful(false).build();
        apiRest.sign(PLATFORM_PRIVATE_KEY, Constants.DEFAULT_DATE_PATTERN);
        return apiRest;
    }

    private void verifySign(V1Model v1Model, String requestBody, String privateKey, String publicKey) {
        String str = v1Model.toString() + requestBody;
        byte[] data = str.getBytes(Constants.CHARSET_UTF_8);
        boolean verifySignResult = SignatureUtils.verifySign(data, Base64.decodeBase64(publicKey), Base64.decodeBase64(v1Model.getSignature()), SignatureUtils.SIGNATURE_TYPE_SHA256_WITH_RSA);
        if (verifySignResult) {
            return;
        }

        String message = "签名错误，原始字符串：" + str + "，签名：" + Base64.encodeBase64String(SignatureUtils.sign(data, Base64.decodeBase64(privateKey), SignatureUtils.SIGNATURE_TYPE_SHA256_WITH_RSA));
        Error error = new Error(ErrorConstants.ERROR_CODE_INVALID_SIGNATURE, message);
        throw new CustomException(error);
    }
}
