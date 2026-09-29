# synSPT Synthetic Data Generator

synSPT 是面向 ImageJ/Fiji 的可扩展 Single Particle Tracking（SPT）合成显微数据
生成插件。当前 Maven 模块拥有全部运行时代码；只读参考数学库 `../../ImageJ-plugin`
不参与构建，也不是运行时依赖。

## Fiji 入口

```text
Fiji
└── Plugins
    └── synSPT
        └── Synthetic Data Generator
```

GUI 显示：Resolution、Frames、Particle Number、Pixel Size（µm/pixel）、
Frame Rate（fps）、SNR、Spot Shape，以及 Brownian Motion、Directed Motion、
Confined Diffusion、Fractional Brownian Motion、Continuous-Time Random Walk、
Lévy Walk、Scaled Brownian Motion、Annealed Transient Time Motion 八个模型复选框。
模型比例、运动参数、PSF 大小和亮度分布由内部配置生成，不在基础界面暴露。

synSPT GUI 接收以帧每秒表示的 Frame Rate。程序随后转换为物理帧间隔
`frameIntervalSeconds = Δt = 1 / frameRate`；例如 30 fps 对应 `Δt = 1/30 s`。
所有运动模型和 ImagePlus 时间标定继续使用以秒表示的 `Δt`。

GUI 接收以 frame/s 表示的帧率，并在创建物理配置时转换为
`frameIntervalSeconds = Δt = 1 / frameRate`。例如 30 fps 对应 `Δt = 1/30 s`。
所有运动模型及 ImagePlus 时间标定继续使用以秒表示的 Δt。

主窗口左下角提供 `README` 按钮，可直接打开随插件 JAR 分发的离线英文用户指南；
该帮助窗口不依赖浏览器、网络或源码目录。

## 架构与数据流

```text
SimulationFormPanel
        ↓
SimulationConfig (user request)
        ↓
SimulationPlanFactory
        ├─ RandomMotionCompositionGenerator + DirichletSampler
        ├─ CompositionMotionAssigner + 8 basic-model parameter samplers
        ├─ MultiStateGenerator + ChangePointGenerator
        ├─ ParticleAppearanceParameterGenerator
        └─ SnrNoiseCalibrator
        ↓
SimulationPlan (realized composition, particles, parameters, calibration)
        ↓
DefaultSimulationEngine → MotionModelFactory → ParticleTrack → Scene
        ↓
CompositeOpticalRenderer
        ├─ CircularGaussianPsfRenderer
        └─ EllipticalGaussianPsfRenderer
        ↓
ExpectedPhotonFrame
        ↓
CameraNoiseModel (Poisson + physical read noise + gain/offset)
        ↓
CameraFrame → ImageStack → ImagePlus
        ↓
DatasetExporter
```

主要 package：

```text
config    用户请求、分布范围、composition 和隐藏内部配置
motion    八种基础随机过程、Multi-state组合运动及不可变状态参数
particle  粒子级模型分配、粒子定义、Trajectory 和 ParticleTrack
scene     视野、轨迹生成和反射边界
optical   逐粒子 PSF/亮度、期望光子渲染
noise     SNR 校准、Poisson 和相机量化
image     ImageJ ImagePlus 构建与显示 LUT
engine    SimulationPlan 和端到端编排
export    TIFF、ground truth CSV、metadata JSON 事务导出
gui       Simulation-only Swing 界面
```

## 运动模型与随机参数

每个粒子使用独立 parameter seed；运动模型只消费已经确定的 `MotionParameters`，轨迹
随机流与参数随机流相互独立。尺度参数默认采用 log-uniform，指数和几何参数默认采用
uniform。范围集中在 `MotionParameterDistributionConfig`：

