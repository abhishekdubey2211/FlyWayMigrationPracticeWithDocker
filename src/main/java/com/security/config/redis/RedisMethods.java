package com.security.config.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Set;

import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;

@Component
public class RedisMethods {

    private static final Logger log = LoggerFactory.getLogger(RedisMethods.class);
    private static final RedisConfiguration redisConn = new RedisConfiguration(); // Singleton
    private static final long DEFAULT_EXPIRY = 24 * 60 * 60; // 24 hours in seconds
    static Gson gson = new Gson();
    // ====== Utility Methods ======

    private static String convertObjToString(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T convertStringToObj(String jsonString, Class<T> clazz) {
        Type type = TypeToken.get(clazz).getType();
        return  gson.fromJson(jsonString, type);
    }

    public static <T> T convertStringToObjType(String jsonString, TypeToken<T> typeToken) {
        return gson.fromJson(jsonString, typeToken.getType());
    }

    // ====== Flush All Data ======

    public static void flushAll() {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("flushAll :: Redis connection not established.");
                return ;
            }
            jedis.flushAll();
        } catch (JedisException e) {
            log.error("Error flushing Redis: {}", e.getMessage(), e);
        }
    }

    public static String getRedisData(String redisKey) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("getRedisData :: Redis connection not established.");
                return null;
            }
            return jedis.get(redisKey);
        } catch (Exception e) {
            log.error("getRedisData :: Error retrieving data for key {}: {}", redisKey, e.getMessage(), e);
            return null;
        }
    }

    public static void setRedisData(String redisKey, String json) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("setRedisData :: Redis connection not established.");
                return;
            }
            jedis.set(redisKey, json);
        } catch (Exception e) {
            log.error("setRedisData :: Error setting data for key {}: {}", redisKey, e.getMessage(), e);
        }
    }

    // String Operations
    public static void setRedisData(String key, String value, Long expiryInSeconds) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("setRedisData :: Redis connection not established.");
                return;
            }
            jedis.set(key, value);
            if (expiryInSeconds != null) {
                jedis.expire(key, expiryInSeconds);
            } else {
                jedis.expire(key, DEFAULT_EXPIRY);
            }
        } catch (JedisException e) {
            System.err.println("Error setting Data: " + e.getMessage());
        }
    }

    public static void delRedisData(String redisKey) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("delRedisData :: Redis connection not established.");
                return;
            }
            jedis.del(redisKey);
        } catch (Exception e) {
            log.error("delRedisData :: Error deleting key {}: {}", redisKey, e.getMessage(), e);
        }
    }

    public static String getMemoryValue(String redisKey, String field) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("getMemoryValue :: Redis connection not established.");
                return null;
            }
            return jedis.hget(redisKey, field);
        } catch (Exception e) {
            log.error("getMemoryValue :: Error retrieving hash field {} for key {}: {}", field, redisKey, e.getMessage(), e);
            return null;
        }
    }

    public static  void setMemoryValue(String redisKey, String field, String value) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("setMemoryValue :: Redis connection not established.");
                return;
            }
            jedis.hset(redisKey, field, value);
        } catch (Exception e) {
            log.error("setMemoryValue :: Error setting hash field {} for key {}: {}", field, redisKey, e.getMessage(), e);
        }
    }

    // Hash Operations
    public static void setMemoryValue(String hashKey, String field, String value, int expirySeconds) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("setMemoryValue :: Redis connection not established.");
                return;
            }
            jedis.hset(hashKey, field, value);
            jedis.expire(hashKey, expirySeconds); // Set expiry time
        } catch (JedisException e) {
            System.err.println("Error setting hash field: " + e.getMessage());
        }
    }

    public static Map<String, String> getRedisHData(String redisKey) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("getRedisHData :: Redis connection not established.");
                return null;
            }
            return jedis.hgetAll(redisKey);
        } catch (Exception e) {
            log.error("getRedisHData :: Error retrieving hash data for key {}: {}", redisKey, e.getMessage(), e);
            return null;
        }
    }

    public static void delRedisHData(String hashKey, String... fields) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("getRedisHData :: Redis connection not established.");
                return;
            }
            jedis.hdel(hashKey, fields);
        } catch (JedisException e) {
            System.err.println("Error deleting hash fields: " + e.getMessage());
        }
    }

    public static boolean isMemoryFieldExists(String hashKey, String field) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("isMemoryFieldExists :: Redis connection not established.");
                return false;
            }
            return jedis.hexists(hashKey, field);
        } catch (JedisException e) {
            System.err.println("Error checking existence of hash field: " + e.getMessage());
            return false;
        }
    }

    // List Operations
    // Set a specific index in the list
    public static boolean lset(String key, long index, String value) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("lset :: Redis connection not established.");
                return false;
            }
            jedis.lset(key, index, value);
            return true;
        } catch (JedisException e) {
            log.error("lset :: Error setting list element at index {}: {}", index, e.getMessage(), e);
            return false;
        }
    }

    // Get an element at a specific index
    public static String lindex(String key, long index) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("lindex :: Redis connection not established.");
                return null;
            }
            return jedis.lindex(key, index);
        } catch (JedisException e) {
            log.error("lindex :: Error getting list element at index {}: {}", index, e.getMessage(), e);
            return null;
        }
    }

    // Add element(s) to the beginning of the list
    public static boolean lpush(String key, String... values) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("lpush :: Redis connection not established.");
                return false;
            }
            jedis.lpush(key, values);
            return true;
        } catch (JedisException e) {
            log.error("lpush :: Error pushing values to list {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    // Add element(s) to the end of the list
    public static boolean rpush(String key, String... values) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("rpush :: Redis connection not established.");
                return false;
            }
            jedis.rpush(key, values);
            return true;
        } catch (JedisException e) {
            log.error("rpush :: Error appending values to list {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    // Remove and return the first element of the list
    public static String lpop(String key) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("lpop :: Redis connection not established.");
                return null;
            }
            return jedis.lpop(key);
        } catch (JedisException e) {
            log.error("lpop :: Error popping first element from list {}: {}", key, e.getMessage(), e);
            return null;
        }
    }

    // Remove and return the last element of the list
    public static String rpop(String key) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("rpop :: Redis connection not established.");
                return null;
            }
            return jedis.rpop(key);
        } catch (JedisException e) {
            log.error("rpop :: Error popping last element from list {}: {}", key, e.getMessage(), e);
            return null;
        }
    }

    // Retrieve a range of elements from the list
    public static List<String> lrange(String key, long start, long end) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("lrange :: Redis connection not established.");
                return Collections.emptyList();
            }
            return jedis.lrange(key, start, end);
        } catch (JedisException e) {
            log.error("lrange :: Error retrieving list range from {} to {} for key {}: {}", start, end, key, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // Trim the list to a specified range
    public static boolean ltrim(String key, long start, long end) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("ltrim :: Redis connection not established.");
                return false;
            }
            jedis.ltrim(key, start, end);
            return true;
        } catch (JedisException e) {
            log.error("ltrim :: Error trimming list {} from {} to {}: {}", key, start, end, e.getMessage(), e);
            return false;
        }
    }

    // Remove occurrences of a value from the list
    public static boolean lrem(String key, long count, String value) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("lrem :: Redis connection not established.");
                return false;
            }
            jedis.lrem(key, count, value);
            return true;
        } catch (JedisException e) {
            log.error("lrem :: Error removing value {} from list {}: {}", value, key, e.getMessage(), e);
            return false;
        }
    }

    // Subscribe to a channel asynchronously
    public static void subscribe(String channel) {
        new Thread(() -> {
            try (Jedis jedis = redisConn.getRedisConn()) {
                if (jedis == null) {
                    log.error("subscribe :: Redis connection not established.");
                    return;
                }
                log.info("Subscribed to channel: {}", channel);
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String ch, String message) {
                        log.info("Received message on channel {}: {}", ch, message);
                        removeMessageFromList(ch, message); // Auto-remove after reading
                    }
                }, channel);
            } catch (JedisException e) {
                log.error("Error subscribing to channel {}: {}", channel, e.getMessage(), e);
            }
        }).start();
    }

    // Publish a message and store it in Redis
    public static boolean publish(String channel, String message) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("publish :: Redis connection not established.");
                return false;
            }
            jedis.publish(channel, message);
            storeMessageInList(channel, message);
            return true;
        } catch (JedisException e) {
            log.error("Error publishing message to channel {}: {}", channel, e.getMessage(), e);
            return false;
        }
    }

    // Store a message in Redis list
    private static void storeMessageInList(String channel, String message) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis != null) {
                jedis.lpush("MESSAGE_STORE#" + channel, message);
                log.info("Stored message in channel {}: {}", channel, message);
            }
        } catch (JedisException e) {
            log.error("Error storing message in channel {}: {}", channel, e.getMessage(), e);
        }
    }

    // Remove a specific message from the list (used in subscribe)
    private static void removeMessageFromList(String channel, String message) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("removeMessage :: Redis connection not established.");
                return;
            }
            jedis.lrem("MESSAGE_STORE#" + channel, 1, message);
            log.info("Removed message from channel {}: {}", channel, message);
        } catch (JedisException e) {
            log.error("Error removing message from channel {}: {}", channel, e.getMessage(), e);
        }
    }

    // Remove a channel and its stored messages
    public static boolean removeChannel(String channel) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("removeChannel :: Redis connection not established.");
                return false;
            }
            Long deletedMessages = jedis.del("MESSAGE_STORE#" + channel);
            log.info("Removed channel {} with {} stored messages", channel, deletedMessages);
            return true;
        } catch (JedisException e) {
            log.error("Error removing channel {}: {}", channel, e.getMessage(), e);
            return false;
        }
    }

    // Get list of all active channels
    public static Set<String> getAllChannels() {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("getAllChannels :: Redis connection not established.");
                return Collections.emptySet();
            }
            return new HashSet<>(jedis.pubsubChannels("*"));
        } catch (JedisException e) {
            log.error("getAllChannels :: Error retrieving channel list: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    // Retrieve stuck messages and remove them after reading
    public static List<String> getStuckMessages(String channel, int limit) {
        try (Jedis jedis = redisConn.getRedisConn()) {
            if (jedis == null) {
                log.error("getStuckMessages :: Redis connection not established.");
                return Collections.emptyList();
            }
            List<String> messages = jedis.lrange("MESSAGE_STORE#" + channel, 0, limit - 1);
            if (!messages.isEmpty()) {
                jedis.ltrim("MESSAGE_STORE#" + channel, messages.size(), -1);
                log.info("Deleted {} messages from channel {}", messages.size(), channel);
            }
            return messages;
        } catch (JedisException e) {
            log.error("getStuckMessages :: Error retrieving messages for channel {}: {}", channel, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static void main(String[] args) {
        // Setting and retrieving data
        setRedisData("ABHISHEK", "HELLO WORLD");
        System.out.println("Stored Data: " + getRedisData("ABHISHEK"));

        // Setting hash data
        setMemoryValue("User:1001", "Name", "Abhishek");
        setMemoryValue("User:1001", "Email", "abdubey42@gmail.com");

        // Retrieving hash data
        System.out.println("User Data: " + getRedisHData("User:1001"));

        // List operations
        lset("UserList", 0, "User1");
        lset("UserList", 1, "User2");
        System.out.println("List Element at Index 0: " + lindex("UserList", 0));

        // Start subscriber in a separate thread
        new Thread(() -> subscribe("COUNT")).start();

        // Publish messages
        for (int i = 0; i < 500; i++) {
            publish("COUNT", "Message " + i);
        }

        // Deleting data
        delRedisData("ABHISHEK");
        System.out.println("Data after deletion: " + getRedisData("ABHISHEK"));
        System.out.println("All chanels : " + getAllChannels());
    }
}