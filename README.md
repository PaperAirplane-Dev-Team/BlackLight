BlackLight 新浪微博客户端
---
详细介绍请见 <http://typeblog.net/blacklight>  
源代码在GPLv3协议下发布

由于新浪微博已经不再接受新的第三方客户端申请，因此BlackLight使用“黑魔法”的方式登录微博。客户端内内置了几个可用的APP KEY。  
个人娱乐之作，勿用于任何盈利性用途。

使用BUCK编译
---
[BUCK](https://github.com/facebook/buck) 是Facebook出品的一款编译工具。  
请确保您的 `$ANDROID_HOME` 环境变量指向正确的Android SDK目录。  
安装好以后，您只需在BlackLight目录下运行 `buck build debug` 即可。  
使用 `buck install debug` 来把编译好的apk安装到手机上。  
请不要尝试使用 `buck build release` 或 `buck install release` ，因为我没有上传发布用的签名文件。

用Shell脚本编译
---
你可以使用我编写的 `build.sh` 来编译。这个脚本主要是为了在手机上编译而编写的，但是在电脑上也可以运行。  
如果你要在手机上编译，请阅读 <http://typeblog.net/tech/2014/07/30/build-android-app-on-android.html>。在电脑上只需普通Linux bash环境即可。  
首先，设置 `$ANDROID_JAR` 环境变量指向你的 `android.jar`。(其实这一步是为Android上编译设计的，因为Android上不是标准的sdk目录)  
然后，确保您的 `aapt` `dx` `javac` `jarsigner` 四个命令可以直接调用。如果不行，尝试把它们符号链接到 `/usr/bin` 或者加入 `$PATH`。如果你用的是Android手机且运行的是 `Terminal IDE` 环境，那么这四个命令应当原本就可以成功调用。  
接下来，切换到 `BlackLight` 目录，运行 `sh build.sh debug` 或 `./build.sh debug`，即可开始编译。编译完成后的apk文件位于 `build/bin/build.apk`。请不要尝试使用 `sh build.sh release` 或 `./build.sh release` ，因为我没有上传发布用的签名文件。在Android上，请只使用 `sh build.sh debug`。  

感谢
---
四次元(@qii)  
碎星iKe(@IssacWong)
