package server.proxyServer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// CachedObject�����ڷ�װ�������ݡ�����ʱ���������ʱ��
class CachedObject {

    // ��������
    private byte[] content;
    // ����ʱ��
    private long expirationTime;
    // ������ʱ��
    private long lastAccessTime;

    // ���캯��
    public CachedObject(byte[] content, long expirationTime) {
        // ��ʼ����������
        this.content = content;
        // ��ʼ������ʱ��
        this.expirationTime = expirationTime;
        // ��ʼ��������ʱ��Ϊ��ǰʱ��
        this.lastAccessTime = System.currentTimeMillis();
    }

    // ��ȡ��������
    public byte[] getContent() {
        // ����������ʱ��Ϊ��ǰʱ��
        lastAccessTime = System.currentTimeMillis();
        return content;
    }

    // �жϻ����Ƿ����
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}

// ProxyServerCache������ʵ�ִ���������Ļ������
public class ProxyServerCache {
    // ����洢
    private ConcurrentHashMap<String, CachedObject> cache = new ConcurrentHashMap<>();
    // 1Сʱ�ĺ���������ΪĬ�Ϲ���ʱ��
    private static final int DEFAULT_EXPIRATION_TIME = 3600000;
    // ��󻺴��������ɸ���ʵ�ʵ���
    private static final int MAX_CACHE_SIZE = 100;

    // ���캯��
    public ProxyServerCache() {
        // ����һ����ʱ�����̳߳�
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // ��ʱ������ڻ���
        executor.scheduleAtFixedRate(() -> {
            // �Ƴ������ѹ��ڵĻ�����
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 1, 1, TimeUnit.HOURS); // ÿСʱִ��һ��
    }

    // ��ӻ�����
    public void put(String key, byte[] content) {
        // ���ù���ʱ��
        long expirationTime = System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME;

        // ��������С�����������
        if (cache.size() >= MAX_CACHE_SIZE) {
            // ��ȡ����Ļ�����
            String eldestKey = cache.keySet().iterator().next();
            // �Ƴ�����Ļ�����
            cache.remove(eldestKey);
        }

        // ����µĻ�����
        cache.put(key, new CachedObject(content, expirationTime));
    }

    // ��ȡ��������
    public byte[] get(String key) {
        // ��ȡ������
        CachedObject cachedObject = cache.get(key);
        // ��������������δ����
        if (cachedObject != null && !cachedObject.isExpired()) {
            // ���ػ�������
            return cachedObject.getContent();
        }
        // ���治���ڻ��ѹ��ڣ�����null
        return null;
    }

    // �Ƴ�������
    public void remove(String key) {
        cache.remove(key);
    }

    // ��⻺�����Ƿ����ָ���ļ�
    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }
}
