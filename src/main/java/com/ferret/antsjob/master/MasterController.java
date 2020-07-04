package com.ferret.antsjob.master;

import com.ferret.antsjob.common.Job;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.concurrent.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Profile("master")
public class MasterController {

    @Autowired
    JobManager jobManager;

    @Autowired
    WkrManager wkrManager;

    @PostMapping(value="/job/save")
    @ResponseStatus(HttpStatus.CREATED)
    Job newJob(@RequestBody Job job) {
        return jobManager.saveJob(job);
    }

    @PostMapping(value="/job/delete")
    @ResponseStatus(HttpStatus.ACCEPTED)
    Job deleteJob(@RequestBody String jobName) {
        return jobManager.deleteJob(jobName);
    }

    @GetMapping(value="/job/list")
    @ResponseStatus(HttpStatus.ACCEPTED)
    List<Job> listJobs() {
        return jobManager.listJobs();
    }

    @PostMapping(value="/job/kill")
    @ResponseStatus(HttpStatus.ACCEPTED)
    Boolean killJob(@RequestBody String jobName) {
        return jobManager.killJob(jobName);
    }

    @GetMapping(value="/worker/list")
    @ResponseStatus(HttpStatus.ACCEPTED)
    List<String> listWorkers() {
        return wkrManager.listAllWorkers();
    }

}
