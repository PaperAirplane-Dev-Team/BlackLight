BlackLight 新浪微博客户端
---  
请注意，当前BlackLight的开发已经被冻结，直到2016年高考后重启。  
The development progress has been _FROZEN_ till June 2016.  
![Logo](https://raw.githubusercontent.com/PaperAirplane-Dev-Team/BlackLight/master/logo.png)  
[![Build Status](https://travis-ci.org/PaperAirplane-Dev-Team/BlackLight.svg?branch=master)](https://travis-ci.org/PaperAirplane-Dev-Team/BlackLight)  

详细介绍以及Changelog请见Play商店。 <https://play.google.com/store/apps/details?id=us.shandian.blacklight>  
源代码在GPLv3协议下发布。

由于新浪微博已经不再接受新的第三方客户端申请，因此BlackLight使用“黑魔法”的方式登录微博。要登录，您需要一个可用的微博App Key，这里有一个：<https://gist.github.com/PeterCxy/3085799055f63c63c911>  
个人娱乐之作，勿用于任何盈利性用途。  

如果你有意加入本项目，你可以阅读我们的Wiki以及下面的内容来获得有关指导。  

使用Android Studio导入
---
1. `git clone` 本项目到本地
2. 在`Android Studio` 中执行 `Import` 并耐心等待……等待……再等待
3. 调试编译时请选择`app`模块并进行调试编译（若选择`app-snapshot`会发生错误）  
4. Enjoy it!

直接使用Gradle编译
---
1. `git clone` 本项目到本地
2. 在项目根目录下运行 `gradle :app-snapshot:assembleDebug` （注意，不能编译 Release 版的 BlackLight Snapshot ）

使用AIDE导入
---
抱歉，我们不再支持AIDE。

编译非调试版本
---
如果您想编译非调试版本，即 `Release` 版本，请在项目根目录创建 `signing.properties`

```
STORE_FILE=/path/to/your/publish/keystore
STORE_PASSWORD=your_passwd
KEY_ALIAS=your_alias
KEY_PASSWORD=your_passwd
```

然后即可使用 `gradle :app:assembleRelease`


感谢
---
* 四次元(@qii)  
* 碎星iKe(@IssacWong)  
* clowwindy  
* drakeet  
* 日可待（绘制Logo）  
* 某中二的薛定谔之猫
* 小湖爱闹
* 九十九路比|Kenny  
* 加大号的猫


主要开发者
---
* @PeterCxy（码代码）  
* @Harry-Chen（制造bug）  
* @fython（酱油）  
* @xavieryao （编写梗）

纸飞机开发团队 @PaperAirplane-Dev-Team 出品
