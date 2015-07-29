package com.ociweb.helloHazelcast;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ICountDownLatch;

public class HelloHazelcast {
    /////////////////////////////////
    //Simple count down latch example
    ////////////////////////////////    

    public final static int MEMBER_COUNT = 5; //for time this was reduced to only 5 instances.
    private final static Logger log = LoggerFactory.getLogger(HelloHazelcast.class);
    final static String LATCH_NAME="helloHazelcast Latch";
    private final static int TIME_OUT = 60*30; //30 minutes
    final static String COUNT_NAME = "helloHazelcast Count";
    
    public static void main(String[] args) {
            
            HelloHazelcast myInstance = new  HelloHazelcast();
            myInstance.run();
                        
    }
   
    
    public void run()  {
        
        log.info("Instance starting up...");
        
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        IAtomicLong noticeCount = hazelcastInstance.getAtomicLong(COUNT_NAME);
        ICountDownLatch latch = hazelcastInstance.getCountDownLatch(LATCH_NAME);
               
        if (noticeCount.get()>0) {
            log.warn("Already started up");
            return;
        }
        
        
        boolean setCountByMe = latch.trySetCount( MEMBER_COUNT );
                
        try {
            latch.countDown();
            if (!latch.await(TIME_OUT, TimeUnit.SECONDS)) {
                log.error("Timeout of {} expired",TIME_OUT);
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        if (setCountByMe) {
            //true so I am the one who set it so I will be the one to print the message
            System.out.println("We are started!");
            noticeCount.incrementAndGet();
            latch.destroy();       
        }
        
        ////
        //do some work here if you like
        ///
        hazelcastInstance.shutdown();
        
    }    
    
}
