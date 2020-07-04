package com.ferret.antsjob.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JobExecuteResult {
    private JobExecuteInfo jobExecuteInfo;
    private String output;
    private String error;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
}
