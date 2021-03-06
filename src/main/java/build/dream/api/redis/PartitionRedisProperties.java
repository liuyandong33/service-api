package build.dream.api.redis;

import build.dream.common.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value = "partition.redis")
public class PartitionRedisProperties extends RedisProperties {
}
