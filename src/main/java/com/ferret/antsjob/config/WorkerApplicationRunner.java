package com.ferret.antsjob.config;

import com.ferret.antsjob.worker.Executor;
import com.ferret.antsjob.worker.Register;
import com.ferret.antsjob.worker.Scheduler;
import com.ferret.antsjob.worker.WkrJobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@Component
@Profile("worker")
@Order(1)
public class WorkerApplicationRunner implements ApplicationRunner {

    @Autowired
    Register register;

    @Autowired
    Scheduler scheduler;

    @Autowired
    Executor executor;

    @Autowired
    WkrJobManager wkrJobManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        register.initRegister();
        System.out.println("\u001B[32m >>> started worker <<<");
        scheduler.initScheduler();

        wkrJobManager.watchJobs();
    }
}
