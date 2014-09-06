# Makefile for ButterKnife
# This is an annonation library
JAVAC		:= javac	# Java compiler
MAKE		:= make		# GNU Makefile

# Build Configs
BUILD_DIR	:= build
CLASS_PATH	:= $(BUILD_DIR)/classes
SRC_DIR		:= butterknife/src/main/java
JAR_LIB		:= $(ANDROID_JAR)
SRC_TS		:= $(BUILD_DIR)/sources.ts

# Source list
SRC			:= \
	$(foreach dir, \
		$(SRC_DIR), \
		$(foreach srcdir, \
			$(shell find $(dir) -maxdepth 10 -type d), \
			$(wildcard $(srcdir)/*.java) \
		 ) \
	 )

# Classpath arguments for javac
JAVAC_CLASS	:= $(subst $(TAB),$(EMPTY),\
	$(subst $(SPACE),$(COLON),$(JAR_LIB)))

# Make rules
define target
	@echo -e "	\033[36mBuilding target:\033[0m $1"
endef

.PHONY: clean pre classes

# Clean up
clean:
	$(call target, Clean)
	@rm -rf $(BUILD_DIR)

# Prepare
pre:
	$(call target, Environment)
	@mkdir -p $(CLASS_PATH)

# Classes
classes:
	@$(MAKE) $(SRC_TS)
	
$(SRC_TS):
	$(call target, Classes)
	@$(MAKE) pre
	@$(JAVAC) -encoding utf-8 -cp $(JAVAC_CLASS) -d $(CLASS_PATH) $(SRC)
	@echo $(shell date) > $@
