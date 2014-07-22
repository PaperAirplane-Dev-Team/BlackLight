BlackLight 新浪微博客户端
---
详细介绍请见 <http://typeblog.net/blacklight>  
源代码在GPLv3协议下发布

由于新浪微博已经不再接受新的第三方客户端申请，因此BlackLight使用“黑魔法”的方式登录微博。客户端内内置了几个可用的APP KEY。  
个人娱乐之作，勿用于任何盈利性用途。

使用BUCK编译
---
[BUCK](https://github.com/facebook/buck) 是Facebook出品的一款编译工具。  
安装好以后，您只需在BlackLight目录下运行 `buck build debug` 即可。  
使用 `buck install debug` 来把编译好的apk安装到手机上。  
请不要尝试使用 `buck build release` 或 `buck install release` ，因为我没有上传发布用的签名文件。

感谢
---
四次元(@qii)  
碎星iKe(@IssacWong)  
AIDE(本客户端采用的开发工具)
