# android_kernel_sprd_sc9832e

基于 **Linux 4.4.147** 的紫光展锐（Unisoc / Spreadtrum）**SC9832E / SL8541E（sharkle 平台）** Android 内核源码树，面向 **LineageOS 17.1** 及国产 SC9832E 设备（DW99、VP09、GM01 等）维护，并集成了 **KernelSU** root 方案。

> 仓库根目录下的 `README`（无扩展名）是 Linux 内核官方自带文档，与本项目无关；本文件才是本仓库的说明文档。

---

## 1. 基本信息

| 项目 | 内容 |
| --- | --- |
| 内核版本 | Linux 4.4.147（`NAME = Blurry Fish Butt`） |
| 目标平台 | Unisoc / Spreadtrum SC9832E、SL8541E（sharkle 平台，ARM64） |
| 默认架构 | `arm64` |
| 许可证 | GPL-2.0（见 `COPYING`） |

### 分支说明

| 分支 | 说明 |
| --- | --- |
| `lineage-17.1` | 本分支，主线分支，集成 **KernelSU v3.3.0-27**（backslashxx fork） |
| `lineage-17.1-resukisu` | 开发分支，在主线基础上改用 **ReSukiSU v4.2.0-rc1**，VP09 额外启用 manual hook |

---

## 2. 主要特性

- **KernelSU 集成**：`CONFIG_KSU=y`，内核驱动以符号链接方式接入（`drivers/kernelsu -> ../KernelSU/kernel`，见 `drivers/Makefile` 第 182 行）。本分支内核侧版本为 **v3.3.0-27**（`KSU_VERSION=32628`），取自 `backslashxx/KernelSU`，来源与提交记录在 `KernelSU/SOURCE_REVISION`（`SOURCE_TAG=v3.3.0-27`、`SOURCE_COMMIT=cf07de01`），接入脚本为 `KernelSU/kernel/setup.sh`。VP09 只启用 `CONFIG_KSU`，未启用 manual hook（manual hook 是 `lineage-17.1-resukisu` 分支的差异）。
- **EROFS 支持**：从 `EROFS_4.9` 内核树 backport 完整的 EROFS 实现（`fs/erofs`，约 7000 行），支持 legacy / inline data 布局、LZ4/LZ4HC 定长输出压缩、xattr（含 SELinux `security.selinux` 标签）与 POSIX ACL，并补上 `EROFS_SUPER_MAGIC` 与 tracepoint。配套将 `lib/lz4` 升级为 Linux 4.11 的 LZ4 实现（提供 `LZ4_compress_default` / `LZ4_decompress_safe` / `LZ4_compress_HC` 新 API），并改造 crypto、zram、squashfs、`decompress_unlz4` 等 5 处调用点。已在 SL8541e（内核 4.4）上验证 EROFS GSI system 分区可挂载并启动到桌面。
- **无线/外设修复**：
  - `sprdwcn` 在 erofs 上解析 fstab 固件路径失败的问题；
  - WCN 固件路径截断（`sizeof(char*)` 误用）修复；
  - GNSS 校准失败不再阻塞 WiFi 启动。
- **KernelSU 版本适配**：内核侧由 v3.2.2（32424）升级至 v3.3.0-27（32628），并移除已废弃的 `ksu_execveat_hook` 以修复 `fs/exec` 兼容性。
- **VP09 设备树修复**：修正 `syscon-cells`、DCAM IOMMU 地址并重整缩进。
- **现代 GCC 可用性**：多批次 `warn-fix` 清理了 GCC 12 / GCC 16 下的告警（`strncpy` 截断、packed UAPI 结构体、柔性数组、`-Wdangling-pointer` 误报等），使高版本工具链可以正常编译这套 4.4 内核。
- **DTB 合并工具**：`tools/dtbtool`，把单机型的多个 `.dtb` 合并为可烧录的 `dtb.img`。
- **CI 自动化**：GitHub Actions 手动触发编译 DW99 / VP09 内核并产出可下载的 artifact。

