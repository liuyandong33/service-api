package build.dream.api.controllers;

import build.dream.api.configurations.SyncDataConfiguration;
import build.dream.api.constants.Constants;
import build.dream.api.domains.BaseDomain;
import build.dream.common.api.ApiRest;
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

        return GsonUtils.toJson(ApiRest.builder().message("处理成功！").successful(true).build());
    }

    @RequestMapping(value = "/sign")
    public String sign() {
        return "demo/sign";
    }

    @RequestMapping(value = "/doSign", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String doSign() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String accessToken = requestParameters.get("access_token");
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
        return Base64.encodeBase64String(sign);
    }
}
