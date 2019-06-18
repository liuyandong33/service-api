package build.dream.api.services;

import build.dream.common.saas.domains.PosPrivilege;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.SearchModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrivilegeService {
    @Transactional(readOnly = true)
    public List<PosPrivilege> obtainAllPosPrivileges() {
        SearchModel searchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .build();
        return DatabaseHelper.findAll(PosPrivilege.class, searchModel);
    }
}
