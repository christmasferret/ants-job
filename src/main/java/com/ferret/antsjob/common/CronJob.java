package com.ferret.antsjob.common;

import com.cronutils.model.time.ExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CronJob {
    private ExecutionTime executionTime;
    private ZonedDateTime nextExecution;
}
