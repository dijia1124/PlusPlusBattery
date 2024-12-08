# PlusPlusBattery - 电池信息及寿命估算工具

## 简介

`PlusPlusBattery` 是一款适合一加宝宝体质的简单电池信息显示和电池寿命估算应用，提供实时电池状态监测，并能在特定条件下评估电池的完全充电容量。

## 特点

- **实时电池信息**：显示当前电池电量、电流、健康状态、电压等信息。
- **电池寿命估算**：仅在电池电流为0且电量达到100%时，计算并记录完全充电容量，以此估算电池的健康状况和寿命。不代表真实值。只是估算值。

## 使用原理

应用利用当电池充满（电量达到100%）且电流值为0时的charge counter作为完全充电容量（full charge capacity）的依据，并记录这个值直到下一次数据更新。

## 安装

本应用目前仅支持安卓系统。您可以通过以下步骤进行安装：

1. 确保您的设备运行较新的ColorOS或OxygenOS，比如ColorOS 15。
2. 前往Release页面下载并安装 `app-release.apk` APK文件。
3. 运行APP。


## 开源许可

本项目采用 MIT 许可证。详情请参阅项目中的 `LICENSE` 文件。

---

# PlusPlusBattery - Battery Information and Life Estimation Tool

## Introduction

`PlusPlusBattery` is a simple app designed for newer OnePlus device users to display battery information and estimate battery life. It provides real-time monitoring of battery status and estimates the full charge capacity under specific conditions.

## Features

- **Real-Time Battery Information**: Displays current battery level, current, health status, voltage, and more.
- **Battery Life Estimation**: Calculates and records the full charge capacity only when the battery current is 0 and the level reaches 100%, to estimate the battery's health and lifespan.

## Principle of Operation

The app uses the charge counter at the moment when the battery is fully charged (level reaches 100%) and the current drops to zero as the basis for the full charge capacity. This value is recorded until the next refresh. This data helps users understand the actual charging capacity of the battery and assess its health condition.

## Installation

This app is currently available only for Android systems. Follow these steps to install:

1. Ensure your device is running on newer ColorOS/OxygenOS, such as OxygenOS 15.
2. Go to Release Page, and then download and install the `app-release.apk` APK file.
3. Open the app and grant necessary permissions to access battery status information.

## Developer Information

If you have any suggestions or feedback, please contact me through:

- Email: [zrh19981123@gmail.com](mailto:zrh19981123@gmail.com)

## Open Source License

This project is licensed under the MIT License. For more details, please see the `LICENSE` file in the project.


