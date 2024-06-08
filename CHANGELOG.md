# **Change Log**
All notable changes to this project will be documented in this file. 

## 1.1.0-alpha.2 (Jun 9, 2024)
- Feat: 发布到MavenCentral；
- Feat: 增加ModelProcessor;
- Feat: ImageDecoder、ImageCanvas更名为SamplingDecoder、SamplingCanvas;
- Feat: 新增[使用文档](https://jvziyaoyao.github.io/scale/) 、[API文档](https://jvziyaoyao.github.io/scale/reference/);
- Feat: 更换开源协议为Apache2.0;
- Fix: 修复enterTransform后close小图未显示的问题;
- Fix: 修复ZoomablePager中state与页码不匹配的问题;
- Fix: 修复TransformPreviewer在缩放率大于1时关闭动画错乱的问题;
- Fix: 将Zoomable最大缩放比调整回4;

## 1.1.0-alpha.1 (May 25, 2024)
- Feat: 弃用com.origeek.imageViewer；
- Feat: 重构ImageViewer；
- Feat: 重构ImageGallery为ImagePager；
- Feat: 重构ImagePreviewer；
- Feat: 新增ZoomableView；
- Feat: 新增ZoomablePager；
- Feat: 新增Previewer；

## 1.0.2-alpha.8 (Dec 1, 2023)
- Feat: galleryState添加pageCount；

## 1.0.2-alpha.6 (Oct 8, 2023)
- Feat: 支持上下滑手势关闭图片预览；

## 1.0.2-alpha.5 (Aug 18, 2023)
- Feat: 适配到高版本的HorizontalPager；
 
## 1.0.2-alpha.4 (Jun 19, 2023)
- Fix: 修复imageDecoder release之后获取长宽导致崩溃的问题；
- Fix: 解决TransformItem在LazyList中不及时刷新的问题；
- Feat: 支持大图进行图片旋转；
- Feat: 重构ComposeModel部分以支持手势操作；

## 1.0.2-alpha.3 (May 12, 2023)
- Fix: 移除TransformImageView中的movable，提高滚动性能；
- Feat: Pager组件更新到官方的Pager；

## 1.0.2-alpha.2 (Jan 11, 2023)
- Fix: OrigeekUI切换到发布版本1.0.1-alpha.1

## 1.0.2-alpha.1 (Jan 10, 2023)
- Feat: 支持transform动画效果
- Feat: 支持viewer下拉关闭
- Feat: viewer支持placeholder
- Feat: 对自定义动画曲线提供更完善的支持
- Feat: Canvas大图组件增加淡入淡出效果
- Fix: 集成Viewer时不需要额外集成Pager
- Fix: 优化各组件的参数配置，提高代码简洁度
- Fix: 修复大图预览时卡顿的问题
- Fix: 修复各组件旋转屏幕时状态丢失的问题

## 1.0.1-alpha.3 (Oct 5, 2022)
- Fix crash caused by screen orientationn changed (#7)

## 1.0.1-alpha.2 (Jun 23, 2022)
- Create a sample for this library

## 1.0.1-alpha.1 
- Create this repository and release the first version
