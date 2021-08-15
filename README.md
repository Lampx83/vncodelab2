#Deploy lên server tomcat
Cập nhật apt
```
sudo apt update
sudo apt upgrade
```
Cài git
```aidl
sudo apt install git
```


Cài
```aidl
sudo apt-get install lsof
```

Cài maven
```aidl
sudo apt install maven
```


Cài golang
```aidl
sudo apt install golang







Cài Java
```
sudo apt-get install default-jdk
```

Tải Tomcat
```
wget https://mirrors.ocf.berkeley.edu/apache/tomcat/tomcat-9/v9.0.52/bin/apache-tomcat-9.0.52.tar.gz
```
Giải nén Tomcat
```aidl
tar xzf apache-tomcat-9.0.52.tar.gz
```
Chuyển tomcat vào thư mục
```aidl
sudo mv apache-tomcat-9.0.50 /usr/local/tomcat9
```

Thiết lập các biến môi trường
```aidl
echo "export CATALINA_HOME="/usr/local/tomcat9"" >> ~/.bashrc
echo "export JAVA_HOME="/usr/lib/jvm/java-11-oracle"" >> ~/.bashrc
echo "export JRE_HOME="/usr/lib/jvm/java-11-oracle"" >> ~/.bashrc
source ~/.bashrc
```


Hướng dẫn cách deploy ứng dụng trên Google Cloud
##Compute engine
###Vào thư mục vncodelab2
```
cd vncodelab2
git pull
```
###Stop port 80 (bỏ dấu source)
```
sudo kill -9 $(sudo lsof -t -i:80)
sudo kill -9 $(sudo lsof -t -i:8080)
sudo kill -9 $(sudo lsof -t -i:8443)
```
###Bật dịch vụ Web
```
sudo nohup mvn spring-boot:run &
```
## Google Appengine
###Kiểm tra xem đang ở project nào:
Tạo thư mục appengine và file app.yaml
Sử dụng plugin appengine-maven-plugin, khai báo trong pom.xml
###Kiểm tra xem đang ở project nào:
```
gcloud config get-value project
```
###Chuyển sang ứng dụng:
```
gcloud config set project vncodelab
```
###Deploy ứng dụng:
```
mvn clean package appengine:deploy -Dapp.deploy.promote=false -Dapp.deploy.version=ver1
```
###To view your app, use command:
```
gcloud app browse clear
```
