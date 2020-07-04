package com.ferret.antsjob.worker;

import com.ferret.antsjob.common.Constants;
import com.ferret.antsjob.util.JobUtil;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.options.PutOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Profile("worker")
public class Register {

    @Autowired
    Client client;

    @Autowired
    KV kvClient;

    @Autowired
    Lease lease;

    String localIp;

    private AtomicBoolean running;

    public Register() {
        this.localIp = JobUtil.getLocalIp();
        this.running = new AtomicBoolean(true);
    }

    public void initRegister() {
        Thread thread = new Thread(new RegisterTask());
        thread.setDaemon(true);
        thread.start();
    }

    public class RegisterTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                long leaseId = 0;
                try {
                    CompletableFuture<LeaseGrantResponse> leaseGrantResp = lease.grant(10);
                    leaseId = leaseGrantResp.get().getID();
                    lease.keepAlive(leaseId, new RegisterObsr(running));

                    PutOption option = PutOption.newBuilder()
                            .withLeaseId(leaseId)
                            .build();

                    ByteSequence workerKey = ByteSequence.from((Constants.JOB_WORKER_DIR + localIp).getBytes());
                    ByteSequence jobValue = Constants.BLANK_VALUE;

                    CompletableFuture<PutResponse> putFuture = kvClient.put(workerKey, jobValue, option);
                    while (running.get()) {
                        try {
                            Thread.sleep(1000);   // query etcd response
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        lease.revoke(leaseId);
                        Thread.sleep(10000);  //wait ten second to connec to ETCD again
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
