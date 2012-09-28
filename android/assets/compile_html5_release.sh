#!/bin/bash

rm -rf release
cd ../../server/
bash compile_client_release.sh
cd ../android/assets
cp -r ../../server/release release
