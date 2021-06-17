package com.mzh.emock.util;

import java.lang.reflect.Field;
import java.util.*;

public class EObjectMatcher {
    private static Object[] hasRead = new Object[500];
    private static int curr=0;
    private static Object currentTarget=null;

    private final Map<Object,List<FieldDescription>> holdingObject=new EObjectMap<>();
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
        if(curr % 10000==0)
            System.out.println(curr);
        hasRead[curr]=o;
        curr++;
    }

    private EObjectMatcher() {
    }

    public static class FieldDescription {
        public FieldDescription(int index) {
            this.index = index;
            this.isArrayIndex = true;
        }

        public FieldDescription(Field field) {
            this.nativeField = field;
            this.isArrayIndex = false;
        }

        private boolean isArrayIndex;
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

    public static Map<Object,List<FieldDescription>> match(Object src, Object target) throws Exception {
        if(target!=currentTarget){
            hasRead=new Object[500];
            curr=0;
        }
        EObjectMatcher result = new EObjectMatcher();
        result.getAllDeclaredFieldsHierarchy(src, result.holdingObject, target);
        return result.holdingObject;
    }


    private void getAllDeclaredFieldsHierarchy(Object src, Map<Object, List<FieldDescription>> holdingObject, Object target) throws Exception {
        if (src == null || hasRead(src)) {
            return;
        }
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
                holdingObject.get(src).add(new FieldDescription(field));
            }
            getAllDeclaredFieldsHierarchy(value, holdingObject, target);
        }
    }

    private void findInArray(Object[] src, Map<Object, List<FieldDescription>> holdingObject, Object target) throws Exception {
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
                holdingObject.get(src).add(new FieldDescription(i));
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
        EMockUtil.optWithParent(clz, c -> {
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