---

## 3. 目录结构

```
android_kernel_sprd_sc9832e/
├── .github/workflows/          # GitHub Actions：DW99 / VP09 内核构建
│   ├── build-dw99-kernel.yml
│   └── build-vp09-kernel.yml
├── arch/arm64/
│   ├── configs/                # defconfig（lineageos_dw99 / lineageos_vp09 / ...）
│   └── boot/dts/sprd/          # 展锐设备树（dw99.dts、vp09.dts、gm01.dts、sc9832e.dtsi 等）
├── drivers/                    # 内核驱动（kernelsu 为指向 KernelSU/kernel 的符号链接）
├── KernelSU/                   # KernelSU 源码（kernel / manager / userspace / scripts）
├── sprd-board-config/          # 展锐板级配置（sharkle、sharkl3、pike2 等）
├── sprd-diffconfig/            # 展锐差异配置（用户版 / go 版 / trusty 等）
├── tools/dtbtool/              # 独立编译的 DTB 合并工具
├── README                      # 上游 Linux 内核官方文档
└── README.md                   # 本文件
```

---

## 4. 设备与配置对应关系

| defconfig | 设备树 | 说明 |
| --- | --- | --- |
| `lineageos_dw99_defconfig` | `arch/arm64/boot/dts/sprd/dw99.dts` | **DW99**，启用 `CONFIG_KSU`、EROFS |
| `lineageos_vp09_defconfig` | `arch/arm64/boot/dts/sprd/vp09.dts` | **VP09**，启用 `CONFIG_KSU`、EROFS |
| `lineageos_gm01_defconfig` | `arch/arm64/boot/dts/sprd/gm01.dts` | **GM01**，未启用 KernelSU |
| `sprd_sharkle_defconfig` | `sp9832e-1h10-native.dts` 等 | 展锐 sharkle 平台原生参考配置 |
| `sprd_sharkl3_defconfig`、`sprd_sharkl5_defconfig`、`sprd_roc1_defconfig`、`ranchu64_defconfig`、`EOL-*` | — | 同源码树内其他平台/历史配置 |

已注册的 DTB 目标见 `arch/arm64/boot/dts/sprd/Makefile`（`gm01.dtb`、`dw99.dtb`、`vp09.dtb`、`sp9832e-*` 等）。

---

## 5. 编译指南

### 5.1 安装依赖（Debian / Ubuntu）

```bash
sudo dpkg --add-architecture i386
sudo apt-get update
sudo apt-get install -y \
  build-essential bc bison flex \
  libssl-dev libncurses5-dev \
  gcc-aarch64-linux-gnu g++-aarch64-linux-gnu \
  u-boot-tools xz-utils ccache \
  device-tree-compiler \
  libstdc++6:i386 zlib1g:i386
```

### 5.2 本地编译（推荐）

```bash
# 以 DW99 为例
export ARCH=arm64
export CROSS_COMPILE=aarch64-linux-gnu-
export CC="ccache aarch64-linux-gnu-gcc"      # 可选，加速二次编译

make lineageos_dw99_defconfig                 # VP09 请替换为 lineageos_vp09_defconfig

# 可选：自定义内核版本后缀
echo 'CONFIG_LOCALVERSION="-dw99"' >> .config
make olddefconfig

make -j$(nproc)            # 内核镜像 Image / Image.gz / vmlinux
make -j$(nproc) dtbs       # 设备树 *.dtb
make -j$(nproc) modules    # 内核模块
```

### 5.3 生成合并 DTB 镜像（dtb.img）

```bash
# 1) 编译 dtbtool（宿主机 x86-64 工具，不参与内核主构建）
make -C tools/dtbtool clean || true
make -C tools/dtbtool

# 2) 收集本机型的 DTB
mkdir -p /tmp/dtb_input
find arch/arm64/boot/dts -name "*dw99*.dtb" -exec cp {} /tmp/dtb_input/ \;

# 3) 合并（注意：输出路径不能与输入目录同名）
./tools/dtbtool/dtbtool -o /tmp/dtb.img -p /usr/bin/ -s 2048 /tmp/dtb_input/
```

