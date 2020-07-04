package com.ferret.antsjob.config;

import com.ferret.antsjob.util.JobUtil;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Watch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("master")
public class MasterConfiguration {

    @Bean
    public Client myClient() {
        return Client.builder().endpoints("http://" + JobUtil.getLocalIp() + ":2379").build();
    }

    @Bean
    @Autowired
    public KV myKV(Client client) {
        return client.getKVClient();
    }

    @Bean
    @Autowired
    public Lease myLease(Client client) {
        return client.getLeaseClient();
    }

    @Bean
    @Autowired
    public Watch myWatcher(Client client) {
        return client.getWatchClient();
    }
}
