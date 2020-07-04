package com.ferret.antsjob.lock;

import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.atomic.AtomicBoolean;

public class LockObsr implements StreamObserver<LeaseKeepAliveResponse> {

    private AtomicBoolean running = null;

    public LockObsr(AtomicBoolean running) {
        this.running = running;
    }

    @Override
    public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
//        System.out.println("lock-leaseOnNext");
    }

    @Override
    public void onError(Throwable throwable) {
//        System.out.println("lock-leaseOnError");
        running.set(false);
    }

    @Override
    public void onCompleted() {
        System.out.println("lock-leaseOnNextCompleted!");
        running.set(false);
    }
}
