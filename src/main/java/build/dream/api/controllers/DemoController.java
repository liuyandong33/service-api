package build.dream.api.controllers;

import build.dream.api.configurations.SyncDataConfiguration;
import build.dream.api.constants.Constants;
import build.dream.api.domains.BaseDomain;
import build.dream.common.api.ApiRest;
import build.dream.common.constants.ConfigurationKeys;
import build.dream.common.domains.saas.TenantSecretKey;
import build.dream.common.mappers.CommonMapper;
import build.dream.common.mappers.UniversalMapper;
import build.dream.common.utils.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping(value = "/demo")
public class DemoController {
    public static final String PLATFORM_PUBLIC_KEY = ConfigurationUtils.getConfiguration(ConfigurationKeys.PLATFORM_PUBLIC_KEY);

    static {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/test")
    @ResponseBody
    public String test() throws IOException, SQLException {
        String dbIp = "192.168.51.166";
        String user = "sa";
        String password = "123";
        String port = "1433";
        String dbName = "KgtBsRegie2013_bak";
        String url = "jdbc:jtds:sqlserver://" + dbIp + ":" + port + "/" + dbName;

        String mybatisConfigPath = "aa_mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(mybatisConfigPath);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        Connection connection = DriverManager.getConnection(url, user, password);
        SqlSession sqlSession = sqlSessionFactory.openSession(connection);

        Configuration configuration = sqlSessionFactory.getConfiguration();
        configuration.setDatabaseId(DatabaseUtils.obtainDatabaseId(connection, false));
        configuration.addMapper(UniversalMapper.class);
        configuration.addMapper(CommonMapper.class);
        UniversalMapper universalMapper = sqlSession.getMapper(UniversalMapper.class);

        for (Class<? extends BaseDomain> domainClass : SyncDataConfiguration.SYNC_DOMAIN_CLASSES) {
            boolean proceed = true;
            BigInteger maxId = BigInteger.ZERO;
            String domainClassName = domainClass.getName();
            while (proceed) {
                PagedSearchModel pagedSearchModel = new PagedSearchModel();
                pagedSearchModel.addSearchCondition("id", Constants.SQL_OPERATION_SYMBOL_GREATER_THAN, maxId);
                pagedSearchModel.setOrderBy("id");
                pagedSearchModel.setPage(1);
                pagedSearchModel.setRows(5000);

                List<? extends BaseDomain> data = UniversalDatabaseHelper.findAllPaged(universalMapper, domainClass, pagedSearchModel);
                int size = data.size();
                if (size > 0) {
                    Map<String, Object> valueMap = new HashMap<String, Object>();
                    valueMap.put(SyncDataConfiguration.FIELD_NAME_DOMAIN_CLASS_NAME, domainClassName);
                    valueMap.put(SyncDataConfiguration.FIELD_NAME_DATA, ZipUtils.zipText(JacksonUtils.writeValueAsString(data)));
                    CommonRedisUtils.lpush(SyncDataConfiguration.KEY_SYNC_DATA, JacksonUtils.writeValueAsString(valueMap));
                    maxId = data.get(size - 1).getId();
                } else {
                    proceed = false;
                }
            }
        }
        sqlSession.close();
        connection.close();

        return JacksonUtils.writeValueAsString(ApiRest.builder().message("处理成功！").successful(true).build());
    }

    @RequestMapping(value = "/sign")
    public ModelAndView sign() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String tenantId = requestParameters.get("tenantId");
        String tenantCode = requestParameters.get("tenantCode");


        Map<String, Object> model = new HashMap<String, Object>();
        model.put("timestamp", CustomDateUtils.format(new Date(), Constants.DEFAULT_DATE_PATTERN));
        model.put("id", UUID.randomUUID().toString());

        if (StringUtils.isNotBlank(tenantId)) {
            SearchModel searchModel = SearchModel.builder()
                    .autoSetDeletedFalse()
                    .equal(TenantSecretKey.ColumnName.TENANT_ID, BigInteger.valueOf(Long.valueOf(tenantId)))
                    .build();
            TenantSecretKey tenantSecretKey = DatabaseHelper.find(TenantSecretKey.class, searchModel);
            if (Objects.nonNull(tenantSecretKey)) {
                model.put("privateKey", tenantSecretKey.getPrivateKey());
            }
        }

        if (StringUtils.isNotBlank(tenantCode)) {
            SearchModel searchModel = SearchModel.builder()
                    .autoSetDeletedFalse()
                    .equal(TenantSecretKey.ColumnName.TENANT_CODE, tenantCode)
                    .build();
            TenantSecretKey tenantSecretKey = DatabaseHelper.find(TenantSecretKey.class, searchModel);
            if (Objects.nonNull(tenantSecretKey)) {
                model.put("privateKey", tenantSecretKey.getPrivateKey());
            }
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("demo/sign");
        modelAndView.addAllObjects(model);
        return modelAndView;
    }

    @RequestMapping(value = "/doSign", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String doSign() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String serviceName = requestParameters.get("serviceName");
        String apiVersion = requestParameters.get("apiVersion");
        String accessToken = requestParameters.get("accessToken");
        String method = requestParameters.get("method");
        String timestamp = requestParameters.get("timestamp");
        String id = requestParameters.get("id");
        String body = requestParameters.get("body");
        String privateKey = requestParameters.get("privateKey");

        Map<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.put("access_token", accessToken);
        sortedMap.put("method", method);
        sortedMap.put("timestamp", timestamp);
        sortedMap.put("id", id);

        List<String> pairs = new ArrayList<String>();
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            pairs.add(entry.getKey() + "=" + entry.getValue());
        }
        byte[] data = (StringUtils.join(pairs, "&") + body).getBytes(Constants.CHARSET_UTF_8);
        byte[] sign = SignatureUtils.sign(data, Base64.decodeBase64(privateKey), SignatureUtils.SIGNATURE_TYPE_SHA256_WITH_RSA);

        String signature = Base64.encodeBase64String(sign);
        Map<String, String> queryStringMap = new HashMap<String, String>();
        queryStringMap.put("access_token", accessToken);
        queryStringMap.put("method", method);
        queryStringMap.put("timestamp", timestamp);
        queryStringMap.put("id", id);
        queryStringMap.put("signature", signature);

        Map<String, String> result = new HashMap<String, String>();

        String url = CommonUtils.getOutsideUrl(serviceName, "api", apiVersion) + "?" + WebUtils.buildQueryString(queryStringMap, Constants.CHARSET_NAME_UTF_8);
        url = "http://localhost:41011/api/v1?" + WebUtils.buildQueryString(queryStringMap, Constants.CHARSET_NAME_UTF_8);
        result.put("url", url);
        result.put("signature", signature);
        return JacksonUtils.writeValueAsString(result);
    }

    @RequestMapping(value = "/generateTicket", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String generateTicket() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String accessToken = requestParameters.get("accessToken");
        String clientId = requestParameters.get("clientId");

        Map<String, Object> ticketPlaintextMap = new HashMap<String, Object>();
        ticketPlaintextMap.put("accessToken", accessToken);
        ticketPlaintextMap.put("timestamp", System.currentTimeMillis());
        ticketPlaintextMap.put("clientId", clientId);
        String ticketPlaintext = JacksonUtils.writeValueAsString(ticketPlaintextMap);
        byte[] data = RSAUtils.encryptByPrivateKey(ticketPlaintext.getBytes(Constants.CHARSET_UTF_8), Base64.decodeBase64(PLATFORM_PUBLIC_KEY), RSAUtils.PADDING_MODE_RSA_ECB_PKCS1PADDING);
        return Base64.encodeBase64String(data);
    }
}
