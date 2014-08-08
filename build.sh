#!/bin/bash
 
function scandir() {
	local cur_dir parent_dir workdir
	workdir=$1
	cd ${workdir}
	if [ ${workdir} = "/" ]
	then
		cur_dir=""
	else
		cur_dir=$(pwd)
	fi
 
	for dirlist in $(ls ${cur_dir})
	do
		if test -d ${dirlist};then
			cd ${dirlist}
			scandir ${cur_dir}/${dirlist}
			cd ..
		else
			echo ${cur_dir}/${dirlist}
			echo "\n"
		fi
	done
}

function usage() {
	echo "usage: sh build.sh [debug|release]"
}

# Verification
if [ $# != "1" ]; then
	usage
	exit 1
elif [ $1 != "debug" ] && [ $1 != "release" ]; then
	usage
	exit 1
fi

# Clean up
echo 'Cleanning up...'
rm -rf build

# Init
echo 'Initializing build environment...'
mkdir build
mkdir build/gen
mkdir build/bin
mkdir build/bin/classes

# Configurations
res="-S res -S libs/SlidingUpPanel/res -S libs/SwipeBackLayout/library/src/main/res" # Resources
ext_pkg='com.sothree.slidinguppanel.library:me.imid.swipebacklayout.lib' # Package that needs resources
src='src libs/SlidingUpPanel/src libs/SystemBarTint/src libs/SwipeBackLayout/library/src/main/java build/gen' # Sources
jar="$ANDROID_JAR:libs/android-support-v4.jar:libs/gson-2.2.2.jar:libs/SlidingUpPanel/libs/nineoldandroids-2.4.0.jar" # JARs
manifest='AndroidManifest.xml' # Manifest
assets='assets' # Assets
pkgs='us.shandian.blacklight' # Packages that needs to generate BuildConfig

# Run aapt
echo 'Compiling resources...'
aapt p -m -M $manifest -A $assets -I $ANDROID_JAR $res --extra-packages $ext_pkg --auto-add-overlay -J build/gen -F build/bin/build.apk

# Generate BuildConfig
echo 'Generating BuildConfig...'
if [ ${1} = "debug" ]; then
	flag="true"
elif [ ${1} = "release" ]; then
	flag="false"
fi
for pkg in $pkgs
do
	path="build/gen/${pkg//.//}"
	mkdir -p $path
	echo -e "package $pkg;\npublic class BuildConfig {\n	public static final boolean DEBUG=$flag;\n}" >> "$path/BuildConfig.java"
done

# Get list of sources
echo 'Generating list of sources...'
for dir in $src
do
	echo -e `scandir $dir` >> build/bin/sources.list
done

# Run javac
echo 'Compiling Java sources...'
javac -encoding utf-8 -cp $jar @build/bin/sources.list -d build/bin/classes

# Dex
echo 'Dexing...'
jar=${jar//:/ }
jar=${jar//$ANDROID_JAR/}
dx --dex --no-strict --output=build/bin/classes.dex build/bin/classes $jar

# Merge the dex and the apk
echo 'Merging...'
cd build/bin
aapt a build.apk classes.dex
cd ../..

# Sign
echo 'Signing...'
if [ ${1} = "debug" ]; then
	jarsigner -keystore keystore/debug.keystore -storepass android -sigalg MD5withRSA -digestalg SHA1 build/bin/build.apk my_alias
elif [ ${1} = "release" ]; then
	# Jarsigner will ask me for my passwords ^_^
	jarsigner -keystore keystore/publish.keystore -sigalg MD5withRSA -digestalg SHA1 build/bin/build.apk peter

	# Zipalign
	echo "Zipaligning..."
	zipalign 4 build/bin/build.apk build/bin/build_aligned.apk
	rm -r build/bin/build.apk
	mv build/bin/build_aligned.apk build/bin/build.apk
fi

# Finished
echo "Apk built for $1: ${PWD}/build/bin/build.apk"

# If built debug version, install it
if [ ${1} = "debug" ]; then
	echo 'Installing...'
	if [ -f "/system/build.prop" ]; then
		# Install on an Android device
		pm install -r "${PWD}/build/bin/build.apk"
	else
		# Install from a PC
		adb install -r "${PWD}/build/bin/build.apk"
	fi
fi
