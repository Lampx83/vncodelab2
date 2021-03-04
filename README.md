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