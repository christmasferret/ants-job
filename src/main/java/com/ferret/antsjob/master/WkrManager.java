package com.ferret.antsjob.master;

import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.ferret.antsjob.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("master")
public class WkrManager implements AutoCloseable{

    @Autowired
    Client client;

    @Autowired
    KV kvClient;

    @Autowired
    Lease lease;

    public WkrManager() {
    }

    public List<String> listAllWorkers () {
        List<String>  workerList = new ArrayList<>();

        ByteSequence key = ByteSequence.from(Constants.JOB_WORKER_DIR.getBytes());
        GetOption option = GetOption.newBuilder()
                .withPrefix(key)
                .build();
        CompletableFuture<GetResponse> getFuture = kvClient.get(key, option);
        try {
            List<KeyValue> kvs = getFuture.get().getKvs();
            for (KeyValue kv : kvs) {
                String res = new String(kv.getKey().getBytes(), StandardCharsets.UTF_8);
                workerList.add(res);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return workerList;
    }


    @Override
    public void close() {
        kvClient.close();
        client.close();
    }
}
