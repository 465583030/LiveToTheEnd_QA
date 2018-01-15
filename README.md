# LiveToTheEnd

百万英雄(西瓜视频)/芝士超人/花椒直播/冲顶大会/一直播/YY 答题赢现金奖励 自动搜索答案

## 安装环境

- 安装Java
- 安装adb

安装方法这里不再赘述

## 填写配置文件

`target`目录里有打包好的`money.jar`，首次运行需要生成配置文件，
运行以下命令：

```
java -jar money.jar
```

会在同目录生成`config.properties`，然后填写相应的配置。

配置内容如下所示：

- SCREEN_WIDTH = 手机屏幕分辨率宽
- SCREEN_HEIGHT = 手机屏幕分辨率高
- PROBLEM_AREA_X = 答题区域（可自动生成）
- PROBLEM_AREA_Y = 答题区域（可自动生成）
- PROBLEM_AREA_WIDTH = 答题区域（可自动生成）
- PROBLEM_AREA_HEIGHT = 答题区域（可自动生成）
- ADB_PATH = adb完整安装路径（Windows用户注意转义字符）
- IMAGE_TEMP_PATH = 图片缓存路径（Windows用户注意转义字符）
- BD_OCR_APP_ID = 百度OCR AppID
- BD_OCR_API_KEY = 百度OCR API Key
- BD_OCR_API_TOKEN = 百度OCR Secret Key


[百度OCR](https://cloud.baidu.com/product/ocr)

## 识别答题区域

```
java -jar money.jar -auto-config 图片路径
```

图片为答题时的截图，请发送到电脑上进行配置。也可以使用默认配置，提取答题区域是为了让OCR更快的识别出文字。

也可以不指定图片路径，需要手机停留在答题页面，数据线连接着电脑，然后回车。

```
java -jar money.jar -auto-config
```

## 运行

请打开手机USB调试。

在命令行输入：

```
java -jar money.jar
```

运行后程序会等待用户输入，题目出来的时候输入 `p` ，敲回车，会展示搜索结果并打开浏览器百度搜索。

根据搜索的 `问题+选项` 的结果个数进行排序，并输出。这样的方法往往不太准确，后期打算对字符串进行匹配，提高准确率。

输入`exit`终止程序。

## BTW

此程序只是简单的搜索，结果仅做参考。

省去的主要是手动打字进行搜索的时间。

自己测试基本都是五秒内出结果。


