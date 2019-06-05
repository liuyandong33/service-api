package build.dream.api.services;

import build.dream.api.models.demo.DemoModel;
import build.dream.common.api.ApiRest;
import org.springframework.stereotype.Service;

@Service
public class DemoService {
    public ApiRest demo(DemoModel demoModel) {
        return ApiRest.builder().message("处理成功！").successful(true).build();
    }
}
