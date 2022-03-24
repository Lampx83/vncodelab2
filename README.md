#Deploy lên server tomcat
Cập nhật apt
```
sudo apt update
```
Cài đặt, và chạy
```
sudo apt install git
sudo apt-get install lsof
sudo apt install maven
sudo apt install golang
go get github.com/googlecodelabs/tools/claat
git clone https://github.com/Lampx83/vncodelab2.git
cd vncodelab2
sudo mvn spring-boot:run
ss -ltn  => kiểm tra cổng
$ sudo ufw allow from any to any port 8080 proto tcp  => Mở cổng 8080 nếu cần
mongod --dbpath /Users/xuanlam/data/db 
```
ps -ef|grep -E "apache|httpd" | grep -v "grep"

# Deploy

###Stop ports
``` 
cd vncodelab
git pull
sudo kill -9 $(sudo lsof -t -i:80)
sudo kill -9 $(sudo lsof -t -i:443)
sudo nohup mvn spring-boot:run &
```
###Bật dịch vụ Web
```
git pull
sudo kill -9 $(sudo lsof -t -i:80)
sudo kill -9 $(sudo lsof -t -i:443)
sudo mvn spring-boot:run
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