package com.meeting.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeetingRoom {
    private Long id;
    private String name;
    private String location;
    private Boolean disabled;
    private String openTimeStart;
    private String openTimeEnd;
}
