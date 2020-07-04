package com.ferret.antsjob.master;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferret.antsjob.common.Job;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.ferret.antsjob.common.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class JobManager implements AutoCloseable {

        @Autowired
        Client client;

        @Autowired
        KV kvClient;

        @Autowired
        Lease lease;

    public JobManager() {
    }

    public Job saveJob(Job job) {

        ObjectMapper objectMapper = new ObjectMapper();
        Job oldJob = null;

        try {
            PutOption option = PutOption.newBuilder()
                    .withPrevKV()
                    .build();

            ByteSequence jobKey = ByteSequence.from((Constants.JOB_SAVE_DIR + job.getName()).getBytes());
            ByteSequence jobValue = ByteSequence.from(objectMapper.writeValueAsString(job).getBytes());

            CompletableFuture<PutResponse> putFuture = kvClient.put(jobKey, jobValue, option);
            PutResponse pr = putFuture.get();
            if (pr.hasPrevKv()) {
                String res = new String(pr.getPrevKv().getValue().getBytes(), StandardCharsets.UTF_8);
                oldJob = objectMapper.readValue(res, Job.class);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return oldJob;
    }


    public List<Job> listJobs() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Job> jobList = new ArrayList<>();
        ByteSequence key = ByteSequence.from(Constants.JOB_SAVE_DIR.getBytes());

        GetOption option = GetOption.newBuilder()
                .withPrefix(key)
                .build();

        CompletableFuture<GetResponse> getFuture = kvClient.get(key, option);
        try {
            List<KeyValue> kvs = getFuture.get().getKvs();
            for (KeyValue kv : kvs) {
                String res = new String(kv.getValue().getBytes(), StandardCharsets.UTF_8);
                Job job = objectMapper.readValue(res, Job.class);
                jobList.add(job);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jobList;
    }

    public Job deleteJob(String jobName) {
        ObjectMapper objectMapper = new ObjectMapper();
        Job oldJob = null;

        try {
            DeleteOption option = DeleteOption.newBuilder()
                    .withPrevKV(true)
                    .build();

            ByteSequence jobKey = ByteSequence.from((Constants.JOB_SAVE_DIR + jobName).getBytes());

            CompletableFuture<DeleteResponse> delFuture = kvClient.delete(jobKey, option);
            DeleteResponse pr = delFuture.get();
            if (pr.getPrevKvs().size() > 0) {
                String res = new String(pr.getPrevKvs().get(0).getValue().getBytes(), StandardCharsets.UTF_8);
                oldJob = objectMapper.readValue(res, Job.class);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return oldJob;
    }

    public boolean killJob(String jobName) {
        ObjectMapper objectMapper = new ObjectMapper();
        Job oldJob = null;
        long leaseId = 0;

        try {
            CompletableFuture<LeaseGrantResponse> leaseGrantResp = lease.grant(1);
            leaseId = leaseGrantResp.get().getID();

            PutOption option = PutOption.newBuilder()
                    .withLeaseId(leaseId)
                    .build();

            ByteSequence killerKey = ByteSequence.from((Constants.JOB_KILLER_DIR + jobName).getBytes());
            ByteSequence jobValue = Constants.BLANK_VALUE;

            CompletableFuture<PutResponse> putFuture = kvClient.put(killerKey, jobValue, option);
            PutResponse pr = putFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void close() {
        kvClient.close();
        client.close();
    }
}
