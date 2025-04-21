package com.example.attendance_system.repository;

import com.example.attendance_system.entity.AnnouncementReadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementReadLogRepository extends JpaRepository<AnnouncementReadLog, Long> {
    
    /**
     * 查询员工是否已读某公告
     * @param announcementId 公告ID
     * @param employeeNo 员工编号
     * @return 阅读记录
     */
    Optional<AnnouncementReadLog> findByAnnouncementIdAndEmployeeNo(Long announcementId, String employeeNo);
    
    /**
     * 查询某公告的所有阅读记录
     * @param announcementId 公告ID
     * @return 阅读记录列表
     */
    List<AnnouncementReadLog> findByAnnouncementId(Long announcementId);
    
    /**
     * 统计已读人数
     * @param announcementId 公告ID
     * @return 已读人数
     */
    long countByAnnouncementId(Long announcementId);
    
    /**
     * 查询员工所有已读公告ID
     * @param employeeNo 员工编号
     * @return 公告ID列表
     */
    @Query("SELECT a.announcementId FROM AnnouncementReadLog a WHERE a.employeeNo = :employeeNo")
    List<Long> findAnnouncementIdsByEmployeeNo(String employeeNo);
} 