| 模型 | 数学实现 | 默认随机范围 |
| --- | --- | --- |
| Brownian | `Δx = sqrt(2DΔt)ξ` | `D ∈ [0.01, 5.0] µm²/s` |
| Directed | Brownian + constant drift | `D ∈ [0.001, 0.5]`, `v ∈ [0.2, 8.0] µm/s`, direction `∈ [0,2π)` |
| Confined | 圆形拒绝反射边界 | `D ∈ [0.01,1.0]`, `R ∈ [0.05,0.5] µm`, 100 substeps/frame |
| FBM | Davies–Harte，失败时 Hosking | `H ∈ [0.1,0.9]`, `K_H ∈ [0.01,1.0]` |
| CTRW | Pareto waiting + Gaussian jump | `α ∈ [0.2,0.9]`, `D ∈ [0.01,1.0]`, `τ₀/Δt ∈ [0.5,3.0]` |
| Lévy Walk | coupled finite-speed flights | `σ ∈ [1.1,1.9]`, `v ∈ [0.2,5.0] µm/s`, `τ₀/Δt ∈ [0.5,3.0]` |
| SBM | deterministic time change | `α ∈ [0.2,1.8]`, `Kα ∈ [0.01,1.0]` |
| ATTM | `α=σ/γ`, `σ<γ<σ+1` | `σ ∈ [0.5,1.5]`, `γ-σ ∈ [0.05,0.95]`, `Dmax ∈ [0.02,1.0]`, `τ₀/Δt ∈ [0.5,3.0]` |
| Multi-state Motion | 2–5个基础运动状态连续拼接 | 状态类型与参数独立采样；通常每段至少10帧，短轨迹自动调整 |

`FBM` 是当前模块的独立实现。它仅复用参考库中经核对的数学思想，没有源码调用、
类路径依赖或构建耦合。

## 随机模型组成

用户只决定参与的模型集合。`MotionWeightRegistry` 保存先验权重，
`RandomMotionCompositionGenerator` 对已选择模型建立 Dirichlet 参数：

```text
alpha_i = concentration × selected_weight_i / selected_weight_sum
```

默认 concentration 为 8。每次 Generate 使用独立 composition seed；固定 root seed 时
composition、粒子级模型分配、参数、轨迹、成像和噪声完全可复现。未选择模型不会进入
composition。`CompositionMotionAssigner` 使用 largest-remainder 方法把 composition ratio
转换为严格等于 Particle Number 的模型数量，并用独立 assignment seed 打乱粒子顺序。
当粒子数不少于已选模型数时，每个已选模型至少分配一个粒子。普通粒子包含一个覆盖完整
轨迹的基础状态。Multi-state粒子包含2–5个连续状态；状态类型从八种基础模型中随机选择，
允许但降低连续重复概率，每个状态独立采样强类型参数。change point采用Dirichlet-like随机
划分，默认最短段长为10帧；帧数不足时自动降低最短段长，全部状态仍严格覆盖完整轨迹。

## 逐粒子显微成像

用户在一次 simulation 中统一选择 `Circular Gaussian` 或
`Elliptical Gaussian`，所有粒子使用相同的 PSF shape。
`ParticleAppearanceParameterGenerator` 在粒子创建时一次性生成其余成像属性，整个轨迹期间
保持固定：

- circular 模式下，SMALL/MEDIUM/LARGE 的 `sigma` 分别为 `[1.2,1.5]` / `[1.6,2.0]` / `[2.0,2.5] px`，每个粒子独立随机；
- elliptical 模式下，major sigma 使用相同三类范围、aspect ratio
  `∈ [0.60,0.70]`、rotation `∈ [0,π)`，每个粒子独立随机；
- PSF support radius = `5 × max(sigmaX,sigmaY)`；
- uncalibrated brightness weight log-uniform `∈ [1000,5000]`，每个粒子独立随机；
  所有权重再除以样本中位数，使相对亮度中位数为 1，最终 photon budget 由目标 SNR 标定。

圆形 PSF 使用误差函数做像素面积解析积分；椭圆 PSF 使用旋转 Gaussian 的子像素积分。
视野外光子自然裁剪。期望光子先叠加背景，再进行 Poisson shot-noise 采样。

## SNR 和相机噪声

SNR 是 `NoiseConfig` 中的用户请求，默认 10.0。当前定义为 Peak Pixel SNR：
`R = A / sqrt(A + B + σr²)`，其中 `A = P × peakWeight` 是 PSF 最亮像素的
粒子信号，`B` 是每像素背景光子数，`σr` 是换算到 photon 等效单位的物理 read noise。
`SnrNoiseCalibrator` 固定背景、read noise、gain 和 PSF，通过闭式反解与 binary search
确定全局 photon scale，使逐粒子预测 SNR 的中位数达到 target SNR。相机阶段只采样真实
Poisson shot noise 和物理 read noise，不再通过额外 Gaussian noise 人为降低 SNR。
若目标超过 bit depth 与当前相机参数允许的 detector-limited maximum，生成前直接拒绝，
不会静默补充非物理噪声。

## 输出

