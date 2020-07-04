package com.ferret.antsjob.lock;

import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ferret.antsjob.common.Constants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JobLock {

    KV kvClient;
    Lease lease;

    String jobName;
    Boolean isLocked;
    long succeedleaseId;

    public JobLock(KV kvClient, Lease lease, String jobName) {
        this.kvClient = kvClient;
        this.lease = lease;
        this.jobName = jobName;
        this.isLocked = false;
    }

    public boolean tryLock() {

        CompletableFuture<LeaseGrantResponse> leaseGrantResp = lease.grant(5);
        Long leaseId = null;
        try {
            leaseId = leaseGrantResp.get().getID();
            lease.keepAlive(leaseId, new LockObsr(new AtomicBoolean(true)));

            ByteSequence lockKey = ByteSequence.from((Constants.JOB_LOCK_DIR + jobName).getBytes());

            PutOption poption = PutOption.newBuilder()
                    .withLeaseId(leaseId)
                    .build();

            GetOption goption = GetOption.DEFAULT;

            Txn txn = kvClient.txn()
                    .If(new Cmp(lockKey, Cmp.Op.EQUAL, CmpTarget.version(0)))
                    .Then(Op.put(lockKey, Constants.BLANK_VALUE, poption))
                    .Else(Op.get(lockKey, goption));
            CompletableFuture<TxnResponse> txnRespFuture = txn.commit();

            TxnResponse tr = txnRespFuture.get();
            if (tr.isSucceeded()) {
                this.succeedleaseId = leaseId;
                this.isLocked = true;
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        lease.revoke(leaseId);
        return false;
    }

    public void unlock() {
        if (this.isLocked) {
            this.lease.revoke(this.succeedleaseId);
            this.isLocked = false;
        }
    }
}
