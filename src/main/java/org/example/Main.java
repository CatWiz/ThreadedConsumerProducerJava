package org.example;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.Random;

public class Main {
    private static final int storageCapacity = 3;
    private static final int maxProducersConsumers = 3;

    private static final int[] productionPlan = {3, 5, 2};
    private static final int[] consumptionPlan = {3, 3, 4};

    private static final Semaphore storageAccessSemaphore = new Semaphore(maxProducersConsumers, true);
    private static final Semaphore storageSpaceSemaphore = new Semaphore(storageCapacity, true);
    private static final Semaphore productionSemaphore = new Semaphore(0, true);

    private static final ConcurrentLinkedQueue<String> storage = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        Thread[] producers = new Thread[productionPlan.length];
        Thread[] consumers = new Thread[consumptionPlan.length];

        for (int i = 0; i < productionPlan.length; i++) {
            int producerIndex = i;
            producers[producerIndex] = new Thread(() -> Producer(producerIndex, productionPlan[producerIndex]));
            producers[producerIndex].start();
        }

        for (int i = 0; i < consumptionPlan.length; i++) {
            int consumerIndex = i;
            consumers[consumerIndex] = new Thread(() -> Consumer(consumerIndex, consumptionPlan[consumerIndex]));
            consumers[consumerIndex].start();
        }

        // Wait for all threads to finish
        for (Thread producer : producers) {
            try {
                producer.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Thread consumer : consumers) {
            try {
                consumer.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Програму завершено.");
    }

    private static void Producer(int id, int productionAmount) {
        Random random = new Random();

        for (int i = 0; i < productionAmount; i++) {
            try {
                storageSpaceSemaphore.acquire();
                storageAccessSemaphore.acquire();
                Thread.sleep(random.nextInt(2000) + 1000);

                String product = id + "-" + i;
                storage.add(product);
                System.out.println("Виробник " + id + " додав продукт " + product + "\t Час: " + getTimeMSec());

                productionSemaphore.release();
                storageAccessSemaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void Consumer(int id, int consumptionAmount) {
        Random random = new Random();

        for (int i = 0; i < consumptionAmount; i++) {
            try {
                productionSemaphore.acquire();
                storageAccessSemaphore.acquire();

                String product = storage.poll();
                if (product != null) {
                    System.out.println("Споживач " + id + " взяв продукт " + product + "\t Час: " + getTimeMSec());
                } else {
                    System.out.println("Споживач " + id + " не знайшов продукції у сховищі\t Time: " + getTimeMSec());
                }

                storageSpaceSemaphore.release();
                storageAccessSemaphore.release();

                Thread.sleep(random.nextInt(2000) + 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static final long initialTime = System.currentTimeMillis();
    private static long getTimeMSec() {
        return System.currentTimeMillis() - initialTime;
    }
}