# This is the Makefile wrapper of gradle
# In order to auto-build in Atom editor
# Atom plugin: build-systems

.PHONY: app-debug app-release app-install-debug app-clean

app-debug:
	@gradle :app:assembleDebug

app-release:
	@gradle :app:assembleRelease

app-install:
	@adb install -r app/build/outputs/apk/app-debug.apk

app-clean:
	@gradle :app:clean
