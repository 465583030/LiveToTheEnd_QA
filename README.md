# LiveToTheEnd

百万英雄(西瓜视频)/芝士超人/花椒直播/冲顶大会/一直播/YY语音/好看视频/百万富翁/大白汽车分期/UC浏览器/蘑菇大富翁/优酷/作业帮/网易新闻 答题赢现金奖励 自动搜索答案

## 安装环境

- 安装Java
- 安装adb or 安卓模拟器

（adb获取截图较慢，建议使用安卓模拟器）

安装方法这里不再赘述。

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
- PROBLEM_AREA_X = 答题区域左上角横坐标（可自动生成）
- PROBLEM_AREA_Y = 答题区域左上角纵坐标（可自动生成）
- PROBLEM_AREA_WIDTH = 答题区域宽度（可自动生成）
- PROBLEM_AREA_HEIGHT = 答题区域高度（可自动生成）
- ADB_PATH = adb完整安装路径（Windows用户注意转义字符）如：`C\:\\adbpath\\adb`
- IMAGE_TEMP_PATH = 图片缓存路径（Windows用户注意转义字符）`C\:\\picturepath\\`
- BD_OCR_APP_ID = 百度OCR AppID
- BD_OCR_API_KEY = 百度OCR API Key
- BD_OCR_API_TOKEN = 百度OCR Secret Key

注意以上自动配置只支持adb截图，如果只使用模拟器，则需要自己填写答题区域的坐标。

[百度OCR](https://cloud.baidu.com/product/ocr)

## 识别答题区域（真机，adb）

```
java -jar money.jar -auto-config 图片路径
```

图片为答题时的截图，请发送到电脑上进行配置。也可以使用默认配置，提取答题区域是为了让OCR更快的识别出文字。

也可以不指定图片路径，需要手机停留在答题页面，通过数据线连接电脑，按回车键后程序自动获取截图并填写坐标信息。

```
java -jar money.jar -auto-config
```

## 识别答题区域（安卓模拟器）

暂不支持，请手动填写。

## 运行

### 真机
请打开手机USB调试。

在命令行输入：

```
java -jar money.jar
```

### 安卓模拟器

在命令行输入：

```
java -jar money.jar -C <配置文件相对路径> -D -M3
```

### 网络截图

`-M3`表示一个问题最多3个选项，`-M4`表示最多四个选项。
运行后程序会等待用户输入，题目出来的时候输入 `p` ，敲回车，会展示搜索结果并打开浏览器百度搜索。

根据搜索的 `n(问题+选项)/n(选项)` 的大小进行排序，并输出最可能的选项（`n`表示搜索结果个数），如果题目中含有否定词，则需自己选择最小值。还会输出每个选项的后验概率(百分比的形式)以及`n(问题+选项)`的相对值。这样的方法不是很准确，后期打算对字符串进行匹配，提高准确率。

输入`exit`终止程序。

## 测试

adb基本都是五秒内出结果。

模拟器1秒左右。

安卓APP截图1秒左右。

## END

此程序主要省去的是手动打字进行搜索的时间，结果仅作参考。




