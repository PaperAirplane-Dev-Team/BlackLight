BlackLight 新浪微博客户端
---
![Logo](https://raw.githubusercontent.com/PaperAirplane-Dev-Team/BlackLight/master/res/drawable-xxhdpi/ic_launcher.png)   
详细介绍请见 <http://typeblog.net/blacklight>  
Play商店 <https://play.google.com/store/apps/details?id=us.shandian.blacklight>  
源代码在GPLv3协议下发布

由于新浪微博已经不再接受新的第三方客户端申请，因此BlackLight使用“黑魔法”的方式登录微博。客户端内内置了几个可用的APP KEY。  
个人娱乐之作，勿用于任何盈利性用途。  

如果你有意加入本项目，你可以阅读我们的Wiki以及下面的内容来获得有关指导。  

用GNU Make编译
---
你可以使用我编写的 `Makefile` 来编译。  
如果你要在手机上编译，请阅读 <http://typeblog.net/tech/2014/07/30/build-android-app-on-android.html>。在电脑上只需普通Linux bash环境即可。  
1. 设置 `$ANDROID_JAR` 环境变量指向你的 `android.jar`。(其实这一步是为Android上编译设计的，因为Android上不是标准的sdk目录)  
2. 确保您的 `$PATH` 环境变量中包含 `Makefile` 开头定义的那些命令。如果你在电脑上编译则可以忽略 `pm`，在手机上编译则可以忽略 `adb`。  
3. 切换到 `BlackLight` 目录，运行 `make debug` ，即可开始编译。编译完成后的apk文件位于 `build/bin/build.apk`。  
4. 请*__不要__*尝试使用 `make release`，因为我没有上传发布用的签名。  
5. 编译完成后，可使用 `make install` 命令安装到手机上。  

用Eclipse导入&编译
---
1. __首先请保证你的Android开发环境拥有API Level 20的开发平台。__   
2. 将项目Clone到一个临时文件夹(不能是Eclipse的Workspace中)。
3. 执行`git checkout eclipse` (这是一个*__仅编译__* 分支，除了必要的项目格式转换，请勿在此分支上贡献代码。我们将不会接受该分支的任何Pull Request)
4. 在Eclipse中*__仅仅导入__* BlackLight项目，请不要选择子项目，并选中 'Copy projects into workspace' 选项。
5. 在资源管理器中定位到Eclipse的Workspace目录，记下BlackLight项目的路径。
6. 在Eclipse中进行导入操作，填入第4步记下的路径，导入所有的子项目。*__不要选中__* 'Copy projects into workspace' 选项。
7. 如果需要，重启Eclipse并Clean所有项目。
8. Enjoy it!

感谢
---
四次元(@qii)  
碎星iKe(@IssacWong)


开发者
---
@PeterCxy  
@Harry-Chen  
@fython  
@一抔学渣  

纸飞机开发团队 @PaperAirplane-Dev-Team 出品