选择一个现有可写的总输出目录后，每次 Generate 在模拟开始前创建一个独立 run 子目录。
每个成功的 run 默认只生成三个文件：

```text
synSPT-output/
└── 20260915_084501_BROWNIAN_seed12345/
    ├── simulation.tif
    ├── trajectory.csv
    └── metadata.json
```

run_id 使用本地开始时间，格式为 `YYYYMMDD_HHmmss_MODEL_seedSEED`。
只选择一个模型时使用该模型枚举名（包括 `MULTI_STATE`）；选择多个模型时统一使用 `MIXED`。
同名目录或文件已存在时，原子创建自动尝试 `_2`、`_3` 等后缀，绝不覆盖旧数据。
失败或取消的 run 保留已预留目录，避免重复使用该标识；临时导出文件会清理。
使用 `run_id + particle_id` 可定位某次 simulation 的完整粒子轨迹。

`trajectory.csv`：

```text
particle_id,frame,time_s,x_um,y_um,segment_id,motion_type,is_change_point,motion_parameters
```

单状态和 Multi-state 统一使用上述九列。`time_s = frame * frameIntervalSeconds`；
`x_um,y_um` 是最终用于图像生成的真实坐标。`segment_id` 在每个粒子内从 0 开始，
`motion_type` 为当前段的基础模型。只有非初始段的第一帧标记 `is_change_point=true`，
即使前后模型相同但参数改变也如此；frame 0 始终为 false。
`motion_parameters` 保存带单位的实际段参数，例如 `D=1.0 um^2/s`，采用标准 CSV 转义。

`metadata.json` schema 10.1 在顶层记录 `run_id`（与最终目录名一致）、`root_seed`、
`selected_motion_models` 和带时区的 `started_at`。`root_seed` 是原 `random_seed` 的规范名称，
为实际模拟使用的有符号 64 位整数，读取器应避免转换为有精度损失的浮点数。
保留全局模拟、相机、噪声和 SNR 标定信息，并保存每粒子的
PSF、相对亮度、光子预算、实际种子与状态段记录，以及 Multi-state 生成规则。
其中 `realized_snr` 是参考位置的理论标定统计，不是含噪图像的实测 SNR。

内部使用 `ExportConfig.advancedExport()` 时，会在该 run 内额外生成高级输出目录：

```text
advanced/
├── trajectory_states.csv
├── particle_properties.csv
└── trajectory_segments.csv
```

`advanced/trajectory_states.csv`：

```text
particle_id,state_id,motion_type,start_frame,end_frame,parameters
```

每行表示一个连续状态区间。`motion_type` 始终是八种基础模型之一；Multi-state是粒子级
组合标识，不作为状态内部的位移方程。相邻状态即使使用同一种模型，也具有独立参数。

`advanced/particle_properties.csv`：

```text
particle_id,brightness_photons_per_frame,psf_shape,radius_pixels,sigma_x_pixels,sigma_y_pixels,rotation_radians,motion_parameters
```

`advanced/trajectory_segments.csv`：

```text
particle_id,start_frame,end_frame,motion_state
```

`metadata.json` schema 10.1 保存 run 身份、root seed、粒子级模型 composition、固定 spot shape、目标/实际 SNR、noise
calibration、motion/appearance distribution config、相机参数、坐标约定和本次实际输出文件。
所有启用的文件先 staging，再以 CREATE_NEW 提交；拒绝覆盖任何已有文件。
提交失败时只回滚本次创建的文件并清理临时文件，旧文件保持不变。

ImagePlus 保持原始 8/16-bit ADU 像素。显示 LUT 使用整个 stack 的实际 unsigned pixel
范围，不改写 TIFF 数据或 ground truth。

## Java 8 构建

```bash
mvn clean test
mvn clean package
```

Maven 使用 `source=1.8`、`target=1.8`。最终 Fiji 插件：

```text
target/synSPT-2.0.0-SNAPSHOT.jar
```

ImageJ 依赖为 `provided`；Commons Math 在 shaded JAR 中重定位到插件私有命名空间。

粒子外观真值包含 `psf_size_class`、`sigma_x`、`sigma_y`（pixel）、`aspect_ratio`（短轴/长轴）、`rotation_angle`（rad）及原始 `brightness_weight`。保留带单位的 sigma/rotation 字段；`relative_brightness` 是原始 weight 除以样本中位数，`emitter_photons_per_frame` 是 SNR 校准后的光子数。
