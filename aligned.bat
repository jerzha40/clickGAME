keytool -genkey -v -keystore my-release-key.keystore -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
unzip -q android-release-unsigned.apk -d temp_apk
cd temp_apk
zip -n "resources.arsc" -qr ../android-release-unsigned-uncompressed.apk *
cd ..
zipalign -f -p 4 android-release-unsigned-uncompressed.apk android-release-aligned.apk
apksigner sign --ks my-release-key.keystore --ks-key-alias my-key-alias --out android-release-signed.apk android-release-aligned.apk
adb -s 7f1184f8 install -r android-release-signed.apk
