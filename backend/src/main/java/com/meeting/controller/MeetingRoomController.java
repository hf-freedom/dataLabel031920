package com.meeting.controller;

import com.meeting.common.Result;
import com.meeting.entity.MeetingRoom;
import com.meeting.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin
public class MeetingRoomController {

    @Autowired
    private DataStore dataStore;

    @GetMapping
    public Result<List<MeetingRoom>> getAllRooms() {
        return Result.success(dataStore.getAllRooms());
    }

    @GetMapping("/{id}")
    public Result<MeetingRoom> getRoomById(@PathVariable Long id) {
        MeetingRoom room = dataStore.getRoomById(id);
        if (room == null) {
            return Result.error("会议室不存在");
        }
        return Result.success(room);
    }

    @PostMapping
    public Result<MeetingRoom> createRoom(@RequestBody MeetingRoom room) {
        return Result.success(dataStore.saveRoom(room));
    }

    @PutMapping("/{id}")
    public Result<MeetingRoom> updateRoom(@PathVariable Long id, @RequestBody MeetingRoom room) {
        MeetingRoom existing = dataStore.getRoomById(id);
        if (existing == null) {
            return Result.error("会议室不存在");
        }
        room.setId(id);
        return Result.success(dataStore.saveRoom(room));
    }

    @PutMapping("/{id}/toggle")
    public Result<MeetingRoom> toggleRoom(@PathVariable Long id) {
        MeetingRoom room = dataStore.getRoomById(id);
        if (room == null) {
            return Result.error("会议室不存在");
        }
        room.setDisabled(!room.getDisabled());
        return Result.success(dataStore.saveRoom(room));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        dataStore.deleteRoom(id);
        return Result.success();
    }
}
