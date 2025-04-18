package com.example.attendance_system.repository;

import com.example.attendance_system.entity.FaceFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 人脸特征数据访问接口
 */
@Repository
public interface FaceFeatureRepository extends JpaRepository<FaceFeature, Long> {
    
    /**
     * 根据员工编号查询人脸特征
     * @param employeeNo 员工编号
     * @return 人脸特征
     */
    FaceFeature findByEmployeeNo(String employeeNo);
    
    /**
     * 根据员工编号删除人脸特征
     * @param employeeNo 员工编号
     */
    void deleteByEmployeeNo(String employeeNo);
    
    /**
     * 根据员工编号查询是否存在人脸特征
     * @param employeeNo 员工编号
     * @return 是否存在
     */
    boolean existsByEmployeeNo(String employeeNo);

    /**
     * 查询所有有效的人脸特征
     * @return 人脸特征列表
     */
    @Query("SELECT f FROM FaceFeature f WHERE f.status = 1")
    List<FaceFeature> findAllActiveFeatures();
} 