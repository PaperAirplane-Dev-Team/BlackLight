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
DEX_NAME	:= classes.dex
OUT_DEX		:= $(BIN_DIR)/$(DEX_NAME)
OUT_APK		:= $(BIN_DIR)/$(APK_NAME)
CHROME_DIR	:= chrome/vendor/chromium/crx
# Path to directories that contain source
# Including source directories of library projects
SRC_DIR		:= \
	src \
	libs/SlidingUpPanel/src \
	libs/SwipeBackLayout/library/src/main/java \
	libs/HoloColorPicker/src \
	libs/AndroidSwipeLayout/library/src/main/java \
	libs/subsampling-scale-image-view/library/src \
	libs/CircleImageView/circleimageview/src/main/java
# Annonation Library Projects
ANNONATIONS	:= libs/butterknife
# Annonation Processors
PROCESSORS	:= butterknife.internal.ButterKnifeProcessor
# Timestamp file of java sources
# Just a fake "target", doesn't matter in fact
SRC_TS		:= $(BUILD_DIR)/sources.ts
# Path to directories containing resources
# Including library projects
RES_DIR		:= \
	res \
	libs/SlidingUpPanel/res \
	libs/SwipeBackLayout/library/src/main/res \
	libs/HoloColorPicker/res \
	libs/AndroidSwipeLayout/library/src/main/res \
	libs/subsampling-scale-image-view/library/res \
	libs/CircleImageView/circleimageview/src/main/res
# Timestamp file of resources
RES_TS		:= $(BUILD_DIR)/resources.ts
# External packages that need to generate R.java under.
# Usually these are library projects' package names.
# If a library does not contain any resource
# We do not need to put it here.
EXT_PKG		:= \
	com.sothree.slidinguppanel.library \
	me.imid.swipebacklayout.lib \
	com.larswerkman.holocolorpicker \
	com.daimajia.swipe \
	com.davemorrissey.labs.subscaleview \
	de.hdodenhof.circleimageview
# Include all jar libraries needed
# Including android.jar
# Please set the $ANDROID_JAR environment variable
# Pointing to your android.jar
JAR_LIB		:= \
	$(ANDROID_JAR) \
	libs/android-support-v4.jar \
	libs/gson-2.2.2.jar \
	libs/SlidingUpPanel/libs/nineoldandroids-2.4.0.jar
