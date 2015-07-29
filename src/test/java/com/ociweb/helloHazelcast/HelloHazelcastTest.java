package com.ociweb.helloHazelcast;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ICountDownLatch;

public class HelloHazelcastTest {
    private final static int INSTANCES = 10;
    private final static String TEST_LOCK_NAME = "Test Case";
    
    @Test
    public void runTest() {
         HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
         
         //prevent test from running elsewhere a the same time.
         if (hazelcastInstance.getLock(TEST_LOCK_NAME).tryLock()) {         
             try {
                 IAtomicLong noticeCount = hazelcastInstance.getAtomicLong(HelloHazelcast.COUNT_NAME);
                 ICountDownLatch latch = hazelcastInstance.getCountDownLatch(HelloHazelcast.LATCH_NAME);
                 
                 //ensure clean values in the cluster
                 noticeCount.destroy();
                 latch.destroy();
                
                 ExecutorService service = Executors.newFixedThreadPool(INSTANCES);
                
                 List<Future<?>> futureList = new ArrayList<Future<?>>();
                 int i = INSTANCES;
                 while (--i>=0) {
                 
                     futureList.add(service.submit(new Runnable() {
            
                        @Override
                        public void run() {
                            
                            HelloHazelcast hh = new HelloHazelcast();
                            hh.run();
                            
                        }}));                      
                 }
                 
                 for(Future<?> f: futureList) {
                     try {
                        f.get();
                    } catch (InterruptedException | ExecutionException e) {
                       fail();
                    } //block till done
                 }
                 service.shutdown();
                 try {
                    assertTrue(service.awaitTermination(1, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Unable to complete");
                }
                 
                 
                 assertEquals("Should only have printed the message once but it was "+noticeCount.get(),1, noticeCount.get());
             } finally {
                 hazelcastInstance.getLock(TEST_LOCK_NAME).unlock();                 
             }
         } else {
             fail("Test is already running elsewhere");
         }
    }
    
}
