package build.dream.api.controllers;

import build.dream.api.constants.Constants;
import build.dream.api.models.data.UploadDataBodyModel;
import build.dream.api.models.data.UploadDataModel;
import build.dream.api.utils.ApiUtils;
import build.dream.common.api.ApiRest;
import build.dream.common.constants.ErrorConstants;
import build.dream.common.utils.*;
import com.aliyun.openservices.ons.api.Message;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping(value = "/data")
public class DataController {
    private static final String PLATFORM_PRIVATE_KEY = ConfigurationUtils.getConfiguration(Constants.PLATFORM_PRIVATE_KEY);

    @RequestMapping(value = "/uploadData", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String uploadData(HttpServletRequest httpServletRequest) {
        ApiRest apiRest = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        String requestBody = null;
        try {
            String contentType = httpServletRequest.getContentType();
            ValidateUtils.isTrue(Constants.CONTENT_TYPE_APPLICATION_JSON_UTF8.equals(contentType), ErrorConstants.INVALID_CONTENT_TYPE_ERROR);

            String requestMethod = httpServletRequest.getMethod();
            ValidateUtils.isTrue(Constants.REQUEST_METHOD_POST.equals(requestMethod), ErrorConstants.INVALID_REQUEST_METHOD_ERROR);

            UploadDataModel uploadDataModel = ApplicationHandler.instantiateObject(UploadDataModel.class, requestParameters);
            uploadDataModel.validateAndThrow();

            requestBody = ApplicationHandler.getRequestBody(httpServletRequest, Constants.CHARSET_NAME_UTF_8);
            ApiUtils.verifySign(uploadDataModel.toString() + requestBody, uploadDataModel.getSignature(), TenantUtils.obtainPrivateKey(), TenantUtils.obtainPublicKey());

            UploadDataBodyModel uploadDataBodyModel = JacksonUtils.readValue(requestBody, UploadDataBodyModel.class);
            uploadDataBodyModel.validateAndThrow();

            String topic = "_" + TenantUtils.obtainPartitionCode() + "_upload_data_message_topic";
//            KafkaUtils.send(topic, requestBody);
            Message message = new Message();
            message.setBody(requestBody.getBytes(Constants.CHARSET_UTF_8));
            message.setTopic(topic);
            RocketMQUtils.send(message);

            apiRest = ApiRest.builder()
                    .message("上传数据成功！")
                    .successful(true)
                    .build();
            apiRest.sign(PLATFORM_PRIVATE_KEY, Constants.DEFAULT_DATE_PATTERN);
        } catch (Exception e) {
            apiRest = ApiUtils.transformException(e);
            LogUtils.error("上传数据失败", this.getClass().getName(), "uploadData", e, requestParameters, requestBody);
        }
        return JacksonUtils.writeValueAsString(apiRest, Constants.DEFAULT_DATE_PATTERN);
    }
}
