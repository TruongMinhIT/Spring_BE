#!/bin/bash
SERVER_DEPLOY=192.168.50.9
TARGET_DIR=/opt/deploy/sellmgr/media
APP_ID=sellmgr-media

rm -rf mediaBuild
mkdir mediaBuild
cd mediaBuild

echo "Build source..."

git clone ssh://git@git.developteam.net:2222/tsa/org-media-template.git
# git checkout master

# Copy log cfg
rm -rf org-media-template/source/org-media/src/main/resources/log4j2.xml
cp ../media/logback-spring.xml org-media-template/source/org-media/src/main/resources/

cd org-media-template/source/org-media
mvn clean package

cd ../../../

echo "Update config..."
mkdir release

cp org-media-template/source/org-media/target/org-media-spring-boot.jar release/app.jar

cp ../media/application.properties release/application.properties
cp ../media/service-template.service release/$APP_ID.service
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
ssh root@$SERVER_DEPLOY "cd $TARGET_DIR && cp ../cfg/media/application-prod.prop application-prod.properties"

echo " ---> Deploy new service..."
ssh root@$SERVER_DEPLOY "mv $TARGET_DIR/$APP_ID.service /lib/systemd/system/$APP_ID.service"
ssh root@$SERVER_DEPLOY "chmod 644 /lib/systemd/system/$APP_ID.service && systemctl daemon-reload"
ssh root@$SERVER_DEPLOY "systemctl enable $APP_ID.service"
ssh root@$SERVER_DEPLOY "systemctl start $APP_ID.service"

echo "Cleanup..."
cd ..
rm -rf mediaBuild
echo "############# DONE #############"