
import os
import sys
import dlib
import cv2
import numpy as np
import json
from PIL import Image

# 获取命令行参数，即图像文件路径
if len(sys.argv) != 2:
    result = {"error": "使用方法: python extract_face_feature.py <图像文件路径>"}
    print(json.dumps(result))
    sys.exit(1)

image_path = sys.argv[1]

# 检查文件是否存在
if not os.path.exists(image_path):
    result = {"error": "图像文件不存在"}
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
    
    # 如果检测到多个人脸
    if len(faces) > 1:
        result = {"error": "检测到多个人脸，请确保图像中只有一个人脸"}
        print(json.dumps(result))
        sys.exit(1)
    
    # 获取人脸特征点
    shape = predictor(img_rgb, faces[0])
    
    # 计算128维人脸特征向量
    face_descriptor = face_reco_model.compute_face_descriptor(img_rgb, shape)
    
    # 将dlib.vector转换为Python列表
    face_descriptor_list = [float(x) for x in face_descriptor]
    
    # 构造结果
    result = {
        "status": "success",
        "message": "人脸特征提取成功",
        "face_count": 1,
        "feature_vector": face_descriptor_list
    }
    
    # 输出JSON结果
    print(json.dumps(result))
    sys.exit(0)

except Exception as e:
    result = {"error": f"处理图像时出错: {str(e)}"}
    print(json.dumps(result))
    sys.exit(1) 