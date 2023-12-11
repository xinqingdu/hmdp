package indiv.hmdp.utils;

import jdk.nashorn.internal.runtime.ECMAException;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author
 * @date 2023/12/10 14 35
 * discription
 */

public class ConcurrentHashMap<K, V> implements Map<K, V> {
    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return size.get() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        if (this.lock.isLocked()) {
            try {
//                Thread.sleep(20);
                return get(key);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (key == null) return null;
        int pos = hash(key);
        NodeLock nodeLock = map[pos];
        if (nodeLock == null) return null;
        try {
            nodeLock.lock.lock();
            Node node = nodeLock.head;
            if (nodeLock.head == null) {
                return null;
            }
            if (nodeLock.head.next == null) {
                return node.v;
            }

            while (node != null && !node.k.equals(key)) {
                node = node.next;
            }
            return node == null ? null : node.v;
        } finally {
            nodeLock.lock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        if (this.lock.isLocked()) {
            try {
                Thread.sleep(20);
                return put(key, value);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (key == null) {
            throw new RuntimeException("key not be null");
        }
        int pos = hash(key);
        NodeLock nodeLock = map[pos];
        if (nodeLock == null) map[pos] = new NodeLock();
        nodeLock = map[pos];
        try {
            nodeLock.lock.lock();
            int size = innerPut(key, value);
            if (size > 8) {
                rehash();
            }
            return value;
        } finally {
            nodeLock.lock.unlock();
        }

    }

    private int innerPut(K key, V value) {
        int pos = hash(key);
        NodeLock nodeLock = map[pos];
        if (nodeLock == null) map[pos] = new NodeLock();
        nodeLock = map[pos];
        if (nodeLock == null) map[pos] = new NodeLock();
        if (nodeLock.head == null) {
            nodeLock.head = new Node(key, value);
            nodeLock.tail = nodeLock.head;
            nodeLock.size++;
            size.incrementAndGet();
            return nodeLock.size;
        }
        Node node = nodeLock.head;
        while (node != null && !node.k.equals(key)) {
            node = node.next;
        }
        if (node == null) {
            nodeLock.tail.next = new Node(key, value);
            nodeLock.tail = nodeLock.tail.next;
            nodeLock.size++;
            size.incrementAndGet();
            return nodeLock.size;
        }
        node.v = value;
        return nodeLock.size;
    }

    private void innerBatchPut(Node head) {
        if (head == null) return;
        Node node = head;
        while (node != null) {
            innerPut(node.k, node.v);
            node = node.next;
        }
    }

    @Override
    public V remove(Object key) {
        if (this.lock.isLocked()) {
            try {
                Thread.sleep(20);
                return remove(key);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (key == null) {
            throw new RuntimeException("key not be null");
        }
        int pos = hash(key);
        NodeLock nodeLock = map[pos];
        if (nodeLock == null) return null;
        try {
            nodeLock.lock.lock();
            Node node = nodeLock.head;
            if (node == null) {
                return null;
            }
            if (node.k.equals(key)) {
                V value = node.v;
                nodeLock.head = node.next;
                nodeLock.tail = node.next;
                nodeLock.size--;
                size.decrementAndGet();
                return value;
            }
            while (node.next != null && !node.next.k.equals(key)) {
                node = node.next;
            }
            if (node.next == null) {
                return null;
            }
            V value = node.next.v;
            if (nodeLock.tail == node.next) {
                nodeLock.tail = node;
            }
            node.next = node.next.next;
            nodeLock.size--;
            size.decrementAndGet();
            return value;
        } finally {
            nodeLock.lock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (K key : m.keySet()) {
            V value = m.get(key);
            put(key, value);
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    class Node {
        K k;
        V v;
        Node next;

        public Node(K k, V v) {
            this.k = k;
            this.v = v;
        }
    }


    NodeLock[] map;

    ReentrantLock lock;

    AtomicInteger size;

    static int defaultSize = 8;

    public ConcurrentHashMap() {
        this.map = new NodeLock[defaultSize];
        size = new AtomicInteger(0);
        lock = new ReentrantLock();
        for (int i = 0; i < map.length; i++) {
            map[i] = new NodeLock();
        }
    }


    public int hash(Object k) {
        return k.hashCode() & (map.length - 1);
    }

    public void rehash() {
        try {
            this.lock.lock();
//            Thread.sleep(50);

            NodeLock[] map1 = new NodeLock[map.length];
            System.arraycopy(map, 0, map1, 0, map.length);
            map = new NodeLock[map.length << 1];
            size.set(0);
            for (int i = 0; i < map.length; i++) {
                map[i] = new NodeLock();
            }
            for (NodeLock nodeLock :
                    map1) {
                Node node = nodeLock.head;
                if (node == null) continue;
                int pos = hash(node.k);
                NodeLock newLock = map[pos];
                try {
                    newLock.lock.lock();
                    innerBatchPut(node);
                } finally {
                    newLock.lock.unlock();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.lock.unlock();
        }
    }

    public NodeLock[] getMap() {
        return map;
    }
}

class NodeLock {
    ConcurrentHashMap.Node head;
    ConcurrentHashMap.Node tail;
    int size;
    ReentrantLock lock;

    public NodeLock() {
        lock = new ReentrantLock();
        size = 0;
    }

}

class Test {
    public static void main(String[] args) {
        ConcurrentHashMap<String, Integer> myMap = new ConcurrentHashMap<>();
        int k = 5;
        int data = 1000;
        CountDownLatch countDownLatch = new CountDownLatch(k);
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int j = finalI * data; j < (finalI + 1) * data; j++) {
                    myMap.put(String.valueOf(j), j);
                }
                countDownLatch.countDown();
            }).start();
        }
        try {
            boolean await = countDownLatch.await(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        for (int i = 0; i < 1000; i++) {
            System.out.print(myMap.remove(String.valueOf(i))+" ");
        }
        System.out.println();
        NodeLock[] map1 = myMap.getMap();
        System.out.println("length: " + map1.length);
        System.out.println("size: " + myMap.size.get());
        for (int i = 0; i < map1.length; i++) {
            ConcurrentHashMap.Node node = map1[i].head;
            while (node != null) {
                System.out.print("{key:" + node.k + " value:" + myMap.get(node.k) + "}, ");
                node = node.next;
            }
            System.out.println();
        }
    }
}