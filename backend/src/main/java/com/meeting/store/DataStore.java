package com.meeting.store;

import com.meeting.entity.MeetingRoom;
import com.meeting.entity.Reservation;
import com.meeting.entity.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DataStore {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<Long, MeetingRoom> rooms = new ConcurrentHashMap<>();
    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();

    private final AtomicLong userIdGenerator = new AtomicLong(1);
    private final AtomicLong roomIdGenerator = new AtomicLong(1);
    private final AtomicLong reservationIdGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        initUsers();
        initRooms();
    }

    private void initUsers() {
        users.put(1L, new User(1L, "管理员", "ADMIN"));
        users.put(2L, new User(2L, "张三", "USER"));
        users.put(3L, new User(3L, "李四", "USER"));
        users.put(4L, new User(4L, "王五", "USER"));
        userIdGenerator.set(5);
    }

    private void initRooms() {
        rooms.put(1L, new MeetingRoom(1L, "会议室A", "一楼101", false, "08:00", "18:00"));
        rooms.put(2L, new MeetingRoom(2L, "会议室B", "一楼102", false, "08:00", "18:00"));
        rooms.put(3L, new MeetingRoom(3L, "会议室C", "二楼201", false, "09:00", "17:00"));
        rooms.put(4L, new MeetingRoom(4L, "会议室D", "二楼202", false, "08:00", "20:00"));
        rooms.put(5L, new MeetingRoom(5L, "会议室E", "三楼301", false, "08:00", "18:00"));
        roomIdGenerator.set(6);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUserById(Long id) {
        return users.get(id);
    }

    public List<MeetingRoom> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public MeetingRoom getRoomById(Long id) {
        return rooms.get(id);
    }

    public MeetingRoom saveRoom(MeetingRoom room) {
        if (room.getId() == null) {
            room.setId(roomIdGenerator.getAndIncrement());
        }
        rooms.put(room.getId(), room);
        return room;
    }

    public void deleteRoom(Long id) {
        rooms.remove(id);
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }

    public Reservation getReservationById(Long id) {
        return reservations.get(id);
    }

    public Reservation saveReservation(Reservation reservation) {
        if (reservation.getId() == null) {
            reservation.setId(reservationIdGenerator.getAndIncrement());
        }
        reservations.put(reservation.getId(), reservation);
        return reservation;
    }

    public void deleteReservation(Long id) {
        reservations.remove(id);
    }

    public List<Reservation> getReservationsByRoomId(Long roomId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getRoomId().equals(roomId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Reservation> getReservationsByUserId(Long userId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getUserId().equals(userId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Reservation> getPendingReservations() {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if ("PENDING".equals(r.getStatus())) {
                result.add(r);
            }
        }
        return result;
    }

    public boolean hasConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        for (Reservation r : reservations.values()) {
            if (r.getRoomId().equals(roomId) && 
                !"REJECTED".equals(r.getStatus()) && 
                !"CANCELLED".equals(r.getStatus())) {
                if (excludeId != null && r.getId().equals(excludeId)) {
                    continue;
                }
                if (startTime.isBefore(r.getEndTime()) && endTime.isAfter(r.getStartTime())) {
                    return true;
                }
            }
        }
        return false;
    }
}
