package com.bigdata.etl.scheduler;

import com.bigdata.etl.model.entity.EtlTask;
import com.bigdata.etl.repository.EtlTaskMapper;
import com.bigdata.etl.service.EtlTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CronScheduler {

    private final EtlTaskMapper etlTaskMapper;
    private final EtlTaskService etlTaskService;

    @Scheduled(fixedDelay = 30_000)
    public void checkAndRunCronTasks() {
        log.debug("CronScheduler: scanning for scheduled ETL tasks...");

        LambdaQueryWrapper<EtlTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EtlTask::getScheduleType, "CRON")
               .eq(EtlTask::getStatus, "ENABLED");
        List<EtlTask> cronTasks = etlTaskMapper.selectList(wrapper);

        if (cronTasks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (EtlTask task : cronTasks) {
            try {
                if (shouldRunNow(task, now)) {
                    log.info("CronScheduler: triggering task id={}, name={}", task.getId(), task.getName());
                    etlTaskService.runTask(task.getId());
                }
            } catch (Exception e) {
                log.error("CronScheduler: failed to trigger task id={}", task.getId(), e);
            }
        }
    }

    private boolean shouldRunNow(EtlTask task, LocalDateTime now) {
        if (task.getCronExpression() == null || task.getCronExpression().isBlank()) {
            return false;
        }

        if (task.getLastRunTime() != null) {
            long secondsSinceLastRun = java.time.Duration.between(task.getLastRunTime(), now).getSeconds();
            if (secondsSinceLastRun < 120) {
                return false;
            }
        }

        try {
            String[] fields = task.getCronExpression().trim().split("\s+");
            if (fields.length < 5) {
                log.warn("Invalid cron expression for task id={}", task.getId());
                return false;
            }
            int offset = fields.length >= 6 ? 1 : 0;
            return matchesField(fields[offset], now.getMinute())
                    && matchesField(fields[offset + 1], now.getHour())
                    && matchesField(fields[offset + 2], now.getDayOfMonth())
                    && matchesField(fields[offset + 3], now.getMonthValue())
                    && matchesField(fields[offset + 4], now.getDayOfWeek().getValue() % 7);
        } catch (Exception e) {
            log.warn("Error parsing cron for task id={}", task.getId());
            return false;
        }
    }

    private boolean matchesField(String cronField, int actual) {
        if ("*".equals(cronField)) {
            return true;
        }
        for (String part : cronField.split(",")) {
            if (part.contains("/")) {
                String[] stepParts = part.split("/");
                int step = Integer.parseInt(stepParts[1]);
                int start = "*".equals(stepParts[0]) ? 0 : Integer.parseInt(stepParts[0]);
                if ((actual - start) % step == 0) {
                    return true;
                }
            } else if (part.contains("-")) {
                String[] rangeParts = part.split("-");
                int start = Integer.parseInt(rangeParts[0]);
                int end = Integer.parseInt(rangeParts[1]);
                if (actual >= start && actual <= end) {
                    return true;
                }
            } else {
                if (Integer.parseInt(part) == actual) {
                    return true;
                }
            }
        }
        return false;
    }
}
