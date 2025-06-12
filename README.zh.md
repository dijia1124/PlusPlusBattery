# PlusPlusBattery - 加加电池 （第三方欧加手机电池信息查看工具）

简体中文 | [English](./README.md)

## 简介

`加加电池` 是一款适合一加宝宝体质的简单电池信息显示和电池寿命估算应用，提供实时电池状态监测，并能在特定条件下评估电池的完全充电容量和硅碳负极电池未经补偿的相对真实的容量和健康度。Oppo和Realme机型也适用。

## 特性

- **实时电池信息**：无需Root权限显示当前电池电量、电压、电流、充放功率、健康状态等信息。
- **电池寿命估算**：仅在电量达到100%且电流在0-20mA范围内时，计算并记录完全充电容量，以此估算电池的健康状况和寿命。不代表真实值。只是估算值。
- **历史循环次数记录**: 每日打开app时记录当天的循环次数并记录在本地Room数据库中，用户可在历史页查看。
- **通知栏电池监控**: 在通知区域显示实时电池信息。显示条目可以自定义。
- **Root模式**: 需要Root权限读取额外信息。
- **当前剩余电量**: 通过读取/sys/class/oplus_chg/battery/battery_rm获取的值。此值随电量变动。
- **完全充满时的容量（battery_fcc）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_fcc获取的值。此值随充放使用上下浮动。
- **真实完全充满时的容量（Root模式）**: 通过反推得到的未经偏移量补偿的fcc值。目前的硅碳负极电池会有算法通过偏移量对不同的截止电压进行fcc的补偿。
- **电池健康（battery_soh）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_soh获取的值。此值随充放使用上下浮动。
- **真实电池健康（Root模式）**: 通过反推得到的未经偏移量补偿的soh值。目前的硅碳负极电池会有算法通过偏移量对不同的截止电压进行soh的补偿。
- **截止电压（vbat_uv）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/vbat_uv获取的值。低于此电压会关机。
- **电池序列号（battery_sn）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_sn获取的值。
- **电池生产日期（battery_manu_date）（Root模式）**: 通过读取/sys/class/oplus_chg/battery/battery_manu_date获取的值。
- **Qmax (batt_qmax) （Root模式）**: Qmax 指的是电池的化学容量。该容量的值与负载无关。这是电池在极低负载电流下能够释放的容量，通常以 mAh 表示。在系统中，此值会因某些条件而发生变化。

## 下载

- [F-Droid Release](https://f-droid.org/en/packages/com.dijia1124.plusplusbattery/)
- [Github Release](https://github.com/dijia1124/PlusPlusBattery/releases)

## 安装

1. 确保您的一加设备运行较新的ColorOS或OxygenOS（比如ColorOS 15)，或是较新的类原生系统。
2. 前往Release页面下载并安装APK文件。
3. 运行APP。
4. （可选）授予root权限
5. （可选）对于通知栏电池监控：加加电池的电池优化/后台限制需要在系统设置里关闭。另外，对于 ColorOS 15 用户，还需要在系统设置中启用加加电池的“自启动”权限，以使通知栏电池监控服务根据屏幕开启/关闭正确恢复/暂停。

## 鸣谢

感谢 [@shminer](https://github.com/shminer) 提供对fcc&soh偏移量相关的源码、算法和思路。


## 开源许可

本项目采用 MIT 许可证。详情请参阅项目中的 `LICENSE` 文件。
