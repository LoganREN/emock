package com.mzh.emock.util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EObjectMap<K,V> implements Map<K,V> {
    private int initSize=16;
    private int curr=0;
    private KV<K,V>[] kv;

    private class KV<K,V>{
        public KV(K k, V v) {
            this.k = k;
            this.v = v;
        }

        private K k;
        private V v;

        public K getK() {
            return k;
        }

        public void setK(K k) {
            this.k = k;
        }

        public V getV() {
            return v;
        }

        public void setV(V v) {
            this.v = v;
        }
    }


    @Override
    public int size() {
        return curr;
    }

    @Override
    public boolean isEmpty() {
        return curr==0;
    }

    @Override
    public boolean containsKey(Object key) {
        if(kv==null){
            return false;
        }
        for(int i=0;i<curr;i++){
            if(key==kv[i].getK()){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if(kv==null){
            return false;
        }
        for(int i=0;i<curr;i++){
            if(value==kv[i].getV()){
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        if(kv==null){
            return null;
        }
        for (int i=0;i<curr;i++) {
            if (kv[i].getK() == key) {
                return kv[i].getV();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        if(kv==null){
           kv=new KV[initSize];
        }
        if(curr==kv.length){
            kv= Arrays.copyOf(kv,kv.length*2);
        }
        for(int i=0;i<curr;i++){
            if(kv[i].getK()==key){
                kv[i].setV(value);
                return value;
            }
        }
        KV<K,V> o=new KV<>(key,value);
        kv[curr]=o;
        curr++;
        return value;
    }

    @Override
    public V remove(Object key) {
        System.out.println("not support remove");
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(K key:m.keySet()){
            put(key,m.get(key));
        }
    }

    @Override
    public void clear() {
        this.curr=0;
        this.kv=null;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set=new ESet<>();
        for(int i=0;i<curr;i++){
            set.add(kv[i].getK());
        }
        return set;
    }

    @Override
    public Collection<V> values() {
       Collection<V> cc=new ArrayList<>();
       for(int i=0;i<curr;i++){
            cc.add(kv[i].getV());
       }
       return cc;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        System.out.println("un support entrySet");
        return null;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Map.super.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Map.super.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return Map.super.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return Map.super.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return Map.super.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Map.super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Map.super.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Map.super.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return Map.super.merge(key, value, remappingFunction);
    }
}
