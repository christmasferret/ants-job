package com.ferret.antsjob.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JobEvent {

    public enum EventType {
        SAVE,
        DELETE,
        KILL
    }

    private EventType eventType;
    private Job job;

}
