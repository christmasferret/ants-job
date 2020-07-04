package com.ferret.antsjob.config;

import com.ferret.antsjob.master.WkrManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("master")
@Order(1)
public class MasterApplicationRunner implements ApplicationRunner {

    @Autowired
    WkrManager workerManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> allWorkers = workerManager.listAllWorkers();
        System.out.println("\u001B[32m >>> List all workers: <<<" + allWorkers);
    }
}
