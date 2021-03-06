package build.dream.api.services;

import build.dream.common.domains.saas.*;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.SearchModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrivilegeService {
    /**
     * 获取所有POS权限
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<PosPrivilege> obtainAllPosPrivileges() {
        SearchModel searchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .build();
        return DatabaseHelper.findAll(PosPrivilege.class, searchModel);
    }

    /**
     * 获取所有APP权限
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<AppPrivilege> obtainAllAppPrivileges() {
        SearchModel searchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .build();
        return DatabaseHelper.findAll(AppPrivilege.class, searchModel);
    }

    /**
     * 获取所有后台权限
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<BackgroundPrivilege> obtainAllBackgroundPrivileges() {
        SearchModel searchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .build();
        return DatabaseHelper.findAll(BackgroundPrivilege.class, searchModel);
    }

    /**
     * 获取所有运维平台权限
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<DevOpsPrivilege> obtainDevOpsPrivileges() {
        SearchModel searchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .build();
        return DatabaseHelper.findAll(DevOpsPrivilege.class, searchModel);
    }

    /**
     * 获取所有运营平台权限
     *
     * @return
     */
    public List<OpPrivilege> obtainAllOpPrivileges() {
        SearchModel searchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .build();
        return DatabaseHelper.findAll(OpPrivilege.class, searchModel);
    }
}
