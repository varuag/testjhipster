package com.varuag.app.config;

import com.hazelcast.config.*;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.instance.HazelcastInstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

@Configuration
@EnableCaching
@AutoConfigureAfter(value = {MetricsConfiguration.class, DatabaseConfiguration.class})
@Profile("!" + Constants.SPRING_PROFILE_FAST)
public class CacheConfiguration {

  private final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

  private static HazelcastInstance hazelcastInstance;

  @Inject
  private Environment env;

  private CacheManager cacheManager;

  @PreDestroy
  public void destroy() {
    log.info("Closing Cache Manager");
    Hazelcast.shutdownAll();
  }

  @Bean
  public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
    log.debug("Starting HazelcastCacheManager");
    cacheManager = new com.hazelcast.spring.cache.HazelcastCacheManager(hazelcastInstance);
    return cacheManager;
  }

  @Bean
  public HazelcastInstance hazelcastInstance() {
    log.debug("Configuring Hazelcast");
    Config config = new Config();
    config.setInstanceName("jhipster");
    config.getNetworkConfig().setPort(5701);
    config.getNetworkConfig().setPortAutoIncrement(true);

    config.setManagementCenterConfig(initializeManagementCenterConfig());

    if (env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
      System.setProperty("hazelcast.local.localAddress", "127.0.0.1");

      config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
      config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
      config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
    }

    config.getMapConfigs().put("default", initializeDefaultMapConfig());
    config.getMapConfigs().put("com.varuag.app.domain.*", initializeDomainMapConfig());
    config.getMapConfigs().put("my-sessions", initializeClusteredSession());

    hazelcastInstance = HazelcastInstanceFactory.newHazelcastInstance(config);

    return hazelcastInstance;
  }

  private ManagementCenterConfig initializeManagementCenterConfig() {
    ManagementCenterConfig config = new ManagementCenterConfig();
    config.setEnabled(true);
    config.setUrl("http://localhost:8085/mancenter");
    return config;
  }

  private MapConfig initializeDefaultMapConfig() {
    MapConfig mapConfig = new MapConfig();

        /*
            Number of backups. If 1 is set as the backup-count for example,
            then all entries of the map will be copied to another JVM for
            fail-safety. Valid numbers are 0 (no backup), 1, 2, 3.
         */
    mapConfig.setBackupCount(0);

        /*
            Valid values are:
            NONE (no eviction),
            LRU (Least Recently Used),
            LFU (Least Frequently Used).
            NONE is the default.
         */
    mapConfig.setEvictionPolicy(EvictionPolicy.LRU);

        /*
            Maximum size of the map. When max size is reached,
            map is evicted based on the policy defined.
            Any integer between 0 and Integer.MAX_VALUE. 0 means
            Integer.MAX_VALUE. Default is 0.
         */
    mapConfig.setMaxSizeConfig(new MaxSizeConfig(0, MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE));

        /*
            When max. size is reached, specified percentage of
            the map will be evicted. Any integer between 0 and 100.
            If 25 is set for example, 25% of the entries will
            get evicted.
         */
    mapConfig.setEvictionPercentage(25);

    return mapConfig;
  }

  private MapConfig initializeDomainMapConfig() {
    MapConfig mapConfig = new MapConfig();

    mapConfig.setTimeToLiveSeconds(env.getProperty("cache.timeToLiveSeconds", Integer.class, 3600));
    return mapConfig;
  }


  private MapConfig initializeClusteredSession() {
    MapConfig mapConfig = new MapConfig();

    mapConfig.setBackupCount(env.getProperty("cache.hazelcast.backupCount", Integer.class, 1));
    mapConfig.setTimeToLiveSeconds(env.getProperty("cache.timeToLiveSeconds", Integer.class, 3600));
    return mapConfig;
  }

  /**
   * @return the unique instance.
   */
  public static HazelcastInstance getHazelcastInstance() {
    return hazelcastInstance;
  }
}
