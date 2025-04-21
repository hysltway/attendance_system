package com.example.attendance_system.repository;

import com.example.attendance_system.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long>, JpaSpecificationExecutor<Announcement> {

    /**
     * 查询有效期内的公告
     *
     * @param now       当前时间
     * @param isDeleted 是否删除
     * @return 公告列表
     */
    List<Announcement> findByValidFromLessThanEqualAndValidToGreaterThanEqualAndIsDeletedOrderByCreatedTimeDesc(
            LocalDateTime now, LocalDateTime now2, Integer isDeleted);

    /**
     * 逻辑删除公告
     *
     * @param id 公告ID
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE Announcement a SET a.isDeleted = 1 WHERE a.id = :id")
    int logicalDeleteById(Long id);

    /**
     * 更新公告状态
     *
     * @param id     公告ID
     * @param status 状态
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE Announcement a SET a.status = :status WHERE a.id = :id")
    int updateStatus(Long id, String status);

    /**
     * 查询特定状态的公告
     *
     * @param status    状态
     * @param isDeleted 是否删除
     * @return 公告列表
     */
    List<Announcement> findByStatusAndIsDeletedOrderByCreatedTimeDesc(String status, Integer isDeleted);

    /**
     * 查询未删除的公告
     *
     * @param isDeleted 是否删除
     * @return 公告列表
     */
    List<Announcement> findByIsDeletedOrderByCreatedTimeDesc(Integer isDeleted);
} 