#Deploy
## Google Appengine
Kiểm tra xem đang ở project nào:
```
gcloud config get-value project
```
Chuyển sang ứng dụng:
```
gcloud config set project vncodelab
```
Deploy ứng dụng:
```
mvn clean package appengine:deploy -Dapp.deploy.promote=false -Dapp.deploy.version=ver1
```
To view your app, use command:
```
gcloud app browse clear
```
#Compute engine
Stop port 80
```
sudo kill -9 $(sudo lsof -t -i:80)
```
Bật dịch vụ Web
```
sudo nohup mvn spring-boot:run &
```