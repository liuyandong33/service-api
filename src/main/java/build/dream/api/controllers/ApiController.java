package build.dream.api.controllers;

import build.dream.api.constants.Constants;
import build.dream.api.models.api.DevOpsV1Model;
import build.dream.api.models.api.OpV1Model;
import build.dream.api.models.api.V1Model;
import build.dream.api.models.api.V2Model;
import build.dream.api.utils.ApiUtils;
import build.dream.common.api.ApiRest;
import build.dream.common.constants.ConfigurationKeys;
import build.dream.common.tuples.Tuple3;
import build.dream.common.utils.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/api")
public class ApiController {
    private static final String PLATFORM_PRIVATE_KEY = ConfigurationUtils.getConfiguration(ConfigurationKeys.PLATFORM_PRIVATE_KEY);

    @RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String v1(HttpServletRequest httpServletRequest) {
        ApiRest apiRest = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        String requestBody = null;
        try {
            ApiUtils.validateRequestMethod(httpServletRequest);
            ApiUtils.validateContentType(httpServletRequest);
            V1Model v1Model = ApplicationHandler.instantiateObject(V1Model.class, requestParameters);
            v1Model.validateAndThrow();

            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("access_token", v1Model.getAccessToken());
            queryParams.put("timestamp", requestParameters.get("timestamp"));
            queryParams.put("id", v1Model.getId());

            Tuple3<String, String, String> tuple3 = ApiUtils.parseMethod(v1Model.getMethod());
            String partitionCode = TenantUtils.obtainPartitionCode();
            requestBody = ApplicationHandler.getRequestBody(httpServletRequest, Constants.CHARSET_NAME_UTF_8);
            apiRest = ProxyUtils.doPostWithJsonRequestBody(partitionCode, tuple3._1(), tuple3._2(), tuple3._3(), queryParams, requestBody);
            ValidateUtils.isTrue(apiRest.isSuccessful(), apiRest.getError());
        } catch (Exception e) {
            apiRest = ApiUtils.transformException(e);
            LogUtils.error("处理失败", this.getClass().getName(), "v1", e, requestParameters, requestBody);
        }
        return JacksonUtils.writeValueAsString(apiRest, Constants.DEFAULT_DATE_PATTERN);
    }

    @RequestMapping(value = "/v2", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String v2(HttpServletRequest httpServletRequest) {
        ApiRest apiRest = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        String requestBody = null;
        try {
            ApiUtils.validateRequestMethod(httpServletRequest);
            ApiUtils.validateContentType(httpServletRequest);
            V2Model v2Model = ApplicationHandler.instantiateObject(V2Model.class, requestParameters);
            v2Model.validateAndThrow();

            requestBody = ApplicationHandler.getRequestBody(httpServletRequest, Constants.CHARSET_NAME_UTF_8);
            ApiUtils.verifySign(v2Model.toString() + requestBody, v2Model.getSignature(), TenantUtils.obtainPrivateKey(), TenantUtils.obtainPublicKey());

            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("access_token", v2Model.getAccessToken());
            queryParams.put("timestamp", requestParameters.get("timestamp"));
            queryParams.put("id", v2Model.getId());

            Tuple3<String, String, String> tuple3 = ApiUtils.parseMethod(v2Model.getMethod());
            String partitionCode = TenantUtils.obtainPartitionCode();
            apiRest = ProxyUtils.doPostWithJsonRequestBody(partitionCode, tuple3._1(), tuple3._2(), tuple3._3(), queryParams, requestBody);
            ValidateUtils.isTrue(apiRest.isSuccessful(), apiRest.getError());

            apiRest.sign(PLATFORM_PRIVATE_KEY, Constants.DEFAULT_DATE_PATTERN);
        } catch (Exception e) {
            apiRest = ApiUtils.transformException(e);
            LogUtils.error("处理失败", this.getClass().getName(), "v2", e, requestParameters, requestBody);
        }
        return JacksonUtils.writeValueAsString(apiRest, Constants.DEFAULT_DATE_PATTERN);
    }

    /**
     * 运营系统api
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/opV1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String opV1(HttpServletRequest httpServletRequest) {
        ApiRest apiRest = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        String requestBody = null;
        try {
            ApiUtils.validateRequestMethod(httpServletRequest);
            ApiUtils.validateContentType(httpServletRequest);
            OpV1Model opV1Model = ApplicationHandler.instantiateObject(OpV1Model.class, requestParameters);
            opV1Model.validateAndThrow();

            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("access_token", opV1Model.getAccessToken());
            queryParams.put("timestamp", requestParameters.get("timestamp"));
            queryParams.put("id", opV1Model.getId());

            Tuple3<String, String, String> tuple3 = ApiUtils.parseMethod(opV1Model.getMethod());
            requestBody = ApplicationHandler.getRequestBody(httpServletRequest, Constants.CHARSET_NAME_UTF_8);
            apiRest = ProxyUtils.doPostWithJsonRequestBody(tuple3._1(), tuple3._2(), tuple3._3(), queryParams, requestBody);
            ValidateUtils.isTrue(apiRest.isSuccessful(), apiRest.getError());
        } catch (Exception e) {
            apiRest = ApiUtils.transformException(e);
            LogUtils.error("处理失败", this.getClass().getName(), "opV1", e, requestParameters, requestBody);
        }
        return JacksonUtils.writeValueAsString(apiRest, Constants.DEFAULT_DATE_PATTERN);
    }

    /**
     * 运维系统api
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/devOpsV1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String devOpsV1(HttpServletRequest httpServletRequest) {
        ApiRest apiRest = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        String requestBody = null;
        try {
            ApiUtils.validateRequestMethod(httpServletRequest);
            ApiUtils.validateContentType(httpServletRequest);
            DevOpsV1Model devOpsV1Model = ApplicationHandler.instantiateObject(DevOpsV1Model.class, requestParameters);
            devOpsV1Model.validateAndThrow();

            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("access_token", devOpsV1Model.getAccessToken());
            queryParams.put("timestamp", requestParameters.get("timestamp"));
            queryParams.put("id", devOpsV1Model.getId());

            Tuple3<String, String, String> tuple3 = ApiUtils.parseMethod(devOpsV1Model.getMethod());
            requestBody = ApplicationHandler.getRequestBody(httpServletRequest, Constants.CHARSET_NAME_UTF_8);
            apiRest = ProxyUtils.doPostWithJsonRequestBody(tuple3._1(), tuple3._2(), tuple3._3(), queryParams, requestBody);
            ValidateUtils.isTrue(apiRest.isSuccessful(), apiRest.getError());
        } catch (Exception e) {
            apiRest = ApiUtils.transformException(e);
            LogUtils.error("处理失败", this.getClass().getName(), "devOpsV1", e, requestParameters, requestBody);
        }
        return JacksonUtils.writeValueAsString(apiRest, Constants.DEFAULT_DATE_PATTERN);
    }
}
