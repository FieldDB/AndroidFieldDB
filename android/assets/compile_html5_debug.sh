#!/bin/bash

echo ""
echo ""
echo "Compiling handlebars"
cd ../../server/
bash compile_handlebars.sh

echo ""
echo ""
echo "Copying files to android assets"
cd ../android/assets
rm -rf release
cp -r ../../server/public release
