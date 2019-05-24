package build.dream.api.tasks;

import build.dream.api.domains.SyncDataConfiguration;
import build.dream.common.utils.CommonRedisUtils;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.JacksonUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HandleSyncDataTask implements Runnable {
    private static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String value = CommonRedisUtils.blpop(SyncDataConfiguration.KEY_SYNC_DATA, 30, TimeUnit.SECONDS);
                if (StringUtils.isNotBlank(value)) {
                    Map<String, String> dataMap = JacksonUtils.readValueAsMap(value, String.class, String.class);
                    String domainClassName = dataMap.get(SyncDataConfiguration.FIELD_NAME_DOMAIN_CLASS_NAME);
                    String data = dataMap.get(SyncDataConfiguration.FIELD_NAME_DATA);
                    Class<?> domainClass = forName(domainClassName);
                    List<?> dataList = JacksonUtils.readValueAsList(data, domainClass);
                    DatabaseHelper.insertAll(dataList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        new Thread(this).start();
    }
}
