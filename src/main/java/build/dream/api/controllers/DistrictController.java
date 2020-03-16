package build.dream.api.controllers;

import build.dream.common.api.ApiRest;
import build.dream.common.beans.District;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.DistrictUtils;
import build.dream.common.utils.JacksonUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/district")
public class DistrictController {
    /**
     * 获取所有省
     *
     * @return
     */
    @RequestMapping(value = "/obtainAllProvinces", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String obtainAllProvinces() {
        List<District> provinces = DistrictUtils.obtainAllProvinces();
        ApiRest apiRest = ApiRest.builder().data(provinces).message("获取所有省信息成功！").build();
        return JacksonUtils.writeValueAsString(apiRest);
    }

    /**
     * 根据pid获取数据
     *
     * @return
     */
    @RequestMapping(value = "/obtainDistrictsByPid", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String obtainDistrictsByPid() {
        List<District> districts = DistrictUtils.obtainDistrictsByPid(ApplicationHandler.getRequestParameter("pid"));
        ApiRest apiRest = ApiRest.builder().data(districts).message("获取区域信息成功！").build();
        return JacksonUtils.writeValueAsString(apiRest);
    }

    /**
     * 初始化数据，比较耗时，只有第一次或数据有变动时使用
     *
     * @return
     */
    @RequestMapping(value = "/initData", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String initData() {
        DistrictUtils.initData();
        return JacksonUtils.writeValueAsString(ApiRest.builder().message("初始化数据成功！").successful(true).build());
    }
}
