package com.ferret.antsjob.worker;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferret.antsjob.common.Constants;
import com.ferret.antsjob.common.Job;
import com.ferret.antsjob.common.JobEvent.EventType;
import com.ferret.antsjob.common.JobEvent;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Watch.Listener;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Component
@Profile("worker")
public class WkrListener implements Listener {

    @Autowired
    BlockingQueue<JobEvent> blockingQueue;

    ObjectMapper objectMapper;

    public WkrListener() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onNext(WatchResponse response) {
        List<WatchEvent> eventList = response.getEvents();
        for (WatchEvent event : eventList) {
            switch (event.getEventType()) {
                case PUT:
                    try {
                        String res = new String(event.getKeyValue().getValue().getBytes(), StandardCharsets.UTF_8);
                        Job job = objectMapper.readValue(res, Job.class);
                        JobEvent jobEvent = new JobEvent(EventType.SAVE, job);
                        blockingQueue.put(jobEvent);
                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case DELETE:
                    try {
                        String res = new String(event.getKeyValue().getKey().getBytes(), StandardCharsets.UTF_8);
                        String jobName = res.replaceAll("^" + Constants.JOB_SAVE_DIR, "");
                        Job job = new Job(jobName, "", "");
                        JobEvent jobEvent = new JobEvent(EventType.DELETE, job);
                        blockingQueue.put(jobEvent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {

    }
}
