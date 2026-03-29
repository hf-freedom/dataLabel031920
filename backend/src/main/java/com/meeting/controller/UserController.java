package com.meeting.controller;

import com.meeting.common.Result;
import com.meeting.entity.User;
import com.meeting.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private DataStore dataStore;

    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(dataStore.getAllUsers());
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = dataStore.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
}
