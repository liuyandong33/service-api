package build.dream.api.controllers;

import build.dream.common.saas.domains.OauthClientDetail;
import build.dream.common.utils.GsonUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/demo")
public class DemoController {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/test")
    @ResponseBody
    public String test() throws IOException, SQLException {
        String mybatisConfigPath = "aa_mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(mybatisConfigPath);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        Connection connection = DriverManager.getConnection("jdbc:mysql://leopard:8066/saas-db?serverTimezone=GMT%2B8&useSSL=true", "root", "root");
        SqlSession sqlSession = sqlSessionFactory.openSession(connection);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", BigInteger.ONE);

        OauthClientDetail oauthClientDetail = sqlSession.selectOne("build.dream.api.mappers.RoleMapper.find", map);
        return GsonUtils.toJson(oauthClientDetail);
    }
}
