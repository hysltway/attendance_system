# Copyright (C) 2018-2023
# SPDX-License-Identifier: MIT

# 人脸识别脚本 - 将输入的人脸图像与数据库中的人脸特征进行比对
# Face recognition script - Compare input face image with features in database

import os
import sys
import dlib
import cv2
import numpy as np
import pandas as pd
import json
from PIL import Image

# 获取命令行参数，即图像文件路径
if len(sys.argv) != 2:
    result = {"error": "使用方法: python recognize_face.py <图像文件路径>"}
    print(json.dumps(result))
    sys.exit(1)

image_path = sys.argv[1]

# 检查文件是否存在
if not os.path.exists(image_path):
    result = {"error": "图像文件不存在"}
    print(json.dumps(result))
    sys.exit(1)

# 特征文件路径
features_path = "data/features_all.csv"
if not os.path.exists(features_path):
    result = {"error": "特征数据库文件不存在，请先录入人脸"}
    print(json.dumps(result))
    sys.exit(1)

try:
    # 加载Dlib人脸检测器
    detector = dlib.get_frontal_face_detector()
    
    # 加载Dlib人脸landmark特征点检测器
    predictor_path = 'data/data_dlib/shape_predictor_68_face_landmarks.dat'
    if not os.path.exists(predictor_path):
        result = {"error": "shape_predictor_68_face_landmarks.dat文件不存在"}
        print(json.dumps(result))
        sys.exit(1)
    predictor = dlib.shape_predictor(predictor_path)
    
    # 加载Dlib Resnet人脸识别模型
    face_rec_model_path = 'data/data_dlib/dlib_face_recognition_resnet_model_v1.dat'
    if not os.path.exists(face_rec_model_path):
        result = {"error": "dlib_face_recognition_resnet_model_v1.dat文件不存在"}
        print(json.dumps(result))
        sys.exit(1)
    face_reco_model = dlib.face_recognition_model_v1(face_rec_model_path)
    
    # 读取图像
    img_pil = Image.open(image_path)
    img_np = np.array(img_pil)
    img_rgb = cv2.cvtColor(img_np, cv2.COLOR_RGB2BGR)
    
    # 检测人脸
    faces = detector(img_rgb, 1)
    
    # 如果没有检测到人脸
    if len(faces) == 0:
        result = {"error": "未检测到人脸"}
        print(json.dumps(result))
        sys.exit(1)
    
    # 获取人脸特征点
    shape = predictor(img_rgb, faces[0])
    
    # 计算128维人脸特征向量
    face_descriptor = face_reco_model.compute_face_descriptor(img_rgb, shape)
    
    # 将dlib.vector转换为Python列表
    current_face_descriptor = np.array([float(x) for x in face_descriptor])
    
    # 读取已知人脸特征数据库
    face_features_db = pd.read_csv(features_path, header=None)
    
    if face_features_db.shape[0] == 0:
        result = {"error": "人脸特征数据库为空，请先录入人脸"}
        print(json.dumps(result))
        sys.exit(1)
    
    # 计算欧式距离并找出最匹配的人脸
    min_distance = float('inf')
    matched_name = "unknown"
    
    for i in range(face_features_db.shape[0]):
        person_name = face_features_db.iloc[i][0]
        feature_array = []
        
        for j in range(1, 129):
            if face_features_db.iloc[i][j] == '':
                feature_array.append(0)
            else:
                feature_array.append(float(face_features_db.iloc[i][j]))
        
        known_face_descriptor = np.array(feature_array)
        
        # 计算欧式距离
        distance = np.sqrt(np.sum(np.square(current_face_descriptor - known_face_descriptor)))
        
        if distance < min_distance:
            min_distance = distance
            matched_name = person_name
    
    # 设置匹配阈值，通常0.6以下可认为是同一个人
    threshold = 0.6
    
    if min_distance <= threshold:
        confidence_score = 1.0 - min_distance  # 将距离转换为相似度得分
        result = {
            "status": "success",
            "message": "人脸识别成功",
            "recognized": True,
            "employee_no": matched_name,
            "confidence": round(confidence_score, 4),
            "distance": round(min_distance, 4)
        }
    else:
        result = {
            "status": "success",
            "message": "未找到匹配的人脸",
            "recognized": False,
            "distance": round(min_distance, 4)
        }
    
    # 输出JSON结果
    print(json.dumps(result))
    sys.exit(0)

except Exception as e:
    result = {"error": f"处理图像时出错: {str(e)}"}
    print(json.dumps(result))
    sys.exit(1) 