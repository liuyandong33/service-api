package build.dream.api.controllers;

import build.dream.api.constants.Constants;
import build.dream.api.models.api.V1Model;
import build.dream.common.api.ApiRest;
import build.dream.common.auth.TenantUserDetails;
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

import java.util.Map;

@Controller
@RequestMapping(value = "/api")
public class ApiController {
    private static final String PLATFORM_PRIVATE_KEY = ConfigurationUtils.getConfiguration(Constants.PLATFORM_PRIVATE_KEY);

    @RequestMapping(value = "/v1", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String v1() {
        ApiRest apiRest = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        try {
            V1Model v1Model = ApplicationHandler.instantiateObject(V1Model.class, requestParameters);
            v1Model.validateAndThrow();

            String method = v1Model.getMethod();
            String[] array = method.split("\\.");

            String serviceName = array[0];
            String controllerName = array[1];
            String actionName = array[2];

            TenantUserDetails tenantUserDetails = (TenantUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            verifySign(v1Model, tenantUserDetails.getPublicKey());

            String partitionCode = "zd1";
            String bizContent = requestParameters.get("biz_content");
            ApplicationHandler.getHttpServletRequest().getQueryString();
            apiRest = ProxyUtils.doPostWithRequestBody(partitionCode, serviceName, controllerName, actionName, bizContent);
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
                code = Constants.ERROR_CODE_UNKNOWN_ERROR;
                message = e.getMessage();
            }
            apiRest = ApiRest.builder().error(new Error(code, message)).successful(false).build();
            apiRest.sign(PLATFORM_PRIVATE_KEY, Constants.DEFAULT_DATE_PATTERN);
            LogUtils.error("处理失败", this.getClass().getName(), "v1", e, requestParameters);
        }
        return GsonUtils.toJson(apiRest, Constants.DEFAULT_DATE_PATTERN);
    }

    private void verifySign(V1Model v1Model, String publicKey) {
        byte[] data = v1Model.toString().getBytes(Constants.CHARSET_UTF_8);
        ValidateUtils.isTrue(SignatureUtils.verifySign(data, Base64.decodeBase64(publicKey), Base64.decodeBase64(v1Model.getSignature()), SignatureUtils.SIGNATURE_TYPE_SHA256_WITH_RSA), new Error("1234", "签名错误！"));
    }
}
