package com.dankegongyu.scm.webapp.controller.quality;

import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 手动触发FullGC
 * 执行了jmap -histo:live pid命令 => 这个会立即触发FullGC
 */
public class TriggerFullGC {

    public static void main(String[] args) throws Exception {

        Process proc = Runtime.getRuntime().exec("jps");
        List<String> messages = printMessage(proc.getInputStream());

        if (!CollectionUtils.isEmpty(messages)) {
            for (String message : messages) {
                if (message.contains("TriggerFullGC")) {
                    String[] messageArr = message.split(" ");
                    String pid = messageArr[0];
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
                }
            }
        }
    }

    private static List<String> printMessage(final InputStream input) {
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
