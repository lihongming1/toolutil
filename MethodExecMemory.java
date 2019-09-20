package com.test.mac;

import java.lang.management.ManagementFactory;
import java.io.*;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试执行某个方法，占用内存的情况
 * 测试参数：-Xms100m -Xmx100m -Xmn100m
 */
public class MethodExecMemory {

    /**
     * 执行方法消耗内存的情况
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 获取pid
        // String name = ManagementFactory.getRuntimeMXBean().getName();
        // String pid = name.split("@")[0];

        // 获取内存大小
        // printMemorySize(pid);
        // 查询Eden Space大小
        printMemoryPoolMXBeans();

        // 创建1MB的内存空间，假设执行某个方法，可以发送http请求调用其他方法
        allocateMemory(20 * 1024 * 1024);
        System.out.println("已分配内存...");

        // 获取内存大小
        // printMemorySize(pid);
        // 查询Eden Space大小
        printMemoryPoolMXBeans();
    }

    /**
     * 分配内存
     *
     * @param size
     */
    public static byte[] allocateMemory(int size) {
        byte[] bytes = new byte[size];
        for (int i = 0; i < bytes.length; i++) {
            // 用0填充数据
            bytes[i] = 0;
        }
        return bytes;
    }

    /**
     * 输出内存使用情况
     */
    public static void printMemoryPoolMXBeans() {
        for (MemoryPoolMXBean memoryPoolMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if (memoryPoolMXBean.getName().contains("Eden")) {
                System.out.println(memoryPoolMXBean.getName() + "   total:" + memoryPoolMXBean.getUsage().getCommitted() / 1024 + "kb" + "   used:" + memoryPoolMXBean.getUsage().getUsed() / 1024 + "kb");
            }
        }
    }

    /**
     * 输出内存情况
     *
     * @param pid
     * @throws Exception
     */
    public static void printMemorySize(String pid) throws Exception {
        Process proc2 = Runtime.getRuntime().exec("jstat -gcnew " + pid);
        List<String> messages2 = printMessage(proc2.getInputStream());
        for (String msg : messages2) {
            System.out.println(msg);
        }
    }

    /**
     * 数据类型转换
     *
     * @param input
     * @return
     */
    public static List<String> printMessage(final InputStream input) {
        List<String> messages = new ArrayList<>();

        Reader reader = new InputStreamReader(input);
        BufferedReader bf = new BufferedReader(reader);
        String line = null;
        try {
            while ((line = bf.readLine()) != null) {
                messages.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                input.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return messages;
    }

}
