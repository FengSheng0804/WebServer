package server.proxyServer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// CachedObject类用于封装缓存内容、过期时间和最后访问时间
class CachedObject {

    // 缓存内容
    private byte[] content;
    // 过期时间
    private long expirationTime;
    // 最后访问时间
    private long lastAccessTime;

    // 构造函数
    public CachedObject(byte[] content, long expirationTime) {
        // 初始化缓存内容
        this.content = content;
        // 初始化过期时间
        this.expirationTime = expirationTime;
        // 初始化最后访问时间为当前时间
        this.lastAccessTime = System.currentTimeMillis();
    }

    // 获取缓存内容
    public byte[] getContent() {
        // 更新最后访问时间为当前时间
        lastAccessTime = System.currentTimeMillis();
        return content;
    }

    // 判断缓存是否过期
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}

// ProxyServerCache类用于实现代理服务器的缓存机制
public class ProxyServerCache {
    // 缓存存储
    private ConcurrentHashMap<String, CachedObject> cache = new ConcurrentHashMap<>();
    // 1小时的毫秒数，作为默认过期时间
    private static final int DEFAULT_EXPIRATION_TIME = 3600000;
    // 最大缓存数量，可根据实际调整
    private static final int MAX_CACHE_SIZE = 100;

    // 构造函数
    public ProxyServerCache() {
        // 创建一个定时任务线程池
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // 定时清理过期缓存
        executor.scheduleAtFixedRate(() -> {
            // 移除所有已过期的缓存项
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 1, 1, TimeUnit.HOURS); // 每小时执行一次
    }

    // 添加缓存项
    public void put(String key, byte[] content) {
        // 设置过期时间
        long expirationTime = System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME;

        // 如果缓存大小超过最大限制
        if (cache.size() >= MAX_CACHE_SIZE) {
            // 获取最早的缓存项
            String eldestKey = cache.keySet().iterator().next();
            // 移除最早的缓存项
            cache.remove(eldestKey);
        }

        // 添加新的缓存项
        cache.put(key, new CachedObject(content, expirationTime));
    }

    // 获取缓存内容
    public byte[] get(String key) {
        // 获取缓存项
        CachedObject cachedObject = cache.get(key);
        // 如果缓存项存在且未过期
        if (cachedObject != null && !cachedObject.isExpired()) {
            // 返回缓存内容
            return cachedObject.getContent();
        }
        // 缓存不存在或已过期，返回null
        return null;
    }

    // 移除缓存项
    public void remove(String key) {
        cache.remove(key);
    }

    // 检测缓存中是否包含指定的键
    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }
}
