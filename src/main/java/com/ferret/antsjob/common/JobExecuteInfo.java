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
public class JobExecuteInfo {

    private Job job;
    private ZonedDateTime planExecution;
    private ZonedDateTime realExecution;
}
