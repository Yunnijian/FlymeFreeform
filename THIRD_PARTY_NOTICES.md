# 第三方资源声明

## 上游项目与代码归属

本项目 fork 自 [Mangi-11/FlymeFreeform](https://github.com/Mangi-11/FlymeFreeform)（GPL-3.0）。上游面向 ColorOS，本 fork 在其基础上增加了 HyperOS 平台适配与扇形布局增强。

扇形多圈布局的部分代码移植自上游 PR #6，原作者 [Minessential](https://github.com/Minessential)，对应提交：

- `21b454b` 支持多圈扇形布局与动态固定应用容量
- `7f4fce5` 支持调节同圈图标间距

本 fork 按双平台架构改写了对应实现（上游 `ColorOsAppCatalog` / `ColorOsFreeformCoordinator` 对应本仓 `platform/common/AppCatalog` / `platform/common/FreeformGestureCoordinator`）。其中 `PinnedAppsScreen.kt`、`AdaptiveOverlayGeometry.kt`、`RadialSettingsCard.kt`、`RadialIconGeometry.kt`、`RadialMenuGeometry.kt`、`RadialMenuSettings.kt`、`RadialDisplayBounds.kt`、`PinnedComponentCodec.kt`，以及 `PinnedComponentCodecTest.kt`、`RadialMenuGeometryTest.kt`、`AdaptiveOverlayGeometryTest.kt`、`RadialIconGeometryTest.kt` 与上述提交逐字节相同，相关权利归原作者所有。

`AppCatalogSnapshotTest.kt` 与其余文件为本 fork 按自身结构改写，未逐字节沿用。

## 应用图标

应用图标中的蓝色气泡图案取自魅族 22 的 Flyme 12.6.0.0A 官方固件，原资源属于“系统界面工具”应用，该应用包含“应用小窗”组件。固件来源：[Flyme 系统更新页面](https://www.flyme.com/firmwarelist-207.html#3)。

本项目保留原始透明 PNG 的图案与配色，增加白色自适应背景、比例留白与单色着色配置。图案的相关权利归原权利人所有，不属于本项目原创资源。固件内未发现该图标的独立再分发许可；公开分发前仍需确认相应授权，本声明不构成授权。

Flyme 小窗是面向 ColorOS 与 HyperOS 的非官方独立实现，与魅族、Flyme、OPPO、ColorOS、小米、HyperOS 及其关联方无关。使用该图案不代表上述主体对本项目的授权、背书或支持。
