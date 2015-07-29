package com.ociweb.helloHazelcast;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
    
    private final static String TEST_LOCK_NAME = "Test Case";
    
    @Test
    public void helloHazelcastTest() {
         HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
         
         //prevent test from running elsewhere a the same time.
         if (hazelcastInstance.getLock(TEST_LOCK_NAME).tryLock()) {         
             try {
                 IAtomicLong noticeCount = hazelcastInstance.getAtomicLong(HelloHazelcast.COUNT_NAME);
                 ICountDownLatch latch = hazelcastInstance.getCountDownLatch(HelloHazelcast.LATCH_NAME);
                 
                 //ensure clean values in the cluster
                 noticeCount.destroy();
                 latch.destroy();
                
                 ExecutorService service = Executors.newFixedThreadPool(HelloHazelcast.MEMBER_COUNT);
                
                 List<Future<?>> futureList = new ArrayList<Future<?>>();
                 int i = HelloHazelcast.MEMBER_COUNT;
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
    
    private final int MAX_INSTANCES = 10;
    
    @Test
    public void minimalExampleTest() {
         HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
                  
         //prevent test from running elsewhere a the same time.
         if (hazelcastInstance.getLock(TEST_LOCK_NAME).tryLock()) {         
                        
             PrintStream tempStorageOfOutputStream = System.out;
                           
             //capture all this output so we can test for the expected value
             final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
             System.setOut(new PrintStream(myOut));
             
             try {
                 //ensure clean world
                 IAtomicLong count = hazelcastInstance.getAtomicLong(MinimalExample.INSTANCE_COUNT_NAME);
                 count.destroy();
                                  
                 ExecutorService service = Executors.newFixedThreadPool(MAX_INSTANCES);
                
                 List<Future<?>> futureList = new ArrayList<Future<?>>();
                 int i = HelloHazelcast.MEMBER_COUNT;
                 while (--i>=0) {
                 
                     futureList.add(service.submit(new Runnable() {
            
                        @Override
                        public void run() {
                            
                            MinimalExample.main(null);
                            
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
                 
                String captured = new String(myOut.toByteArray());
                
                assertEquals("should only output once","We are started!\n",captured);
                 
             } finally {
                 System.setOut(tempStorageOfOutputStream);
                 hazelcastInstance.getLock(TEST_LOCK_NAME).unlock();                 
             }
         } else {
             fail("Test is already running elsewhere");
         }
    }
    
}
