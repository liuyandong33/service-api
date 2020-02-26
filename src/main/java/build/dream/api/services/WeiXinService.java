package build.dream.api.services;

import build.dream.common.domains.saas.WeiXinPublicAccount;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.SearchModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeiXinService {
    @Transactional(readOnly = true)
    public WeiXinPublicAccount obtainWeiXinPublicAccount(String appId) {
        SearchModel searchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .equal(WeiXinPublicAccount.ColumnName.APP_ID, appId)
                .build();
        return DatabaseHelper.find(WeiXinPublicAccount.class, searchModel);
    }
}
