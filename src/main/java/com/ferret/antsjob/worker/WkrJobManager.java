package com.ferret.antsjob.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferret.antsjob.common.Constants;
import com.ferret.antsjob.common.Job;
import com.ferret.antsjob.common.JobEvent;
import com.ferret.antsjob.common.JobEvent.EventType;
import com.ferret.antsjob.lock.JobLock;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Profile("worker")
public class WkrJobManager {

    @Autowired
    Client client;

    @Autowired
    KV kvClient;

    @Autowired
    Lease lease;

    @Autowired
    Watch watcher;

    @Autowired
    WkrListener wkrListener;

    @Autowired
    BlockingQueue<JobEvent> blockingQueue;

    public WkrJobManager() {
    }

    public JobLock createJobLock(String jobName) {
        JobLock jobLock = new JobLock(kvClient, lease, jobName);
        return jobLock;
    }

    public void watchJobs() {

        ObjectMapper objectMapper = new ObjectMapper();
        ByteSequence key = ByteSequence.from(Constants.JOB_SAVE_DIR.getBytes());

        GetOption option = GetOption.newBuilder()
                .withPrefix(key)
                .build();

        CompletableFuture<GetResponse> getFuture = kvClient.get(key, option);
        try {
            GetResponse getResponse = getFuture.get();
            List<KeyValue> kvs = getResponse.getKvs();
            for (KeyValue kv : kvs) {
                String res = new String(kv.getValue().getBytes(), StandardCharsets.UTF_8);
                Job job = objectMapper.readValue(res, Job.class);
                JobEvent jobEvent = new JobEvent(EventType.SAVE, job);
                blockingQueue.put(jobEvent);
            }

            long watchStartRevision = getResponse.getHeader().getRevision() + 1;
            WatchOption watchOption = WatchOption.newBuilder().withRevision(watchStartRevision).withPrefix(key).build();
            watcher.watch(key, watchOption, wkrListener);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void watchKiller() {

        ObjectMapper objectMapper = new ObjectMapper();
        ByteSequence key = ByteSequence.from(Constants.JOB_KILLER_DIR.getBytes());

        GetOption option = GetOption.newBuilder()
                .withPrefix(key)
                .build();

        CompletableFuture<GetResponse> getFuture = kvClient.get(key, option);
        try {
            GetResponse getResponse = getFuture.get();
            List<KeyValue> kvs = getResponse.getKvs();
            for (KeyValue kv : kvs) {
                String res = new String(kv.getValue().getBytes(), StandardCharsets.UTF_8);
                Job job = objectMapper.readValue(res, Job.class);
                JobEvent jobEvent = new JobEvent(EventType.KILL, job);
                blockingQueue.put(jobEvent);
            }

            long watchStartRevision = getResponse.getHeader().getRevision() + 1;
            WatchOption watchOption = WatchOption.newBuilder().withRevision(watchStartRevision).withPrefix(key).build();
            watcher.watch(key, watchOption, wkrListener);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
