#!/usr/bin/expect --
spawn adb shell
expect "$" {
    sleep 0.1
    send "echo 'export TRAVIS=true' >> /system/etc/mkshrc;\n"
    send "cat /system/etc/mkshrc;\n"
    exit;
}
interact