本分支的 `tools/dtbtool/Makefile` 已内置 `CC ?= gcc` 与 `CFLAGS ?= -O2 -Wall -D_GNU_SOURCE`，直接 `make` 即可；需要覆盖时显式传入 `CC=` / `CFLAGS=`。

`dtbtool` 选项：

| 选项 | 含义 |
| --- | --- |
| `-o <file>` | 输出镜像路径（必填） |
| `-p <dir>` | `dtc` 所在目录前缀 |
| `-s <size>` | 页大小，展锐设备通常为 `2048` |
| `-v` | 输出详细日志 |
| `-h` | 帮助 |

### 5.4 编译产物

| 文件 | 说明 |
| --- | --- |
| `arch/arm64/boot/Image` / `Image.gz` | 内核镜像 |
| `arch/arm64/boot/dts/sprd/*.dtb` | 单机型设备树 |
| `dtb.img` | 合并后的设备树镜像（dtbtool 生成） |
| `vmlinux` / `System.map` | 调试符号 |
| `.config` | 最终配置 |

### 5.5 使用 GitHub Actions 编译

在仓库 **Actions** 页面手动触发（`workflow_dispatch`）：

- `Build DW99 Kernel` → `build-dw99-kernel.yml`
- `Build VP09 Kernel` → `build-vp09-kernel.yml`

可填参数 `localversion`（默认 `-dw99` / `-vp09`）。构建完成后在 artifact 中下载 `dw99-kernel-<run_number>-<时间戳>` 或 `vp09-kernel-<run_number>-<时间戳>`，内容包含 `Image`、`Image.gz`、`*.dtb`、`dtb.img`、`vmlinux`、`System.map`、`config`。

> ✅ 两个工作流中的 `actions/checkout` 未指定 `ref`，因此编译的分支**跟随触发工作流时所选择的分支**：在 `lineage-17.1` 上触发即编译本主线分支，在 `lineage-17.1-resukisu` 上触发即编译该开发分支。
>
> 另外，VP09 工作流还会额外通过 `drivers/gpu/arm/midgard` 构建 `mali.ko`。

---

## 6. Root 管理

- 内核驱动源码位于 `KernelSU/`，通过 `drivers/kernelsu` 符号链接参与编译；内核侧版本见 `KernelSU/SOURCE_REVISION`（v3.3.0-27）。
- 需要在设备上安装配套的 Manager APK（KernelSU 管理器）才能授权与管理 root。
- Manager 源码在 `KernelSU/manager/`；如需重新打包 APK，可使用 `KernelSU/repack_apk.py`（配置示例见 `repack-config.example.json`）。

---

## 7. 已知注意事项

- 本分支为主线；`lineage-17.1-resukisu` 在 KernelSU 方案（ReSukiSU + VP09 manual hook）上有差异，提交补丁时请确认目标分支。
- 内核版本为 4.4，新增特性（如 EROFS）均以 backport 形式引入，升级上游组件时需注意 4.4 的 API 限制。
- `make dtbs` 前请确保 `device-tree-compiler` 已安装，否则设备树无法编译。
- 使用 `dtbtool` 时，输出文件不要放在输入 DTB 目录内，否则会读到自身输出。

---

## 8. 致谢与许可

- 上游内核：[Linux Kernel 4.4.147 (GPL-2.0)](https://github.com/kanadenadi/android_kernel_sprd_sc9832e)
- 平台 BSP：Unisoc / Spreadtrum sharkle 平台内核
- Root 方案：[KernelSU](https://github.com/backslashxx/KernelSU)
- 以及所有为本仓库提交修复与适配的贡献者

本仓库遵循 **GNU General Public License v2.0** 发布，详见 `COPYING`。
