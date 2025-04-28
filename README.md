# PlusPlusBattery - 电池信息及寿命估算工具

## 简介

`PlusPlusBattery` 是一款适合一加宝宝体质的简单电池信息显示和电池寿命估算应用，提供实时电池状态监测，并能在特定条件下评估电池的完全充电容量。

## 特点

- **实时电池信息**：无需Root权限显示当前电池电量、电压、电流、充放功率、健康状态等信息。
- **电池寿命估算**：仅在电池电流为0且电量达到100%时，计算并记录完全充电容量，以此估算电池的健康状况和寿命。不代表真实值。只是估算值。
- **历史循环次数记录**: 每日打开app时记录当天的循环次数并记录在本地Room数据库中，用户可在历史页查看。
- **Root模式**: 需要Root权限读取额外信息（执行cat命令）。
- **完全充满时的容量（battery_fcc）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_fcc获取的值。此值随充放使用上下浮动。
- **电池健康（battery_soh）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_soh获取的值。此值随充放使用上下浮动。
- **电池欠压阈值（vbat_uv）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/vbat_uv获取的值。低于此电压会关机。

## 安装

本应用目前仅支持安卓系统。您可以通过以下步骤进行安装：

1. 确保您的一加设备运行较新的ColorOS或OxygenOS（比如ColorOS 15)，或是较新的类原生系统。
2. 前往Release页面下载并安装APK文件。
3. 运行APP。


## 开源许可

本项目采用 MIT 许可证。详情请参阅项目中的 `LICENSE` 文件。

---

# PlusPlusBattery - Battery Information and Life Estimation Tool

## Introduction

`PlusPlusBattery` is a simple app designed for newer OnePlus device users to display battery information and estimate battery life. It provides real-time monitoring of battery status and estimates the full charge capacity under specific conditions.

## Features

- **Real-Time Battery Information**: Displays current battery level, current, power, health status, voltage, and more.
- **Battery Life Estimation**: Calculates and records the full charge capacity only when the battery current is ***0 mA*** and the charge level reaches 100%, to estimate the battery's health and lifespan.
- **Historical Cycle Counts**: Charge Cycle counts are kept locally in the history screen.
- **Root Mode**: Ability to read extra info by executing cat command
- **Full Charge Capacity (battery_fcc)**: Value read by cat /sys/class/oplus_chg/battery/battery_fcc. This value varies depending on charging and discharging activities.
- **Battery Health (battery_soh)**: Value read by cat /sys/class/oplus_chg/battery/battery_soh. This value varies depending on charging and discharging activities.
- **Battery Under-Voltage Threshold (vbat_uv)**: Value read by cat /sys/class/oplus_chg/battery/vbat_uv. The device shuts down if the voltage is lower than this value.

## Installation

This app is currently available only for Android systems. Follow these steps to install:

1. Ensure your device is running on newer ColorOS/OxygenOS (such as OxygenOS 15), or newer AOSP-based ROMs. 
2. Go to Release Page, and then download the APK file.
3. Open the app and grant necessary permissions to access battery status information.

## Developer Information

If you have any suggestions or feedback, please contact me through:

- Email: [zrh19981123@gmail.com](mailto:zrh19981123@gmail.com)

## Open Source License

This project is licensed under the MIT License. For more details, please see the `LICENSE` file in the project.


