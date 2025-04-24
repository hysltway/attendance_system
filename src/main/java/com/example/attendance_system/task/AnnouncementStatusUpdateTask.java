package com.example.attendance_system.task;

import com.example.attendance_system.entity.Announcement;
import com.example.attendance_system.repository.AnnouncementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告状态更新定时任务
 */
@Slf4j
@Component
public class AnnouncementStatusUpdateTask {

    @Autowired
    private AnnouncementRepository announcementRepository;

    /**
     * 定时发布公告（每分钟执行一次）
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void publishScheduledAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        log.info("执行定时发布公告任务，当前时间：{}", now);

        // 查找待发布且发布时间已到的公告
        List<Announcement> announcements = announcementRepository.findByStatusAndIsDeletedOrderByCreatedTimeDesc("draft", 0);

        for (Announcement announcement : announcements) {
            if (announcement.getPublishTime() != null && announcement.getPublishTime().isBefore(now)) {
                announcement.setStatus("published");
                announcementRepository.save(announcement);
                log.info("公告[{}]已定时发布", announcement.getId());
            }
        }
    }

    /**
     * 更新过期公告（每小时执行一次）
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void expireAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        log.info("执行过期公告检查任务，当前时间：{}", now);

        // 查找已发布且有效期已过的公告
        List<Announcement> announcements = announcementRepository.findByStatusAndIsDeletedOrderByCreatedTimeDesc("published", 0);

        for (Announcement announcement : announcements) {
            if (announcement.getValidTo().isBefore(now)) {
                announcement.setStatus("expired");
                announcementRepository.save(announcement);
                log.info("公告[{}]已过期", announcement.getId());
            }
        }
    }
} 