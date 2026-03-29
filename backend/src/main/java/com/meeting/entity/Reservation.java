package com.meeting.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {
    private Long id;
    private Long roomId;
    private Long userId;
    private String userName;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String purpose;
    private LocalDateTime createTime;
}
