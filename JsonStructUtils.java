package com.tblh.bms.sos.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * JSON结构
 *
 * @author lhm
 */
public class JsonStructUtils {

    private static String[] DATE_TIME = {
            "yyyy-MM-dd",
            "yyyyMMddHHmmss",
            "yyyyMMdd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd",
            "yyyyMMdd",
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm"};

    private static final String FIELD_NAME = "fieldName";

    private static final String LEVEL = "level";

    private static final String DATA_TYPE = "dataType";

    private static final String CHILDS = "childs";

    public static void main(String[] args) {
        String json = "{\n" +
                "\t\"a\": {\n" +
                "\t\t\"c\": \"d\"\n" +
                "\t},\n" +
                "\t\"b\": {\n" +
                "\t\t\"e\": [\"x\", \"y\"],\n" +
                "\t\t\"g\": {\n" +
                "\t\t\t\"i\": \"j\"\n" +
                "\t\t},\n" +
                "\t\t\"y\": {\n" +
                "\t\t\t\"i\": \"j\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"yyy\": \"zzz\",\n" +
                "\t\"测试\": \"2019-01-01\"\n" +
                "}";

        System.out.println(json);

        List<JSONObject> resultJsonList = getJsonStruct(json);

        System.out.println(JSON.toJSONString(resultJsonList));

    }

    /**
     * 获取json结构
     *
     * @param json
     * @return
     */
    public static List<JSONObject> getJsonStruct(String json) {
        // 校验JSON
        if (!isJson(json)) {
            return null;
        }
        // 最终数据结构集合
        List<JSONObject> resultJsonList = new ArrayList<>();
        // 遍历集合
        List<JsonStructInfo> list = new ArrayList<JsonStructInfo>();
        // 遍历子集合
        List<JsonStructInfo> temp = new ArrayList<JsonStructInfo>();
        Object rootObj = JSON.parse(json);
        // 封装结构
        JsonStructInfo jsonStructInfo = obj2JsonStructInfo(rootObj, null);
        list.add(jsonStructInfo);
        // 是否初始化
        boolean isInit = false;
        // 等级
        int level = 1;
        while (list != null) {
            for (JsonStructInfo info : list) {
                // 节点
                JSONObject node = (JSONObject) info.getNode();
                // 数据结构
                JSONObject struct = (JSONObject) info.getStruct();
                if (struct == null) {
                    struct = new JSONObject();
                }
                // 孩子节点
                List<JSONObject> childList = new ArrayList<>();
                Set<String> keySet = node.keySet();
                for (String key : keySet) {
                    // 子节点
                    Object childObj = node.get(key);
                    // 数据类型
                    String dataType = handleDataType(childObj);
                    // 结果集
                    JSONObject childJsonObject = new JSONObject();
                    childJsonObject.put(FIELD_NAME, key);
                    childJsonObject.put(LEVEL, level);
                    childJsonObject.put(DATA_TYPE, dataType);
                    childList.add(childJsonObject);
                    if (childObj instanceof JSONObject) {
                        // 封装
                        JsonStructInfo childJsonStructInfo = obj2JsonStructInfo(childObj, childJsonObject);
                        // 加入子集合
                        temp.add(childJsonStructInfo);
                    }
                }
                // 赋值子集
                struct.put(CHILDS, childList);
                if (!isInit) {
                    resultJsonList.add(struct);
                }
            }
            if (temp.size() != 0) {
                // 将子集合替换为遍历集合
                list = new ArrayList<JsonStructInfo>();
                for (JsonStructInfo obj : temp) {
                    list.add(obj);
                }
                temp = new ArrayList<JsonStructInfo>();
                level++;
            } else {
                list = null;
            }
            isInit = true;
        }
        return resultJsonList;
    }

    /**
     * 保存节点和最终数据结构的关系
     */
    @Data
    static class JsonStructInfo {

        private Object node;

        private Object struct;

    }

    /**
     * 将节点和数据结构封装在一起
     *
     * @param node
     * @param struct
     * @return
     */
    public static JsonStructInfo obj2JsonStructInfo(Object node, Object struct) {
        JsonStructInfo jsonStructInfo = new JsonStructInfo();
        jsonStructInfo.setNode(node);
        jsonStructInfo.setStruct(struct);
        return jsonStructInfo;
    }

    /**
     * 是否是JSON结构
     *
     * @param json
     * @return
     */
    private static boolean isJson(String json) {
        boolean isJson = true;
        try {
            JSONObject.parseObject(json);
        } catch (Exception ex) {
            isJson = false;
        }
        return isJson;
    }

    /**
     * 字段数据类型
     *
     * @param obj
     * @return
     */
    private static String handleDataType(Object obj) {
        if (obj instanceof JSONObject) {
            return "object";
        } else if (obj instanceof JSONArray) {
            return "array";
        } else if (obj instanceof Integer) {
            return "integer";
        } else if (obj instanceof Long) {
            return "long";
        } else if (obj instanceof Double) {
            return "double";
        } else if (obj instanceof BigDecimal) {
            return "bigdecimal";
        } else if (obj instanceof BigInteger) {
            return "biginteger";
        } else if (obj instanceof String) {
            // 判断是否是日期类型
            boolean isDate = true;
            try {
                String str = (String) obj;
                DateUtils.parseDate(str, DATE_TIME);
            } catch (Exception e) {
                isDate = false;
            }
            if (isDate) {
                return "date";
            } else {
                return "String";
            }
        } else {
            return "unknown";
        }
    }

}
