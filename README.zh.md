# PlusPlusBattery - 一加电池信息查看工具

简体中文 | [English](./README.md)

## 简介

`PlusPlusBattery` 是一款适合一加宝宝体质的简单电池信息显示和电池寿命估算应用，提供实时电池状态监测，并能在特定条件下评估电池的完全充电容量和硅碳负极电池未经补偿的真实容量和健康度

## 特性

- **实时电池信息**：无需Root权限显示当前电池电量、电压、电流、充放功率、健康状态等信息。
- **电池寿命估算**：仅在电池电流为0且电量达到100%时，计算并记录完全充电容量，以此估算电池的健康状况和寿命。不代表真实值。只是估算值。
- **历史循环次数记录**: 每日打开app时记录当天的循环次数并记录在本地Room数据库中，用户可在历史页查看。
- **Root模式**: 需要Root权限读取额外信息（执行cat命令）。
- **当前剩余电量**: 通过读取/sys/class/oplus_chg/battery/battery_rm获取的值。此值随电量变动。
- **完全充满时的容量（battery_fcc）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_fcc获取的值。此值随充放使用上下浮动。
- **真实完全充满时的容量（Root模式）**: 通过反推得到的未经偏移量补偿的fcc值。目前的硅碳负极电池会有算法通过偏移量对不同的电池欠压阈值进行fcc的补偿。
- **电池健康（battery_soh）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_soh获取的值。此值随充放使用上下浮动。
- **真实电池健康（Root模式）**: 通过反推得到的未经偏移量补偿的soh值。目前的硅碳负极电池会有算法通过偏移量对不同的电池欠压阈值进行soh的补偿。
- **电池欠压阈值（vbat_uv）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/vbat_uv获取的值。低于此电压会关机。
- **电池序列号（battery_sn）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_sn获取的值。
- **电池生产日期（battery_manu_date）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_manu_date获取的值。

## 安装

1. 确保您的一加设备运行较新的ColorOS或OxygenOS（比如ColorOS 15)，或是较新的类原生系统。
2. 前往Release页面下载并安装APK文件。
3. 运行APP。
4. 授予root权限（可选）

## 鸣谢

感谢 [@shminer](https://github.com/shminer) 提供对fcc&soh偏移量相关的源码、算法和思路。


## 开源许可

本项目采用 MIT 许可证。详情请参阅项目中的 `LICENSE` 文件。
