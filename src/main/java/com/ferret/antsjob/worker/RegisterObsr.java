package com.ferret.antsjob.worker;

import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.atomic.AtomicBoolean;

public class RegisterObsr implements StreamObserver<LeaseKeepAliveResponse> {

    private AtomicBoolean running = null;

    public RegisterObsr(AtomicBoolean running) {
        this.running = running;
    }

    @Override
    public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
//        System.out.println("reg-leaseOnNext");
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("reg-leaseOnError");
        running.set(false);
    }

    @Override
    public void onCompleted() {
        System.out.println("reg-leaseOnNextCompleted!");
        running.set(false);
    }
}
