package com.ferret.antsjob.common;


import io.etcd.jetcd.ByteSequence;

public class Constants {
    public static final String JOB_WORKER_DIR = "/antsjob/workers/";
    public static final String JOB_SAVE_DIR = "/antsjob/jobs/";
    public static final String JOB_KILLER_DIR = "/antsjob/killer/";
    public static final String JOB_LOCK_DIR = "/antsjob/lock/";
    public static final ByteSequence BLANK_VALUE = ByteSequence.from("".getBytes());
}
