#!/bin/bash
# Sell Demo - https://cms-selldemo.thsolution.net
SERVER_DEPLOY=192.168.30.12 

# Sell Itz - https://cms-sellmgr.thsolution.net
#SERVER_DEPLOY=192.168.50.9

TARGET_DIR=/opt/deploy/sellmgr/api
APP_ID=sellmgr-api

echo "Build source..."
cd ../source/sell-mgr-api
mvn clean package
cd ../../deploy

echo "Update config..."
mkdir release

cp ../source/sell-mgr-api/target/sell-mgr-api-1.0.0.jar release/app.jar

cp config/* release/
cp service-template.service release/$APP_ID.service
sed -i '' "s/{CONFIG_LOCATION}/$(printf '%s\n' "$TARGET_DIR" | sed -e 's/[]\/$*.^[]/\\&/g')/g" release/$APP_ID.service
sed -i '' "s/{ENV}/prod/g" release/$APP_ID.service
sed -i '' "s/{ENV}/prod/g" release/application.properties



echo "Compress source..."
gtar -czf api.tar.gz release

echo "Deploy to server... $SERVER_DEPLOY"
echo " ---> Stop old service..."
ssh root@$SERVER_DEPLOY "mkdir -p $TARGET_DIR"
ssh root@$SERVER_DEPLOY "systemctl stop $APP_ID.service"
ssh root@$SERVER_DEPLOY "rm -rf $TARGET_DIR/*"

echo " ---> Remove old service"
ssh root@$SERVER_DEPLOY "rm -rf /lib/systemd/system/$APP_ID.service"

echo " ---> Upload build..."
scp api.tar.gz root@$SERVER_DEPLOY:$TARGET_DIR/api.tar.gz
ssh root@$SERVER_DEPLOY "cd $TARGET_DIR && tar -xzf api.tar.gz && rm -rf api.tar.gz && mv release/* . && rm -rf release"
ssh root@$SERVER_DEPLOY "cd $TARGET_DIR && cp ../cfg/api/application-prod.prop application-prod.properties"

echo " ---> Deploy new service..."
ssh root@$SERVER_DEPLOY "mv $TARGET_DIR/$APP_ID.service /lib/systemd/system/$APP_ID.service"
ssh root@$SERVER_DEPLOY "chmod 644 /lib/systemd/system/$APP_ID.service && systemctl daemon-reload"
ssh root@$SERVER_DEPLOY "systemctl enable $APP_ID.service"
ssh root@$SERVER_DEPLOY "systemctl start $APP_ID.service"

echo "Cleanup..."
rm -rf release
rm -rf api.tar.gz
echo "############# DONE #############"