
/*
 *    © [2021] Cognizant. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http:www.apache.orglicensesLICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.cts.idashboard.services.metricservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/*
 * JsonUtil
 *
 * @author Cognizant
 */
public class JsonUtil {


    private static ObjectMapper objectMapper;
    private static final TypeReference<Map<String, String>> mapStringTypeRef = new TypeReference<Map<String, String>>() {
    };

    private JsonUtil() {
    }

    private static void setObjectMapper(ObjectMapper objectMapper) {
        JsonUtil.objectMapper = objectMapper;
    }

    public static String getJsonString(JsonNode jsonNode, String key) {
        return jsonNode.has(key) ? jsonNode.get(key).asText() : "";
    }

    public static List<String> getJsonStringList(JsonNode jsonNode, String key) {
        JavaType stringList = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
        return objectMapper.convertValue(jsonNode.get(key), stringList);
    }

    public static Object getJsonClassList(JsonNode jsonNode, String key, Class className) {
        JavaType classList = objectMapper.getTypeFactory().constructCollectionType(List.class, className);
        return objectMapper.convertValue(jsonNode.get(key), classList);
    }

    /*Convert JsonNode to Map<String, String> and return*/
    public static Map<String, String> getJsonStringMap(JsonNode jsonNode, String key) {
        return objectMapper.convertValue(jsonNode.get(key), mapStringTypeRef);
    }

    /*Convert JsonNode to Map<keyClass, valueClass> and return*/
    public static Object getJsonClassMap(JsonNode jsonNode, String key, Class keyClass, Class valueClass) {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        MapType mapType = typeFactory.constructMapType(Map.class, keyClass, valueClass);
        return objectMapper.convertValue(jsonNode.get(key), mapType);
    }

    /*Convert JsonNode to Map<keyClass, Collection<class>> and return*/
    public static Object getJsonCollectionMap(JsonNode jsonNode, String key, Class keyClass, Collection valueCollectionClass) {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        MapType mapType = typeFactory.constructMapType(Map.class, keyClass, valueCollectionClass.getClass());
        return objectMapper.convertValue(jsonNode.get(key), mapType);
    }

    /*Convert JsonNode to Class Object and return*/
    public static Object getJsonClassObject(JsonNode jsonNode, String key, Class className) {
        return objectMapper.convertValue(jsonNode.get(key), className);
    }

    /*Creating ObjectMapper and assigning to the variable for Global access*/
    public static void init(Environment environment) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                Boolean.parseBoolean(environment.getProperty("spring.jackson.deserialization.FAIL_ON_UNKNOWN_PROPERTIES")));
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        JsonUtil.setObjectMapper(objectMapper);
    }

}
