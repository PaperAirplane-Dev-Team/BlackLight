BlackLight 新浪微博客户端
---
详细介绍请见 <http://typeblog.net/blacklight>  
Play商店 <https://play.google.com/store/apps/details?id=us.shandian.blacklight>  
源代码在GPLv3协议下发布

由于新浪微博已经不再接受新的第三方客户端申请，因此BlackLight使用“黑魔法”的方式登录微博。客户端内内置了几个可用的APP KEY。  
个人娱乐之作，勿用于任何盈利性用途。

用Make编译
---
你可以使用我编写的 `Makefile` 来编译。  
如果你要在手机上编译，请阅读 <http://typeblog.net/tech/2014/07/30/build-android-app-on-android.html>。在电脑上只需普通Linux bash环境即可。  
首先，设置 `$ANDROID_JAR` 环境变量指向你的 `android.jar`。(其实这一步是为Android上编译设计的，因为Android上不是标准的sdk目录)  
然后，确保您的 `$PATH` 环境变量中包含 `Makefile` 开头定义的那些命令。如果你在电脑上编译则可以忽略 `pm`，在手机上编译则可以忽略 `adb`。  
接下来，切换到 `BlackLight` 目录，运行 `make debug` ，即可开始编译。编译完成后的apk文件位于 `build/bin/build.apk`。请不要尝试使用 `make release`，因为我没有上传发布用的签名。编译完成后，可使用 `make install` 命令安装到手机上。  

感谢
---
四次元(@qii)  
碎星iKe(@IssacWong)
