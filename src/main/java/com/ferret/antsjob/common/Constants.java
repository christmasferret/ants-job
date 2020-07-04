package com.ferret.antsjob.common;


import io.etcd.jetcd.ByteSequence;

public class Constants {
    public static final String JOB_WORKER_DIR = "/cron/workers/";
    public static final String JOB_SAVE_DIR = "/cron/jobs/";
    public static final String JOB_KILLER_DIR = "/cron/killer/";
    public static final String JOB_LOCK_DIR = "/cron/lock/";
    public static final ByteSequence BLANK_VALUE = ByteSequence.from("".getBytes());
}
