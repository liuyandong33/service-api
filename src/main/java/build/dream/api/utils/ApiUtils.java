package build.dream.api.utils;

import build.dream.api.constants.Constants;
import build.dream.common.api.ApiRest;
import build.dream.common.constants.ConfigurationKeys;
import build.dream.common.constants.ErrorConstants;
import build.dream.common.exceptions.CustomException;
import build.dream.common.exceptions.Error;
import build.dream.common.tuples.Tuple3;
import build.dream.common.utils.ConfigurationUtils;
import build.dream.common.utils.SignatureUtils;
import build.dream.common.utils.TupleUtils;
import build.dream.common.utils.ValidateUtils;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;

public class ApiUtils {
    private static final String PLATFORM_PRIVATE_KEY = ConfigurationUtils.getConfiguration(ConfigurationKeys.PLATFORM_PRIVATE_KEY);

    /**
     * 转换异常
     *
     * @param exception
     * @return
     */
    public static ApiRest transformException(Exception exception) {
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

    public static void verifySign(String originalString, String signature, String privateKey, String publicKey) {
        byte[] data = originalString.getBytes(Constants.CHARSET_UTF_8);
        boolean verifySignResult = SignatureUtils.verifySign(data, Base64.decodeBase64(publicKey), Base64.decodeBase64(signature), SignatureUtils.SIGNATURE_TYPE_SHA256_WITH_RSA);
        if (verifySignResult) {
            return;
        }

        String message = "签名错误，原始字符串：" + originalString + "，签名：" + Base64.encodeBase64String(SignatureUtils.sign(data, Base64.decodeBase64(privateKey), SignatureUtils.SIGNATURE_TYPE_SHA256_WITH_RSA));
        Error error = new Error(ErrorConstants.ERROR_CODE_INVALID_SIGNATURE, message);
        throw new CustomException(error);
    }

    public static void validateRequestMethod(HttpServletRequest httpServletRequest) {
        ValidateUtils.isTrue(Constants.REQUEST_METHOD_POST.equals(httpServletRequest.getMethod()), ErrorConstants.INVALID_REQUEST_METHOD_ERROR);
    }


    public static void validateContentType(HttpServletRequest httpServletRequest) {
        ValidateUtils.isTrue(Constants.CONTENT_TYPE_APPLICATION_JSON_UTF8.equals(httpServletRequest.getContentType()), ErrorConstants.INVALID_CONTENT_TYPE_ERROR);
    }

    public static Tuple3<String, String, String> parseMethod(String method) {
        String[] array = method.split("\\.");
        return TupleUtils.buildTuple3(array[0], array[1], array[2]);
    }
}
