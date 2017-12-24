#!/bin/bash

rm -rf build/JCloisterZone
mkdir -p build/JCloisterZone

mv build/plugins build/JCloisterZone
mv build/JCloisterZone.jar build/JCloisterZone

chmod a+x build/JCloisterZone/JCloisterZone.jar
cp ../JCloisterZone-plugins/builds/* build/JCloisterZone/plugins

# temporary
rm build/JCloisterZone/plugins/newart.jar

cd build
# zip -r -9 JCloisterZone-$1.zip JCloisterZone
tar cvzf JCloisterZone-$1.tgz JCloisterZone
7z a JCloisterZone-$1.7z JCloisterZone