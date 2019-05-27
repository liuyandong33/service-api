package build.dream.api.configurations;

import build.dream.api.domains.BaseDomain;
import build.dream.api.domains.TmpFdb;

public class SyncDataConfiguration {
    public static final Class<? extends BaseDomain>[] SYNC_DOMAIN_CLASSES = new Class[]{TmpFdb.class};
    public static final String KEY_SYNC_DATA = "_sync_data";
    public static final String FIELD_NAME_DOMAIN_CLASS_NAME = "domainClassName";
    public static final String FIELD_NAME_DATA = "data";
}
