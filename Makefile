# Makefile for BlackLight
# Build tools
AAPT		:= aapt				# Android Asset Packaging Tool
JAVAC		:= javac			# Java Compiler
DX			:= dx				# Dex tool
JARSIGNER	:= jarsigner		# Jar signing tool
ZIPALIGN	:= zipalign			# Zip aligning tool
MAKE		:= make				# GNU Make tool
ADB			:= adb				# Android Debug Bridge
PM			:= /system/bin/pm	# Package Manager on Android
# You do not need PM if you are building on PC.

# Build configs
BUILD_DIR	:= build
BIN_DIR		:= $(BUILD_DIR)/bin
GEN_DIR		:= $(BUILD_DIR)/gen
CLASSES_DIR	:= $(BIN_DIR)/classes
APK_NAME	:= build.apk
OUT_APK		:= $(BIN_DIR)/$(APK_NAME)
SRC_DIR		:= \
	src \
	libs/SlidingUpPanel/src \
	libs/SystemBarTint/src \
	libs/SwipeBackLayout/library/src/main/java
RES_DIR		:= \
	res \
	libs/SlidingUpPanel/res \
	libs/SwipeBackLayout/library/src/main/res
EXT_PKG		:= \
	com.sothree.slidinguppanel.library \
	me.imid.swipebacklayout.lib
JAR_LIB		:= \
	$(ANDROID_JAR) \
	libs/android-support-v4.jar \
	libs/gson-2.2.2.jar \
	libs/SlidingUpPanel/libs/nineoldandroids-2.4.0.jar
ASSET		:= assets
PACKAGE		:= us.shandian.blacklight
MANIFEST	:= AndroidManifest.xml

# Keystores
KEY_DEBUG	:= keystore/debug.keystore # Provided by Android SDK
KEY_RELEASE	:= keystore/publish.keystore
KEY_ALIAS	:= peter # Key alias for relase keystore

# Source list
SRC			:= \
	$(foreach dir, \
		$(SRC_DIR), \
		$(foreach srcdir, \
			$(shell find $(dir) -maxdepth 10 -type d), \
			$(wildcard $(srcdir)/*.java) \
		 ) \
	 )

# Some stuff
EMPTY		:=
SPACE		:= $(EMPTY) $(EMPTY)
TAB			:= $(EMPTY)	$(EMPTY)
COLON		:= $(EMPTY):$(EMPTY)
POINT		:= $(EMPTY).$(EMPTY)
SLASH		:= $(EMPTY)/$(EMPTY)

# Resource arguments for aapt
AAPT_RES	:= $(addprefix -S , $(RES_DIR))
AAPT_EXT	:= $(subst $(TAB),$(EMPTY),\
	$(subst $(SPACE),$(COLON),$(EXT_PKG)))

# Classpath arguments for javac
JAVAC_CLASS	:= $(subst $(TAB),$(EMPTY),\
	$(subst $(SPACE),$(COLON),$(JAR_LIB)))

# Default DEBUG Flag
ifndef DEBUG
	DEBUG	:= true
endif

# Make rules
define gen-cfg
	@mkdir -p $(GEN_DIR)/$1
	@echo -e "package $(PACKAGE);\npublic class BuildConfig {\n	public static final boolean DEBUG=$(DEBUG);\n}" > "$(GEN_DIR)/$1/BuildConfig.java"
endef

define target
	@echo -e "\033[36mBuilding target:\033[0m $1"
endef

.PHONY: clean res pre cfg class dex merge debug install
# Clean up 
clean:
	$(call target, Clean)
	@rm -rf $(BUILD_DIR)

# Prepare build dir
pre:
	$(call target, Environment)
	@mkdir -p $(BIN_DIR)
	@mkdir -p $(GEN_DIR)
	@mkdir -p $(CLASSES_DIR)

# Generate resources
res: pre
	$(call target, Resources)
	@$(AAPT) p -m -M $(MANIFEST) -A $(ASSET) -I $(ANDROID_JAR) $(AAPT_RES) --extra-packages $(AAPT_EXT) --auto-add-overlay -J $(GEN_DIR) -F $(OUT_APK) -f

# Generate build config
cfg: pre
	$(call target, BuildConfig)
	$(foreach pkg, $(PACKAGE), $(call gen-cfg,$(subst $(POINT),$(SLASH),$(pkg))))

# Call javac to build classes
class: pre res cfg
	$(call target, Classes)
	@$(JAVAC) -encoding utf-8 -cp $(JAVAC_CLASS) -d $(CLASSES_DIR) $(SRC) $(foreach srcdir, $(shell find $(GEN_DIR) -maxdepth 10 -type d),$(wildcard $(srcdir)/*.java))

# Convert the classes to dex format
dex: class
	$(call target, Dex)
	@$(DX) --dex --no-strict --output=$(BIN_DIR)/classes.dex $(CLASSES_DIR) $(subst $(ANDROID_JAR) ,$(EMPTY),$(JAR_LIB))

# Merge the dex into apk
merge: dex
	$(call target, Merge)
	@cd $(BIN_DIR) && aapt a $(APK_NAME) classes.dex

# Debug package (do not zipalign)
debug:
	$(call target, Debug)
	@$(MAKE) merge DEBUG=true
	@$(JARSIGNER) -keystore $(KEY_DEBUG) -storepass android -sigalg MD5withRSA -digestalg SHA1 $(OUT_APK) my_alias

# Release package (zipalign)
release:
	$(call target, Release)
	@$(MAKE) merge DEBUG=false
	@$(JARSIGNER) -keystore $(KEY_RELEASE) -sigalg MD5withRSA -digestalg SHA1 $(OUT_APK) $(KEY_ALIAS)
	@$(ZIPALIGN) 4 $(OUT_APK) $(OUT_APK)_zipalign
	@rm -r $(OUT_APK)
	@mv $(OUT_APK)_zipalign $(OUT_APK)

# Install on phone
install:
	$(call target, Install)
	@if [ -f $(PM) ]; then \
		$(PM) install -r $(OUT_APK);\
	else \
		$(ADB) install -r $(OUT_APK);\
	fi
