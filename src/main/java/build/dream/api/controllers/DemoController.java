package build.dream.api.controllers;

import build.dream.api.mappers.CommonMapper;
import build.dream.api.mappers.UniversalMapper;
import build.dream.common.saas.domains.OauthClientDetail;
import build.dream.common.utils.GsonUtils;
import build.dream.common.utils.UniversalDatabaseHelper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

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

        Configuration configuration = sqlSessionFactory.getConfiguration();
        configuration.addMapper(UniversalMapper.class);
        configuration.addMapper(CommonMapper.class);

        UniversalMapper universalMapper = sqlSession.getMapper(UniversalMapper.class);

        List<OauthClientDetail> oauthClientDetails = UniversalDatabaseHelper.findAll(universalMapper, OauthClientDetail.class);
        int a = 100;

        return GsonUtils.toJson(oauthClientDetails);
    }
}
