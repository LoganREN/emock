package com.mzh.emock.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ESet<K> implements Set<K> {
    private int initSize=16;
    private int curr=0;
    private K[] ks;
    @Override
    public int size() {
        return curr;
    }

    @Override
    public boolean isEmpty() {
        return curr==0;
    }

    @Override
    public boolean contains(Object o) {
        if(ks==null){
            return false;
        }
        for(int i=0;i<curr;i++){
            if(ks[i]==o){
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            private int i=0;
            @Override
            public boolean hasNext() {
                return i<curr;
            }

            @Override
            public K next() {
                K k=ks[i];
                i++;
                return k;
            }
        };
    }

    @Override
    public Object[] toArray() {
        return ks;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(K k) {
        if(ks==null){
            ks=(K[])new Object[initSize];
        }
        if(curr==ks.length){
            ks= Arrays.copyOf(ks,ks.length*2);
        }
        ks[curr]=k;
        curr++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        for(K k:c){
            add(k);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Spliterator<K> spliterator() {
        return Set.super.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super K> filter) {
        return Set.super.removeIf(filter);
    }

    @Override
    public Stream<K> stream() {
        return Set.super.stream();
    }

    @Override
    public Stream<K> parallelStream() {
        return Set.super.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super K> action) {
        Set.super.forEach(action);
    }
}
