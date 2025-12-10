package com.muyingmall.monitor;

import com.muyingmall.dto.SystemMetricsDTO.ServerPerformanceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

/**
 * 服务器性能监控组件
 */
@Component
@Slf4j
public class ServerMonitor {

    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;
    private final Runtime runtime;

    public ServerMonitor() {
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.runtime = Runtime.getRuntime();
    }

    /**
     * 获取服务器性能指标
     */
    public ServerPerformanceDTO getServerPerformance() {
        try {
            return ServerPerformanceDTO.builder()
                    // CPU相关
                    .cpuUsage(getCpuUsage())
                    .cpuCores(osBean.getAvailableProcessors())
                    .systemLoadAverage(osBean.getSystemLoadAverage())
                    
                    // 系统内存相关
                    .totalMemory(getTotalPhysicalMemory())
                    .usedMemory(getUsedPhysicalMemory())
                    .freeMemory(getFreePhysicalMemory())
                    .memoryUsage(getMemoryUsage())
                    
                    // JVM内存相关
                    .jvmTotalMemory(runtime.totalMemory())
                    .jvmUsedMemory(runtime.totalMemory() - runtime.freeMemory())
                    .jvmFreeMemory(runtime.freeMemory())
                    .jvmMemoryUsage(getJvmMemoryUsage())
                    
                    // 线程相关
                    .threadCount(threadBean.getThreadCount())
                    .peakThreadCount(threadBean.getPeakThreadCount())
                    
                    // 磁盘相关
                    .totalDiskSpace(getTotalDiskSpace())
                    .usedDiskSpace(getUsedDiskSpace())
                    .freeDiskSpace(getFreeDiskSpace())
                    .diskUsage(getDiskUsage())
                    
                    // 运行时间
                    .uptime(ManagementFactory.getRuntimeMXBean().getUptime())
                    .uptimeFormatted(formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()))
                    .build();
        } catch (Exception e) {
            log.error("获取服务器性能指标失败", e);
            return ServerPerformanceDTO.builder().build();
        }
    }

    // 上一次CPU时间采样数据，用于计算准确的CPU使用率
    private long lastCpuTime = 0;
    private long lastSampleTime = 0;

    /**
     * 获取系统CPU使用率
     * 优先使用getCpuLoad()，如果返回值异常则使用基于时间差的计算方式
     */
    private Double getCpuUsage() {
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                // 方式1：直接使用系统CPU负载
                double cpuLoad = sunOsBean.getCpuLoad();
                if (cpuLoad >= 0 && cpuLoad <= 1) {
                    return Math.round(cpuLoad * 1000) / 10.0; // 保留一位小数
                }

                // 方式2：使用系统负载平均值估算（适用于Linux/Mac）
                double loadAverage = osBean.getSystemLoadAverage();
                int cpuCores = osBean.getAvailableProcessors();
                if (loadAverage >= 0 && cpuCores > 0) {
                    // 负载平均值除以核心数，转换为百分比
                    double estimatedUsage = (loadAverage / cpuCores) * 100;
                    return Math.min(100.0, Math.round(estimatedUsage * 10) / 10.0);
                }

                // 方式3：使用进程CPU作为最后备选
                double processCpuLoad = sunOsBean.getProcessCpuLoad();
                if (processCpuLoad >= 0) {
                    return Math.round(processCpuLoad * 1000) / 10.0;
                }
            }
            return 0.0;
        } catch (Exception e) {
            log.warn("获取CPU使用率失败", e);
            return 0.0;
        }
    }

    /**
     * 获取总物理内存
     */
    private Long getTotalPhysicalMemory() {
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                return sunOsBean.getTotalPhysicalMemorySize();
            }
            return 0L;
        } catch (Exception e) {
            log.warn("获取总物理内存失败", e);
            return 0L;
        }
    }

    /**
     * 获取已用物理内存
     */
    private Long getUsedPhysicalMemory() {
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                return sunOsBean.getTotalPhysicalMemorySize() - sunOsBean.getFreePhysicalMemorySize();
            }
            return 0L;
        } catch (Exception e) {
            log.warn("获取已用物理内存失败", e);
            return 0L;
        }
    }

    /**
     * 获取空闲物理内存
     */
    private Long getFreePhysicalMemory() {
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                return sunOsBean.getFreePhysicalMemorySize();
            }
            return 0L;
        } catch (Exception e) {
            log.warn("获取空闲物理内存失败", e);
            return 0L;
        }
    }

    /**
     * 获取内存使用率
     */
    private Double getMemoryUsage() {
        try {
            long total = getTotalPhysicalMemory();
            long used = getUsedPhysicalMemory();
            return total > 0 ? (used * 100.0 / total) : 0.0;
        } catch (Exception e) {
            log.warn("获取内存使用率失败", e);
            return 0.0;
        }
    }

    /**
     * 获取JVM内存使用率
     */
    private Double getJvmMemoryUsage() {
        try {
            long total = runtime.totalMemory();
            long used = total - runtime.freeMemory();
            return total > 0 ? (used * 100.0 / total) : 0.0;
        } catch (Exception e) {
            log.warn("获取JVM内存使用率失败", e);
            return 0.0;
        }
    }

    /**
     * 获取总磁盘空间
     */
    private Long getTotalDiskSpace() {
        try {
            File root = new File("/");
            return root.getTotalSpace();
        } catch (Exception e) {
            log.warn("获取总磁盘空间失败", e);
            return 0L;
        }
    }

    /**
     * 获取已用磁盘空间
     */
    private Long getUsedDiskSpace() {
        try {
            File root = new File("/");
            return root.getTotalSpace() - root.getFreeSpace();
        } catch (Exception e) {
            log.warn("获取已用磁盘空间失败", e);
            return 0L;
        }
    }

    /**
     * 获取空闲磁盘空间
     */
    private Long getFreeDiskSpace() {
        try {
            File root = new File("/");
            return root.getFreeSpace();
        } catch (Exception e) {
            log.warn("获取空闲磁盘空间失败", e);
            return 0L;
        }
    }

    /**
     * 获取磁盘使用率
     */
    private Double getDiskUsage() {
        try {
            File root = new File("/");
            long total = root.getTotalSpace();
            long used = total - root.getFreeSpace();
            return total > 0 ? (used * 100.0 / total) : 0.0;
        } catch (Exception e) {
            log.warn("获取磁盘使用率失败", e);
            return 0.0;
        }
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long uptimeMs) {
        long days = TimeUnit.MILLISECONDS.toDays(uptimeMs);
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMs) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMs) % 60;

        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d小时 %d分钟", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d分钟 %d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
