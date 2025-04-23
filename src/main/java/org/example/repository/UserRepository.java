package org.example.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.entity.*;
import redis.clients.jedis.UnifiedJedis;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;


public class UserRepository {
    UnifiedJedis jedis;
    public UserRepository() {
        jedis = new UnifiedJedis("redis://localhost:6379");
        String simple = "{\"name\":\"Jordan\",\"email\":\"norman@futurestud.io\",\"age\":26,\"isDeveloper\":true}";
        jedis.set("norman@tuturestud.io", simple);
        String complicate = "{" +
                "\"name\":\"Jonathan\"," +
                "\"email\":\"jonathan@example.com\"," +
                "\"age\":30," +
                "\"isDeveloper\":true," +
                "\"field1\":\"value1\"," +
                "\"field2\":\"value2\"," +
                "\"field3\":\"value3\"," +
                "\"field4\":\"value4\"," +
                "\"field5\":\"value5\"" +
                "}";
        jedis.set("complicate@sample.com", complicate);
    }

    public Optional<UserSimple> getUserSimpleById(String id){
        String userJson = jedis.get(id);
        if(userJson == null) return Optional.empty();
        Gson gson = new Gson();
        UserSimple userObject = gson.fromJson(userJson, UserSimple.class);
        return Optional.of(userObject);
    }

    public Optional<Boolean> saveUserSimple(UserSimple userSimple) {
        jedis.set(userSimple.email, userSimple.email);
        return Optional.of(true);
    }

    public Optional<UserLegacy> getUserLegacyById(String id){
        String userJson = jedis.get(id);
        if(userJson == null) return Optional.empty();
        Gson gson = new Gson();
        UserLegacy userLegacy = gson.fromJson(userJson, UserLegacy.class);
        return Optional.of(userLegacy);
    }

    public Optional<Boolean> saveUserComplicate(UserComplicate userComplicate) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(userComplicate);
        jedis.set(userComplicate.userSimple.email, jsonString);
        return Optional.of(true);
    }
}
