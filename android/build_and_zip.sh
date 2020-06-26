#!/bin/bash

ti build -p android --build-only
unzip -o dist/mve.notification-android-*.zip -d ../example_not_included/

