package com.meeting.controller;

import com.meeting.common.Result;
import com.meeting.entity.MeetingRoom;
import com.meeting.entity.Reservation;
import com.meeting.entity.User;
import com.meeting.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin
public class ReservationController {

    @Autowired
    private DataStore dataStore;

    @GetMapping
    public Result<List<Reservation>> getAllReservations() {
        return Result.success(dataStore.getAllReservations());
    }

    @GetMapping("/{id}")
    public Result<Reservation> getReservationById(@PathVariable Long id) {
        Reservation reservation = dataStore.getReservationById(id);
        if (reservation == null) {
            return Result.error("预定不存在");
        }
        return Result.success(reservation);
    }

    @GetMapping("/room/{roomId}")
    public Result<List<Reservation>> getReservationsByRoom(@PathVariable Long roomId) {
        return Result.success(dataStore.getReservationsByRoomId(roomId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<Reservation>> getReservationsByUser(@PathVariable Long userId) {
        return Result.success(dataStore.getReservationsByUserId(userId));
    }

    @GetMapping("/pending")
    public Result<List<Reservation>> getPendingReservations() {
        return Result.success(dataStore.getPendingReservations());
    }

    @PostMapping
    public Result<Reservation> createReservation(@RequestBody Reservation reservation) {
        MeetingRoom room = dataStore.getRoomById(reservation.getRoomId());
        if (room == null) {
            return Result.error("会议室不存在");
        }
        if (room.getDisabled()) {
            return Result.error("该会议室已被禁用");
        }

        User user = dataStore.getUserById(reservation.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }

        LocalDateTime startTime = reservation.getStartTime();
        LocalDateTime endTime = reservation.getEndTime();

        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            return Result.error("结束时间必须大于开始时间");
        }

        if (dataStore.hasConflict(reservation.getRoomId(), startTime, endTime, null)) {
            return Result.error("该时间段已被预定");
        }

        reservation.setUserName(user.getName());
        reservation.setRoomName(room.getName());
        reservation.setCreateTime(LocalDateTime.now());

        if ("ADMIN".equals(user.getRole())) {
            reservation.setStatus("APPROVED");
        } else {
            reservation.setStatus("PENDING");
        }

        return Result.success(dataStore.saveReservation(reservation));
    }

    @PutMapping("/{id}/approve")
    public Result<Reservation> approveReservation(@PathVariable Long id) {
        Reservation reservation = dataStore.getReservationById(id);
        if (reservation == null) {
            return Result.error("预定不存在");
        }
        if (!"PENDING".equals(reservation.getStatus())) {
            return Result.error("该预定不在待审批状态");
        }
        reservation.setStatus("APPROVED");
        return Result.success(dataStore.saveReservation(reservation));
    }

    @PutMapping("/{id}/reject")
    public Result<Reservation> rejectReservation(@PathVariable Long id) {
        Reservation reservation = dataStore.getReservationById(id);
        if (reservation == null) {
            return Result.error("预定不存在");
        }
        if (!"PENDING".equals(reservation.getStatus())) {
            return Result.error("该预定不在待审批状态");
        }
        reservation.setStatus("REJECTED");
        return Result.success(dataStore.saveReservation(reservation));
    }

    @PutMapping("/{id}/cancel")
    public Result<Reservation> cancelReservation(@PathVariable Long id) {
        Reservation reservation = dataStore.getReservationById(id);
        if (reservation == null) {
            return Result.error("预定不存在");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            return Result.error("该预定已取消");
        }
        if ("REJECTED".equals(reservation.getStatus())) {
            return Result.error("该预定已被驳回");
        }
        reservation.setStatus("CANCELLED");
        return Result.success(dataStore.saveReservation(reservation));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReservation(@PathVariable Long id) {
        dataStore.deleteReservation(id);
        return Result.success();
    }
}
