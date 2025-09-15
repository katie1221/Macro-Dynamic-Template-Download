package com.example.downloadtemplatevb.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonUtils {
    private static JsonUtils allJsonUtils;
    private static JsonUtils notNullJsonUtils;
    private static JsonUtils notDefJsonUtils;
    private static JsonUtils notEmpJsonUtils;
    private ObjectMapper mapper = new ObjectMapper();
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static JsonUtils getAllJsonUtils() {
        return allJsonUtils;
    }

    public static void setAllJsonUtils(JsonUtils allJsonUtils) {
        JsonUtils.allJsonUtils = allJsonUtils;
    }

    public static JsonUtils getNotNullJsonUtils() {
        return notNullJsonUtils;
    }

    public static void setNotNullJsonUtils(JsonUtils notNullJsonUtils) {
        JsonUtils.notNullJsonUtils = notNullJsonUtils;
    }

    public static JsonUtils getNotDefJsonUtils() {
        return notDefJsonUtils;
    }

    public static void setNotDefJsonUtils(JsonUtils notDefJsonUtils) {
        JsonUtils.notDefJsonUtils = notDefJsonUtils;
    }

    public static JsonUtils getNotEmpJsonUtils() {
        return notEmpJsonUtils;
    }

    public static void setNotEmpJsonUtils(JsonUtils notEmpJsonUtils) {
        JsonUtils.notEmpJsonUtils = notEmpJsonUtils;
    }

    public ObjectMapper getMapper() {
        return this.mapper;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonUtils(Include include) {
        this.mapper.setSerializationInclusion(include);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        switch (include) {
            case NON_NULL:
            case NON_EMPTY:
                this.mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
            default:
                SimpleModule module = new SimpleModule();
                module.addDeserializer(Date.class, new CustomDateDeserializer());
                this.mapper.registerModule(module);
                this.setDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    }

    public static JsonUtils buildNormalBinder() {
        Class var0 = JsonUtils.class;
        synchronized(JsonUtils.class) {
            if (allJsonUtils == null) {
                allJsonUtils = new JsonUtils(Include.ALWAYS);
            }
        }

        return allJsonUtils;
    }

    public static JsonUtils buildNonNullBinder() {
        Class var0 = JsonUtils.class;
        synchronized(JsonUtils.class) {
            if (notNullJsonUtils == null) {
                notNullJsonUtils = new JsonUtils(Include.NON_NULL);
            }
        }

        return notNullJsonUtils;
    }

    public static JsonUtils buildNonDefaultBinder() {
        Class var0 = JsonUtils.class;
        synchronized(JsonUtils.class) {
            if (notDefJsonUtils == null) {
                notDefJsonUtils = new JsonUtils(Include.NON_DEFAULT);
            }
        }

        return notDefJsonUtils;
    }

    public static JsonUtils buildNonEmptyBinder() {
        Class var0 = JsonUtils.class;
        synchronized(JsonUtils.class) {
            if (notEmpJsonUtils == null) {
                notEmpJsonUtils = new JsonUtils(Include.NON_EMPTY);
            }
        }

        return notEmpJsonUtils;
    }

    public <T> T getJsonToObject(String json, Class<T> clazz) {
        T object = null;
        if (StringUtils.isNotBlank(json)) {
            try {
                object = this.getMapper().readValue(json, clazz);
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        return object;
    }

    public Object getJsonToList(String json, Class clazz) {
        Object object = null;
        if (StringUtils.isNotBlank(json)) {
            try {
                object = this.getMapper().readValue(json, TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, clazz));
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        return object;
    }

    public <T, P extends List<T>> List<T> getList(String json, Class<P> collectionType, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        } else {
            try {
                return (List)this.getMapper().readValue(json, TypeFactory.defaultInstance().constructCollectionType(collectionType, clazz));
            } catch (IOException var5) {
                var5.printStackTrace();
                return Collections.emptyList();
            }
        }
    }

    public Object getJsonToListByMap(String json, Class clazz) {
        Object object = null;
        if (StringUtils.isNotBlank(json)) {
            try {
                object = this.getMapper().readValue(json, TypeFactory.defaultInstance().constructArrayType(TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, clazz)));
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        return object;
    }

    public Object[] getJsonToArray(String json, Class clazz) {
        Object[] object = null;
        if (StringUtils.isNotBlank(json)) {
            try {
                object = (Object[])this.getMapper().readValue(json, TypeFactory.defaultInstance().constructArrayType(clazz));
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        return object;
    }

    public byte[] getJsonTobyteArray(String json) {
        byte[] object = null;
        if (StringUtils.isNotBlank(json)) {
            try {
                object = (byte[])this.getMapper().readValue(json, TypeFactory.defaultInstance().constructArrayType(Byte.TYPE));
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

        return object;
    }

    public Object getJsonToMap(String json, Class keyclazz, Class valueclazz) {
        Object object = null;
        if (StringUtils.isNotBlank(json)) {
            try {
                object = this.getMapper().readValue(json, TypeFactory.defaultInstance().constructParametricType(HashMap.class, new Class[]{keyclazz, valueclazz}));
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }

        return object;
    }

    public Object getJsonToLinkedMap(String json, Class keyclazz, Class valueclazz) {
        Object object = null;
        if (StringUtils.isNotBlank(json)) {
            try {
                object = this.getMapper().readValue(json, TypeFactory.defaultInstance().constructParametricType(LinkedHashMap.class, new Class[]{keyclazz, valueclazz}));
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }

        return object;
    }

    public static Map<String, String> getJsonToMap(String str) {
        Map<String, String> map = new HashMap();
        if (StringUtils.isNotBlank(str)) {
            String[] s = str.split(",");
            if (s.length > 0) {
                for(int i = 0; i < s.length; ++i) {
                    String con = s[i];
                    int s1 = con.indexOf(":");
                    if (s1 > 0) {
                        map.put(con.substring(0, s1).trim().replace("\"", ""), con.substring(s1 + 1).replace("\"", ""));
                    } else {
                        map.put(con.trim().replace("\"", ""), "");
                    }
                }
            }
        }

        return map;
    }

    public String getMapToJson(Map<String, String> map) {
        List<String[]> list = new ArrayList();
        if (null != map && !map.isEmpty()) {
            Iterator var3 = map.keySet().iterator();

            while(var3.hasNext()) {
                String key = (String)var3.next();
                String[] strS = new String[]{key, (String)map.get(key)};
                list.add(strS);
            }
        }

        return this.jsonObject(list);
    }

    public String jsonObject(List list) {
        StringWriter sw = new StringWriter();

        try {
            JsonGenerator gen = (new JsonFactory()).createGenerator(sw);
            this.getMapper().writeValue(gen, list);
            gen.close();
        } catch (Exception var5) {
        }

        return sw.toString();
    }

    public Object getJsonToObject(String json, Class objclazz, Class... pclazz) {
        Object object = null;
        if (StringUtils.isNotBlank(json)) {
            try {
                object = this.getMapper().readValue(json, TypeFactory.defaultInstance().constructParametricType(objclazz, pclazz));
            } catch (Exception var6) {
            }
        }

        return object;
    }

    public <T> Object getJsonToObject(String json, TypeReference<T> typeReference) {
        Object object = null;
        if (StringUtils.isNotBlank(json)) {
            try {
                object = this.getMapper().readValue(json, typeReference);
            } catch (Exception var5) {
            }
        }

        return object;
    }

    public String toJson(Object object) {
        String json = null;

        try {
            json = this.getMapper().writeValueAsString(object);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return json;
    }

    public void setDateFormat(String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            DateFormat df = new SimpleDateFormat(pattern);
            this.getMapper().setDateFormat(df);
            this.getMapper().setTimeZone(TimeZone.getDefault());
        }

    }

    public static Object getResultObject(String json) {
        return json;
    }

    public static void main(String[] args) {
        String da = "2001-5-17";
        Date a = DateUtils.getDateToString(da, "yyyy-MM-dd");
        System.out.println(buildNormalBinder().toJson(a));
    }

    public class CustomDateDeserializer<Date> extends DateDeserializers.DateDeserializer {
        private static final long serialVersionUID = -3912203293075877780L;

        public CustomDateDeserializer() {
        }

        public java.util.Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            try {
                return super.deserialize(jp, ctxt);
            } catch (JsonMappingException var4) {
                return this.retryParse(jp.getText());
            }
        }

        private java.util.Date retryParse(String dateStr) {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            } else {
                try {
                    if (dateStr.length() == "yyyy-MM-dd".length()) {
                        return (new SimpleDateFormat("yyyy-MM-dd")).parse(dateStr);
                    } else if (dateStr.length() == "yyyyMMdd".length()) {
                        return (new SimpleDateFormat("yyyyMMdd")).parse(dateStr);
                    } else {
                        return dateStr.length() == "yyyyMMddHHmmss".length() ? (new SimpleDateFormat("yyyyMMddHHmmss")).parse(dateStr) : null;
                    }
                } catch (Exception var3) {
                    return null;
                }
            }
        }
    }
}

