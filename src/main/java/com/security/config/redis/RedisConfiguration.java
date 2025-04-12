package com.security.config.redis;

import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import com.security.constants.ConstantData;
import com.security.model.ServerDetails;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

//@Configuration
@Component
public class RedisConfiguration {
    private int connectionType = ConstantData.TYPE_SENTINAL; // or use ConstantData.TYPE_SINGLE
    private String name = "mymaster";
    private String host = "host.docker.internal";
    private int port = 6379;
    private String password = "abhi123";
    private String sentinelPassword = "abhi123"; // Use this if sentinel auth is required
    private String sentinels = "sentinel-1:26379,sentinel-2:26379,sentinel-3:26379";

    int nType = 0;

    private static final Logger log = LoggerFactory.getLogger(RedisConfiguration.class);
    private JedisSentinelPool objSentinelPool;
    private JedisPool objSinglePool;


    @PostConstruct
    public void postConstruct() {
        initializeRedis();
    }

    public void initializeRedis() {
        log.info("******* Initializing Redis ******* Version: {}", ConstantData.strRedisVersion);
        log.info("Redis Config -> connectionType: {}, name: {}, host: {}, port: {}, sentinels: {}",
                connectionType, name, host, port, sentinels);
        ServerDetails objRedisServerDetails = new ServerDetails(1, "Redis Server", "redis", 6379, name, password, connectionType);
        final Set<String> sentinelsSet = new HashSet<>();
        nType = objRedisServerDetails.getConnectiontype();
        JedisPoolConfig objConfig = new JedisPoolConfig();
        objConfig.setMaxTotal(100);
        objConfig.setMaxIdle(40);
        objConfig.setMinIdle(5);
        objConfig.setTestOnBorrow(true);
        objConfig.setTestOnCreate(true);
        objConfig.setTestOnReturn(true);
        objConfig.setNumTestsPerEvictionRun(-1);
        int ntimeout = 2000;

        try {
            switch (nType) {
                case ConstantData.TYPE_SENTINAL:
                    if (sentinels != null) {
                        for (String sentinel : sentinels.split(",")) {
                            sentinelsSet.add(sentinel.trim());
                        }
                    }
                    log.info("sentinels list :: " + sentinelsSet.toString());
                    objSentinelPool = new JedisSentinelPool(
                            objRedisServerDetails.getLoginName(), // master name
                            sentinelsSet,
                            objConfig,
                            ntimeout,
                            sentinelPassword // password for sentinel if needed
//                            objRedisServerDetails.getPassword() // password for Redis
                    );
                    log.info("Initialized Redis with sentinels.");
                    break;
                case ConstantData.TYPE_SINGLE:
                    objSinglePool = new JedisPool(
                            objConfig,
                            objRedisServerDetails.getIpAddress(),
                            objRedisServerDetails.getPortno(),
                            ntimeout,
                            objRedisServerDetails.getPassword()
                    );
                    log.info("Initialized Redis without sentinels.");
                    break;
                case ConstantData.TYPE_SINGLE_SECURE:
                    SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    SSLParameters sslParameters = new SSLParameters();
                    sslParameters.setEndpointIdentificationAlgorithm("HTTPS");

                    log.debug("Configured SSL parameters for secure connection.");

                    objSinglePool = new JedisPool(
                            objConfig,
                            objRedisServerDetails.getIpAddress(),
                            objRedisServerDetails.getPortno(),
                            ntimeout,
                            objRedisServerDetails.getPassword(),
                            true,
                            sslSocketFactory,
                            sslParameters,
                            null // hostnameVerifier
                    );

                    log.info("Initialized Redis with secure connection.");
                    break;
                default:
                    log.warn("Unknown Redis connection type: {}", nType);
                    return;
            }

            try (Jedis objJedis = getRedisConn()) {
                if (objJedis != null) {
                    log.info("Redis connection is alive: {}", objJedis.ping());
                } else {
                    log.error("Failed to establish Redis connection.");
                }
            }
        } catch (JedisException e) {
            log.error("JedisException occurred while initializing Redis: ", e);
        } catch (Exception e) {
            log.error("Unexpected exception occurred while initializing Redis: ", e);
        }
    }

    public Jedis getRedisConn() {
        log.info("Redis Version: {}, Connection Type: {}", getRedisHandlerVersion(), nType);
        try {
            if (nType == ConstantData.TYPE_SENTINAL || nType == ConstantData.TYPE_SENTINAL_SECURE) {
                if (objSentinelPool == null) {
                    initializeRedis();
                }
                return objSentinelPool != null ? objSentinelPool.getResource() : null;
            }
//            else if (nType == ConstantData.TYPE_SINGLE || nType == ConstantData.TYPE_SINGLE_SECURE) {
//                if (objSinglePool == null) {
//                    initializeRedis();
//                }
//                return objSinglePool != null ? objSinglePool.getResource() : null;
//            }
            else{
                if (objSinglePool == null) {
                    initializeRedis();
                }
                return objSinglePool != null ? objSinglePool.getResource() : null;
            }
        } catch (JedisException e) {
            log.error("Error retrieving Redis connection: ", e);
        }
        return null;
    }


    public static String getRedisHandlerVersion() {
        return "Redis Handler v0.0.1 - 7th February 2025";
    }

    public void close() {
        try {
            if (objSentinelPool != null) {
                objSentinelPool.close();
                log.info("Closed JedisSentinelPool.");
            }
            if (objSinglePool != null) {
                objSinglePool.close();
                log.info("Closed JedisPool.");
            }
        } catch (JedisException e) {
            log.error("Error while closing Redis connection pools: ", e);
        }
    }
}