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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping(value = "/api")
public class ApiController {
    private static final String PLATFORM_PRIVATE_KEY = ConfigurationUtils.getConfiguration(Constants.PLATFORM_PRIVATE_KEY);

    @RequestMapping(value = "/v1", method = {RequestMethod.GET, RequestMethod.POST}, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String v1(HttpServletRequest httpServletRequest) {
        ApiRest apiRest = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        try {
            V1Model v1Model = ApplicationHandler.instantiateObject(V1Model.class, requestParameters);
            v1Model.validateAndThrow();

            String method = v1Model.getMethod();
            String[] array = method.split("\\.");

            String serviceName = array[0];
            String controllerName = array[1];
            String actionName = array[2];

            TenantUserDetails tenantUserDetails = (TenantUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String requestBody = ApplicationHandler.getRequestBody(httpServletRequest, Constants.CHARSET_NAME_UTF_8);
            verifySign(v1Model, requestBody, tenantUserDetails.getPrivateKey(), tenantUserDetails.getPublicKey());

            String partitionCode = "zd1";
            String bizContent = requestParameters.get("biz_content");
            ApplicationHandler.getHttpServletRequest().getQueryString();
            apiRest = ProxyUtils.doPostWithRequestBody(partitionCode, serviceName, controllerName, actionName, requestBody);
            ValidateUtils.isTrue(apiRest.isSuccessful(), apiRest.getError());

            apiRest.sign(PLATFORM_PRIVATE_KEY, Constants.DEFAULT_DATE_PATTERN);
        } catch (Exception e) {
            String code = null;
            String message = null;
            if (e instanceof CustomException) {
                CustomException customException = (CustomException) e;
                code = customException.getCode();
                message = customException.getMessage();
            } else {
                code = ErrorConstants.ERROR_CODE_UNKNOWN_ERROR;
                message = e.getMessage();
            }
            apiRest = ApiRest.builder().error(new Error(code, message)).successful(false).build();
            apiRest.sign(PLATFORM_PRIVATE_KEY, Constants.DEFAULT_DATE_PATTERN);
            LogUtils.error("处理失败", this.getClass().getName(), "v1", e, requestParameters);
        }
        return GsonUtils.toJson(apiRest, Constants.DEFAULT_DATE_PATTERN);
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
