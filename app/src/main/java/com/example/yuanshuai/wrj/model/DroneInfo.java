package com.example.yuanshuai.wrj.model;

import java.sql.Timestamp;

public interface DroneInfo
{
    Integer getId();
    String getName();
    String getSerialNo();
    String getRemark();
    Timestamp getCreateTime();
    Integer getDroneModelId();
}
