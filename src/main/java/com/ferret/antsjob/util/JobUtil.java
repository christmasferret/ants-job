package com.ferret.antsjob.util;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.ferret.antsjob.common.Job;
import com.ferret.antsjob.common.JobExecuteInfo;
import com.ferret.antsjob.common.JobSchedulePlan;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;

public class JobUtil {

    public static CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
    public static CronParser parser = new CronParser(cronDefinition);

    public static String getLocalIp() {
        String ip = "";
        try {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                ip = socket.getLocalAddress().getHostAddress();
                return ip;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static JobExecuteInfo buildJobExecuteInfo(JobSchedulePlan jobSchedulePlan) {

        JobExecuteInfo jobExecuteInfo = new JobExecuteInfo(jobSchedulePlan.getJob(), jobSchedulePlan.getNextExecution(), ZonedDateTime.now());
        return jobExecuteInfo;
    }

    public static JobSchedulePlan buildJobSchedulePlan(Job job) {

        Cron unixCron = parser.parse(job.getCronExpr());
        ExecutionTime executionTime = ExecutionTime.forCron(unixCron);
        ZonedDateTime nextExecution = executionTime.nextExecution(ZonedDateTime.now()).get();

        JobSchedulePlan jobSchedulePlan = new JobSchedulePlan(job, executionTime, nextExecution);
        return jobSchedulePlan;
    }

}
