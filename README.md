#Deploy
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
