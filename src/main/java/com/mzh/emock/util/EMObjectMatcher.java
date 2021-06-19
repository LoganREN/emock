package com.mzh.emock.util;

import java.lang.reflect.Field;
import java.util.*;

public class EMObjectMatcher {
    private static Object[] hasRead = new Object[500];
    private static int curr=0;
    private static Object currentTarget=null;

    private final Map<Object,List<FieldInfo>> holdingObject=new EMObjectMap<>();
    private boolean hasRead(Object o){
        for(int i=0;i<curr;i++){
            if(o==hasRead[i]){
                return true;
            }
        }
        return false;
    }
    private void addRead(Object o){
        if(curr==hasRead.length){
            hasRead=Arrays.copyOf(hasRead,hasRead.length*2);
        }
        if(curr % 50000==0)
            System.out.println("EM-ObjectMatcher Handling : currTarget:"+currentTarget+",handleCount:"+curr+"-"+(curr+50000)+"...");
        hasRead[curr]=o;
        curr++;
    }

    private EMObjectMatcher() {
    }

    public static class FieldInfo {
        public FieldInfo(int index) {
            this.index = index;
            this.isArrayIndex = true;
        }

        public FieldInfo(Field field) {
            this.nativeField = field;
            this.isArrayIndex = false;
        }

        private final boolean isArrayIndex;
        private Field nativeField;
        private int index;

        public boolean isArrayIndex() {
            return isArrayIndex;
        }

        public Field getNativeField() {
            return nativeField;
        }

        public int getIndex() {
            return index;
        }
    }

    public static Map<Object,List<FieldInfo>> match(Object src, Object target) {
        if(target!=currentTarget){
            hasRead=new Object[500];
            curr=0;
            currentTarget=target;
        }
        EMObjectMatcher result = new EMObjectMatcher();
        result.getAllDeclaredFieldsHierarchy(src, result.holdingObject, target);
        return result.holdingObject;
    }


    private void getAllDeclaredFieldsHierarchy(Object src, Map<Object, List<FieldInfo>> holdingObject, Object target) {
        if (src == null || hasRead(src)) {
            return;
        }
        try {
            addRead(src);
            List<Field> fields = getAllDeclaredFields(src.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(src);
                if (value == null) {
                    continue;
                }
                if (field.getType().isArray()) {
                    findInArray((Object[]) value, holdingObject, target);
                    continue;
                }
                if (value == target) {
                    if (holdingObject.get(src) == null)
                        holdingObject.computeIfAbsent(src, k -> new ArrayList<>());
                    holdingObject.get(src).add(new FieldInfo(field));
                }
                getAllDeclaredFieldsHierarchy(value, holdingObject, target);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void findInArray(Object[] src, Map<Object, List<FieldInfo>> holdingObject, Object target) {
        if (src == null || hasRead(src)) {
            return;
        }
        addRead(src);
        for (int i = 0; i < src.length; i++) {
            Object value = src[i];
            if (value == null) {
                continue;
            }
            if (value.getClass().isArray() && isReferenceField(value.getClass())) {
                findInArray((Object[]) value, holdingObject, target);
                continue;
            }
            if (value == target) {
                if (holdingObject.get(src) == null)
                    holdingObject.computeIfAbsent(src, k -> new ArrayList<>());
                holdingObject.get(src).add(new FieldInfo(i));
            }
            getAllDeclaredFieldsHierarchy(value, holdingObject, target);
        }
    }


    private boolean isReferenceField(Class<?> type) {
        while(type.isArray()){
            type=type.getComponentType();
        }
        return !type.isEnum() && !type.isPrimitive() && type != String.class
                && type != Character.class && type != Boolean.class
                && type != Byte.class && type != Short.class && type != Integer.class && type != Long.class
                && type != Float.class && type != Double.class;
    }

    private List<Field> getAllDeclaredFields(Class<?> clz) {
        List<Field> res = new ArrayList<>();
        EMUtil.optWithParent(clz, c -> {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                if (isReferenceField(field.getType())) {
                    res.add(field);
                }
            }
        });
        return res;
    }

}
