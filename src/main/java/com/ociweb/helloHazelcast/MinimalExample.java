package com.ociweb.helloHazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class MinimalExample {

    final static String INSTANCE_COUNT_NAME = "started instances";
    
    public static void main(String[] args) {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        
        // irregardless of how many instances start only one will say we are started.
        if (0==hazelcastInstance.getAtomicLong(INSTANCE_COUNT_NAME).getAndIncrement()) {
            System.out.println("We are started!");
        }   
    }    
}
