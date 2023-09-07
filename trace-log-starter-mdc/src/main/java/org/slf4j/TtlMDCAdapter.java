package org.slf4j;

import ch.qos.logback.classic.util.LogbackMDCAdapter;
import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.helpers.ThreadLocalMapOfStacks;

import java.util.*;

/**
 *重构{@link LogbackMDCAdapter}类，搭配TransmittableThreadLocal实现父子线程之间的数据传递
 *
 */
public class TtlMDCAdapter implements MDCAdapter {
    final ThreadLocal<Map<String, String>> readWriteThreadLocalMap = new TransmittableThreadLocal();
    final ThreadLocal<Map<String, String>> readOnlyThreadLocalMap = new TransmittableThreadLocal();
    private final ThreadLocalMapOfStacks threadLocalMapOfDeques = new ThreadLocalMapOfStacks();

    private static TtlMDCAdapter mtcMDCAdapter;

    static {
        mtcMDCAdapter = new TtlMDCAdapter();
        MDC.mdcAdapter = mtcMDCAdapter;
    }

    public static MDCAdapter getInstance() {
        return mtcMDCAdapter;
    }
    public void put(String key, String val) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        } else {
            Map<String, String> current = (Map)this.readWriteThreadLocalMap.get();
            if (current == null) {
                current = new HashMap();
                this.readWriteThreadLocalMap.set(current);
            }

            ((Map)current).put(key, val);
            this.nullifyReadOnlyThreadLocalMap();
        }
    }

    public String get(String key) {
        Map<String, String> hashMap = (Map)this.readWriteThreadLocalMap.get();
        return hashMap != null && key != null ? (String)hashMap.get(key) : null;
    }

    public void remove(String key) {
        if (key != null) {
            Map<String, String> current = (Map)this.readWriteThreadLocalMap.get();
            if (current != null) {
                current.remove(key);
                this.nullifyReadOnlyThreadLocalMap();
            }

        }
    }

    private void nullifyReadOnlyThreadLocalMap() {
        this.readOnlyThreadLocalMap.set(null);
    }

    public void clear() {
        this.readWriteThreadLocalMap.set(null);
        this.nullifyReadOnlyThreadLocalMap();
    }

    public Map<String, String> getPropertyMap() {
        Map<String, String> readOnlyMap = (Map)this.readOnlyThreadLocalMap.get();
        if (readOnlyMap == null) {
            Map<String, String> current = (Map)this.readWriteThreadLocalMap.get();
            if (current != null) {
                Map<String, String> tempMap = new HashMap(current);
                readOnlyMap = Collections.unmodifiableMap(tempMap);
                this.readOnlyThreadLocalMap.set(readOnlyMap);
            }
        }

        return readOnlyMap;
    }

    public Map getCopyOfContextMap() {
        Map<String, String> readOnlyMap = this.getPropertyMap();
        return readOnlyMap == null ? null : new HashMap(readOnlyMap);
    }

    public Set<String> getKeys() {
        Map<String, String> readOnlyMap = this.getPropertyMap();
        return readOnlyMap != null ? readOnlyMap.keySet() : null;
    }

    public void setContextMap(Map contextMap) {
        if (contextMap != null) {
            this.readWriteThreadLocalMap.set(new HashMap(contextMap));
        } else {
            this.readWriteThreadLocalMap.set(null);
        }

        this.nullifyReadOnlyThreadLocalMap();
    }

    public void pushByKey(String key, String value) {
        this.threadLocalMapOfDeques.pushByKey(key, value);
    }

    public String popByKey(String key) {
        return this.threadLocalMapOfDeques.popByKey(key);
    }

    public Deque<String> getCopyOfDequeByKey(String key) {
        return this.threadLocalMapOfDeques.getCopyOfDequeByKey(key);
    }

    public void clearDequeByKey(String key) {
        this.threadLocalMapOfDeques.clearDequeByKey(key);
    }
}
