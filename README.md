# Take 椅 Easy

## 1. 專題簡介
透過UWB定位技術，實現室內智慧輪椅之應用，並輔以專屬app提供管理介面，和普通用戶的操作介面。

## 2. 環境設置

#### 1. UWB上位機
`./D-DWM-PG測試包/C配置測試包/上位機安裝包/安裝方式1/setup.exe`
可對主機站進行簡單參數配置，並啟動定位程序。

#### 2.2 UWB串口驅動
`./D-DWM-PG測試包/C配置測試包/串口驅動/CP210xVCPInstaller_x86.exe`
用以連接UWB主機站

#### 2.3 Android Studio
[Android Studio](https://developer.android.com/studio)
本應用程式之開發環境。

#### 2.4 MySQL Server
[MySQL](https://dev.mysql.com/downloads/mysql/)
紀錄用戶資料與地點座標資訊。

#### 2.5 MQTT Server
[Mosquitto](https://mosquitto.org/download/)
用以傳遞資料給所有訂閱者。

## 3. 啟動步驟

#### 3.1 啟動智慧輪椅裝置
在樹莓派中，導入`project_1210`目錄，首先初始化萬向輪訊號，將殘留的速度值歸零並轉為煞車狀態。
```bash
cd ./project_1210
python init.py
```
初始化裝置後，便可提供智慧輪椅與樹莓派電源。

#### 3.2 啟動UWB室內定位
在場域內放置基站，範圍之左上角視為原點（座標`(0, 0)`，而x軸向右遞增且y軸向下遞增），並執行`D-DWM-PG-APPV4.5.exe`進行操作：
- 串口設置點擊串口連接。
- 連接設置數值更改為`0`，點擊連接設備。
- 將各基站x與y座標輸入（在此系統中，座標單位皆為公分），點擊配置載入。
- 點擊開始定位。

此外，針對應用的場域，需繪製一平面圖供程式讀取並藉此規劃路徑，規則如下：
- 以像素（Pixel）為單位，每一格像素對應至實際場域之10公分長度。
- 白色（`#000000`）決定場域內可行走的區域範圍，其餘顏色視為不可行走的部分（例如牆壁，障礙物等）。

#### 3.3 架設MQTT伺服器
至MQTT Server的安裝目錄`./mosquitto`， 並執行指令啟動MQTT Server。
```bash
mosquitto -c mosquitto.conf -v
```

#### 3.4 設置資料庫
確認安裝MySQL Server設備之MySQL服務已啟動，並導入`takeiteasy_db.sql`。

#### 3.5 啟動智慧輪椅後端程式
進入主程式`main.py`，分別修改並設定MQTT伺服器的IP位址、訊息接收者與發送者的Topics，以及輪子的初始速度訊號值（預設`255`），並指定先前已繪製完成的平面圖檔案。
```python
MQTT_HOST = '192.168.87.193'
SUBSCRIBER_TOPIC = 'takeiteasyMQTT/1'
PUBLISHER_TOPIC = 'takeiteasy'
SPEED = 255
STATE_SPACE = './env_lib_2.png'
```
設定完畢後，執行主程式，程式將會即時接收應用程式訊號並控制智慧輪椅的行動。
```bash
python main.py
```

#### 3.6 開啟應用程式
使用Android Studio開啟應用程式專案`TakeItEasy`，並查看程式碼（位於`./TakeItEasy/app/src/main/java/com/example/takeiteasy`）。在`MySQLConn.java`中，分別修改並設定MySQL伺服器的IP位址、資料庫名稱、使用者名稱以及密碼。
```java
private static final String MYSQL_HOST = "192.168.43.209:3306";
private static final String DB_NAME = "takeiteasy";
private static final String DB_USER = "manager";
private static final String DB_PASSWORD = "Wheelchair";
```
在`MQTTClient.java`中，分別修改並設定MQTT伺服器的IP位址、訊息接收者與發送者的主題。請注意，在應用程式中，接收者與發送者的主題需與樹莓派程式方顛倒，為使彼此能互相傳遞訊息。
```java
private final String url = "tcp://192.168.43.59:1883";
private final String subscriberTopic = "takeiteasy";
private final String publisherTopic = "takeiteasyMQTT/1";
```
最後，將`./TakeItEasy/app/src/main/res/drawable-v24/env_lib_2.png`檔案替換為先前已繪製完成的平面圖檔案。

#### 3.7 完成
執行應用程式後，輸入帳號與密碼會進入主頁面，點擊目前位置可查看智慧輪椅當前位置與可前往的地點，按下並確認地點後，智慧輪椅即開始前往目標。