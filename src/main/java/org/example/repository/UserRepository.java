package org.example.repository;

import com.google.gson.Gson;
import org.example.entity.*;
import redis.clients.jedis.UnifiedJedis;

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

    public Optional<UserSimple> getUserSimpleById(String id) {
        String userJson = jedis.get(id);
        if (userJson == null) return Optional.empty();
        Gson gson = new Gson();
        UserSimple userObject = gson.fromJson(userJson, UserSimple.class);
        return Optional.of(userObject);
    }

    public Optional<Boolean> saveUserSimple(UserSimple userSimple) {
        jedis.set(userSimple.email, userSimple.email);
        return Optional.of(true);
    }

    public Optional<UserLegacy> getUserLegacyById(String id) {
        String userJson = jedis.get(id);
        if (userJson == null) return Optional.empty();
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

    public UserComplicate getUserComplicate(String id) {
        Optional<UserLegacy> maybeUserLegacy = getUserLegacyById(id);
        UserComplicate userComplicate;
        if (maybeUserLegacy.isPresent()) {
            UserLegacy userLegacy = maybeUserLegacy.get();
            UserSimple userSimple = new UserSimple(
                    userLegacy.name,
                    userLegacy.email,
                    userLegacy.age,
                    userLegacy.isDeveloper);
            userComplicate = new UserComplicate(
                    userSimple,
                    "f1",
                    "f2",
                    "f3",
                    "f4",
                    "f5");

        } else {
            UserSimple userSimple = new UserSimple(
                    "New",
                    "new@customer",
                    22,
                    false
            );
            userComplicate = new UserComplicate(
                    userSimple,
                    "ff1",
                    "ff2",
                    "ff3",
                    "ff4",
                    "ff5");
        }
        return userComplicate;
    }
}
