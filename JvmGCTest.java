import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

/**
 * 模拟jvm gc 发生，并且观察日志
 */
public class JvmGCTest {

    public static void main(String[] args) {

        // 触发YoungGC并答应日志
        // goYoungGCLog();

        /**
         * 然后gc日志中这里的used、capacity、committed、reserved几个字段，都表示什么含义？希望大家自己去探索一下。
         * https://www.jianshu.com/p/cd34d6f3b5b4
         */

        /**
         * 对象进入老年代的4个常见的时机
         * 1.躲过15次gc，达到15岁高龄之后进入老年代；
         * 2.动态年龄判定规则，如果Survivor区域内年龄1+年龄2+年龄3+年龄n的对象总和大于Survivor区的50%，此时年龄n以上的对象会进入老年代，不一定要达到15岁
         * 3.如果一次Young GC后存活对象太多无法放入Survivor区，此时直接计入老年代
         * 4.大对象直接进入老年代
         */

        // goObjectToOldSpace(4);

    }

    /**
     * 模拟对象进入老年代
     * -XX:NewSize=10485760
     * -XX:MaxNewSize=10485760
     * -XX:InitialHeapSize=20971520
     * -XX:MaxHeapSize=20971520
     * -XX:SurvivorRatio=8
     * -XX:MaxTenuringThreshold=15
     * -XX:PretenureSizeThreshold=10485760
     * -XX:+UseParNewGC
     * -XX:+UseConcMarkSweepGC
     * -XX:+PrintGCDetails
     * -XX:+PrintGCTimeStamps
     * -Xloggc:gc.log
     */
    public static void goObjectToOldSpace(int type) {
        if (1 == type) {
            // 模拟：躲过15次gc，达到15岁高龄之后进入老年代
            byte[] array = new byte[100 * 1024];

            byte[] array1 = null;
            for (int i = 0; i < 16; i++) {
                if (array1 == null) {
                    array1 = new byte[6 * 1024 * 1024];
                }
                array1 = null;
                array1 = new byte[6 * 1024 * 1024];
            }

        }
        if (2 == type) {
            // 动态年龄判定规则，如果Survivor区域内年龄1+年龄2+年龄3+年龄n的对象总和大于Survivor区的50%，此时年龄n以上的对象会进入老年代，不一定要达到15岁
            byte[] array1 = new byte[2 * 1024 * 1024];
            array1 = new byte[2 * 1024 * 1024];
            array1 = new byte[2 * 1024 * 1024];
            array1 = null;

            byte[] array2 = new byte[256 * 1024];

            byte[] array3 = new byte[2 * 1024 * 1024];
            array3 = new byte[2 * 1024 * 1024];
            array3 = new byte[2 * 1024 * 1024];
            array3 = new byte[128 * 1024];
            array3 = null;

            byte[] array4 = new byte[2 * 1024 * 1024];
        }
        if (3 == type) {
            // 结论：有部分对象会留在Survivor中，有部分对象会进入老年代的。
            byte[] array1 = new byte[2 * 1024 * 1024];
            array1 = new byte[2 * 1024 * 1024];
            array1 = new byte[2 * 1024 * 1024];

            byte[] array2 = new byte[128 * 1024];
            array2 = null;

            byte[] array3 = new byte[2 * 1024 * 1024];
        }
        if (4 == type) {
            // 模拟大对象直接进入老年代
            byte[] array = new byte[9 * 1024 * 1024];
        }
    }

    /**
     * 触发YoungGC并答应日志
     * -XX:NewSize=5242880
     * -XX:MaxNewSize=5242880
     * -XX:InitialHeapSize=10485760
     * -XX:MaxHeapSize=10485760
     * -XX:SurvivorRatio=8
     * -XX:PretenureSizeThreshold=10485760
     * -XX:+UseParNewGC
     * -XX:+UseConcMarkSweepGC
     * -XX:+PrintGCDetails
     * -XX:+PrintGCTimeStamps
     * -Xloggc:gc.log
     */
    public static void goYoungGCLog() {
        // 获得Eden区可用空间
        long edenSize = 0L;
        for (MemoryPoolMXBean memoryPoolMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if (memoryPoolMXBean.getName().contains("Eden")) {
                long max = memoryPoolMXBean.getUsage().getMax() / 1024;
                long used = memoryPoolMXBean.getUsage().getUsed() / 1024;
                System.out.println(max + "-" + used);
                // 100为误差补值
                edenSize = max - used - 100;
            }
        }

        // 填满Eden区
        byte[] array1 = new byte[(int) edenSize * 1024];
        // 变为垃圾
        array1 = null;

        // 触发YoungGC
        byte[] array2 = new byte[2 * 1024 * 1024];
    }

}
