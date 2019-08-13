package build.dream.api.services;

import build.dream.common.domains.saas.AppPrivilege;
import build.dream.common.domains.saas.BackgroundPrivilege;
import build.dream.common.domains.saas.PosPrivilege;
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
}
