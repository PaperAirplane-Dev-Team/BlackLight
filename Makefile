# This is the Makefile wrapper of gradle
# In order to auto-build in Atom editor
# Atom plugin: build-systems

.PHONY: app-debugapp-install-debug app-clean

app-debug:
	@gradle :app:assembleDebug

app-install:
	@adb install app/build/outputs/apk/app-debug.apk

app-clean:
	@gradle :app:clean