# Asset directory
ASSET		:= assets
# Packages that need to generate BuildConfig.java for.
# If a library needs BuildConfig.java,
# Please put it here also.
PACKAGE		:= us.shandian.blacklight
# Timestamp file of BuildConfig
PKG_TS		:= $(BUILD_DIR)/buildconfig.ts
# The main AndroidManifest
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
GEN			:= $(foreach srcdir, $(shell find $(GEN_DIR) -maxdepth 10 -type d),$(wildcard $(srcdir)/*.java))
RES			:= \
	$(foreach dir, \
		$(RES_DIR) $(ASSET), \
		$(foreach srcdir, \
			$(shell find $(dir) -maxdepth 10 -type d), \
			$(wildcard $(srcdir)/*.*) \
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

# Annonation arguments for javac
ANNO_DIR	:= $(addsuffix /build/classes, $(ANNONATIONS))
ANNO_CLASS	:= $(subst $(TAB),$(EMPTY),\
	$(subst $(SPACE),$(COLON),$(ANNO_DIR)))

JAVAC_CLASS	+= $(COLON)$(ANNO_CLASS)
JAVAC_CLASS := $(subst $(SPACE),$(EMPTY),$(JAVAC_CLASS))

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

define build-info
	@echo -e "\033[33mNOTICE: Please always do 'make clean' before you build release package or after upgrading any library!\033[0m"
	@echo -e "\033[32mNOTICE: Ignore any warnings reported by 'find'. That doesn't matter.\033[0m"
	@echo -e "\033[36mTarget apk path:\033[0m  $(OUT_APK)"
endef

define anno
	$(call target, $1)
	@cd $1 && make classes
endef

define anno-clean
	$(call target, $1)
	@cd $1 && make clean
endef

.PHONY: clean pre merge debug_make release_make debug release install anno anno-clean chrome
# Clean up 
clean: anno-clean
	$(call target, Clean)
	@rm -rf $(BUILD_DIR)

# Prepare build dir
pre:
	$(call build-info)
	$(call target, Environment)
	@mkdir -p $(BIN_DIR)
	@mkdir -p $(GEN_DIR)
	@mkdir -p $(CLASSES_DIR)

# Generate resources
$(RES_TS): $(RES) $(MANIFEST)
	$(call target, Resources)
	@$(AAPT) p -m -M $(MANIFEST) -A $(ASSET) -I $(ANDROID_JAR) $(AAPT_RES) --extra-packages $(AAPT_EXT) --auto-add-overlay -J $(GEN_DIR) -F $(OUT_APK) -f
	@echo $(shell date) > $@

# Generate build config
$(PKG_TS):
	$(call target, BuildConfig)
	$(foreach pkg, $(PACKAGE), $(call gen-cfg,$(subst $(POINT),$(SLASH),$(pkg))))
	@echo $(shell date) > $@

# Call javac to build classes
$(SRC_TS): $(SRC) $(GEN)
	$(call target, Classes)
	@$(MAKE) anno
	@$(JAVAC) -encoding utf-8 -cp $(JAVAC_CLASS) -processor $(PROCESSORS) -d $(CLASSES_DIR) $(SRC) $(GEN)
	@echo $(shell date) > $@

# Convert the classes to dex format
$(OUT_DEX): $(SRC_TS)
	$(call target, Dex)
	@$(DX) --dex --no-strict --output=$(OUT_DEX) $(CLASSES_DIR) $(subst $(ANDROID_JAR) ,$(EMPTY),$(JAR_LIB)) $(ANNO_DIR)

# Merge the dex into apk
merge: $(OUT_DEX)
	$(call target, Merge)
	$(shell $(AAPT) r $(OUT_APK) $(DEX_NAME) > /dev/null)
	@cd $(BIN_DIR) && $(AAPT) a $(APK_NAME) $(DEX_NAME)

# Debug package (do not zipalign)
debug_make: pre $(RES_TS) $(PKG_TS)
	@$(MAKE) merge DEBUG=true
	$(call target, Debug)
	@$(JARSIGNER) -keystore $(KEY_DEBUG) -storepass android -sigalg MD5withRSA -digestalg SHA1 $(OUT_APK) my_alias

# Release package (zipalign)
release_make: pre $(RES_TS) $(PKG_TS)
	@$(MAKE) merge DEBUG=false
	$(call target, Release)
	@$(JARSIGNER) -keystore $(KEY_RELEASE) -sigalg MD5withRSA -digestalg SHA1 $(OUT_APK) $(KEY_ALIAS)
	@$(ZIPALIGN) 4 $(OUT_APK) $(OUT_APK)_zipalign
	@rm -r $(OUT_APK)
	@mv $(OUT_APK)_zipalign $(OUT_APK)

# Wrapper for debug build
debug:
	@$(MAKE) debug_make DEBUG=true

# Wrapper for release build
release:
	@$(MAKE) release_make DEBUG=false

# Install on phone
install:
	$(call target, Install)
	@if [ -f $(PM) ]; then \
		$(PM) install -r $(OUT_APK);\
	else \
		$(ADB) install -r $(OUT_APK);\
	fi

# Chrome Plugin
chrome:
	rm -rf $(CHROME_DIR)
	mkdir -p $(CHROME_DIR)
	cp $(OUT_APK) $(CHROME_DIR)

# Annonations
anno:
	$(foreach lib, $(ANNONATIONS), $(call anno, $(lib)))

# Clean up annonations
anno-clean:
	$(foreach lib, $(ANNONATIONS), $(call anno-clean, $(lib)))
