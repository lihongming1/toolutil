package com.dkgy.scm.task;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * JVM调优之探索CMS和G1的物理内存归还机制
 * <p>
 * https://blog.csdn.net/weishuai528/article/details/96899513
 * <p>
 * CMS : -Xms128M -Xmx2048M -XX:+UseConcMarkSweepGC
 * G1  : -Xms128M -Xmx2048M -XX:+UseG1GC
 */
public class CmsVsG1GC {

    @Test
    public void testMemoryRecycle() throws InterruptedException {

        List list = new ArrayList();

        //指定要生产的对象大小为512m
        int count = 512;

        //新建一条线程，负责生产对象，回收内存
        new Thread(() -> {
            try {
                for (int i = 1; ; i++) {
                    System.out.println(String.format("第%s次生产%s大小的对象", i, count));
                    addObject(list, count);
                    System.out.println("增加512m对象，list.size = " + list.size());
                    // 休眠10秒
                    Thread.sleep(1000);
                    // 清理内存
                    list.clear();
                    // 通知gc回收
                    TriggerFullGC.gc();
                    // 打印堆内存信息
                    printJvmMemoryInfo();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        //阻止程序退出
        Thread.currentThread().join();
    }

    public void addObject(List list, int count) {
        for (int i = 0; i < count; i++) {
            OOMobject ooMobject = new OOMobject();
            //向list添加一个1m的对象
            list.add(ooMobject);
            try {
                //休眠100毫秒
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class OOMobject {
        //生成1m的对象
        private byte[] bytes = new byte[1024 * 1024];
    }

    public static void printJvmMemoryInfo() {
        // 虚拟机级内存情况查询
        long vmFree = 0;
        long vmUse = 0;
        long vmTotal = 0;
        long vmMax = 0;
        int byteToMb = 1024 * 1024;
        Runtime rt = Runtime.getRuntime();
        vmTotal = rt.totalMemory() / byteToMb;
        vmFree = rt.freeMemory() / byteToMb;
        vmMax = rt.maxMemory() / byteToMb;
        vmUse = vmTotal - vmFree;
        System.out.println("");
        System.out.println("JVM内存已用的空间为：" + vmUse + " MB");
        System.out.println("JVM内存的空闲空间为：" + vmFree + " MB");
        System.out.println("JVM总内存空间为：" + vmTotal + " MB");
        System.out.println("JVM总内存最大堆空间为：" + vmMax + " MB");
        System.out.println("");
    }

}
