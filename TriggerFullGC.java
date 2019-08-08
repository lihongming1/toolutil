package com.dkgy.scm.task;

import org.springframework.util.CollectionUtils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * 手动触发FullGC
 * 执行了jmap -histo:live pid命令 => 这个会立即触发FullGC
 * 如果不希望触发fullgc 可以使用jmap -histo pid
 */
public class TriggerFullGC {

    public static void gc() {
        try {
//            String pid = null;
//            Process proc = Runtime.getRuntime().exec("jps");
//            List<String> messages = TriggerFullGC.printMessage(proc.getInputStream());
//            for (String str : messages) {
//                if (str.contains(name)) {
//                    String[] strArr = str.split(" ");
//                    pid = strArr[0];
//                    break;
//                }
//            }
            String name = ManagementFactory.getRuntimeMXBean().getName();
            String pid = name.split("@")[0];
            // GC
            gcPid(pid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void gcPid(String pid) {
        try {
            System.out.println("pid: " + pid);
            // 打印日志
            Process proc2 = Runtime.getRuntime().exec("jstat -gcutil " + pid);
            List<String> messages2 = printMessage(proc2.getInputStream());
            if (!CollectionUtils.isEmpty(messages2)) {
                for (String msg : messages2) {
                    System.out.println(msg);
                }
            }
            // 执行命令
            String[] exe = {"/bin/sh", "-c", "jmap -histo:live " + pid + " | head -10"};
            Process proc3 = Runtime.getRuntime().exec(exe);
            System.out.println(proc3);
            List<String> messages3 = printMessage(proc3.getInputStream());
            if (!CollectionUtils.isEmpty(messages3)) {
                for (String msg : messages3) {
                    System.out.println(msg);
                }
            }
            // 打印日志
            Process proc4 = Runtime.getRuntime().exec("jstat -gcutil " + pid);
            List<String> messages4 = printMessage(proc4.getInputStream());
            if (!CollectionUtils.isEmpty(messages4)) {
                for (String msg : messages4) {
                    System.out.println(msg);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

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
