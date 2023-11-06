BUCKET=${1:?"Bucket name is required"}
cd ../
./gradlew shadowJar
test $? -eq 0 || exit
gcloud storage cp ../build/libs/gcloud-instance-manager.jar/ gs://$BUCKET/