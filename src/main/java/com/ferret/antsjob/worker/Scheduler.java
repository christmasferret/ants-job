package com.ferret.antsjob.worker;

import com.ferret.antsjob.common.JobEvent;
import com.ferret.antsjob.common.JobExecuteInfo;
import com.ferret.antsjob.common.JobExecuteResult;
import com.ferret.antsjob.common.JobSchedulePlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import com.ferret.antsjob.util.JobUtil;

@Component
@Profile("worker")
public class Scheduler {

    @Autowired
    Executor executor;

    @Autowired
    BlockingQueue<JobEvent> blockingQueue;

    final Semaphore semp = new Semaphore(1);

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    ;
    ScheduledFuture<Long> sfDelay = null;

    Map<String, JobSchedulePlan> jobScheduleTable;
    Map<String, JobExecuteInfo> jobExecutingTable;

    public Scheduler() {
        this.jobScheduleTable = new HashMap<>();
        this.jobExecutingTable = new HashMap<>();
    }

    public Scheduler(Map<String, JobSchedulePlan> jobScheduleTable) {
        ((ScheduledThreadPoolExecutor) this.scheduledExecutorService).setRemoveOnCancelPolicy(true);
        this.jobScheduleTable = jobScheduleTable;
    }

    static long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2, ChronoUnit unit) {
        return unit.between(d1, d2);
    }

    public long trySchedule() {
        if (jobScheduleTable.size() < 1) {
            System.out.println("No Job Found");
            return 1000;
        }
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nearTime = null;
        for (String jobName : jobScheduleTable.keySet()) {
            JobSchedulePlan jobSchedulePlan = jobScheduleTable.get(jobName);
            ZonedDateTime nextTime = jobSchedulePlan.getNextExecution();

            if (nextTime.isBefore(now) || nextTime.isEqual(now)) {
                CompletableFuture.supplyAsync(() -> tryStartJob(jobSchedulePlan))
                        .thenAcceptAsync(jobExecuteInfo -> {
                            jobExecutingTable.remove(jobExecuteInfo.getJobExecuteInfo().getJob().getName());
                            synchronized (this) {
//                                    semp.acquire();
                                long delay = trySchedule();
                                if (sfDelay != null) sfDelay.cancel(false);
                                sfDelay = scheduledExecutorService.schedule(() -> trySchedule(), delay, TimeUnit.MILLISECONDS);
//                                    semp.release();
                            }
                        });
                jobSchedulePlan.setNextExecution(jobSchedulePlan.getExecutionTime().nextExecution(now).get());
            }
            // closest next execution
            if (nearTime == null || jobSchedulePlan.getNextExecution().isBefore(nearTime)) {
                nearTime = jobSchedulePlan.getNextExecution();
            }
        }
        return zonedDateTimeDifference(now, nearTime, ChronoUnit.MILLIS);
    }

    private JobExecuteResult tryStartJob(JobSchedulePlan jobSchedulePlan) {
        if (jobExecutingTable.containsKey(jobSchedulePlan.getJob().getName())) {
            return null;
        }
        JobExecuteInfo jobExecuteInfo = JobUtil.buildJobExecuteInfo(jobSchedulePlan);
        jobExecutingTable.put(jobSchedulePlan.getJob().getName(), jobExecuteInfo);
        return executor.executeJob(jobExecuteInfo);
    }

    private void handleJobEvent(JobEvent jobEvent) {
        switch (jobEvent.getEventType()) {
            case SAVE:
                JobSchedulePlan jobSchedulePlan = JobUtil.buildJobSchedulePlan(jobEvent.getJob());
                jobScheduleTable.put(jobEvent.getJob().getName(), jobSchedulePlan);
                break;
            case DELETE:
                jobScheduleTable.remove(jobEvent.getJob().getName());
                break;
            case KILL:
                break;
            default:
        }
    }

    public void initScheduler() {
        Thread t1 = new Thread(() -> {
            long delay = trySchedule();
            while (true) {
                try {
                    synchronized (this) {
//                        semp.acquire();
                        if (sfDelay == null || sfDelay.isDone()) {
                            if (sfDelay != null && sfDelay.isDone()) {
                                delay = sfDelay.get();
                            }
                            sfDelay = scheduledExecutorService.schedule(() -> trySchedule(), delay, TimeUnit.MILLISECONDS);
                        }
//                        semp.release();
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.setDaemon(true);
        t1.start();

        Thread t2 = new Thread(() -> {
            while (true) {
                try {
                    JobEvent jobEvent = blockingQueue.take();
                    synchronized (this) {
//                        semp.acquire();
                        System.out.println("new JobEvent received: " + jobEvent);
                        handleJobEvent(jobEvent);
                        long delay = trySchedule();
                        if (sfDelay != null) sfDelay.cancel(false);
                        sfDelay = scheduledExecutorService.schedule(() -> trySchedule(), delay, TimeUnit.MILLISECONDS);
//                        semp.release();
                    }

                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t2.setDaemon(true);
        t2.start();
    }
}
