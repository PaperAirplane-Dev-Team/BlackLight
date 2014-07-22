# BlackLight Debug Binary
android_binary(
  name = 'debug',
  package_type = 'DEBUG',
  manifest = './AndroidManifest.xml',
  target = 'android-19',
  keystore = ':debug_keystore',
  deps = [
    ':bl-res',
    ':bl-src',
    ':sup-res',
  ],
)

# BlackLight Release Binary
android_binary(
  name = 'release',
  package_type = 'RELEASE',
  manifest = './AndroidManifest.xml',
  target = 'android-19',
  keystore = ':release_keystore',
  deps = [
    ':bl-res',
    ':bl-src',
    ':sup-res',
  ],
)


# BlackLight Resources
android_resource(
  name = 'bl-res',
  res = './res',
  package = 'us.shandian.blacklight',
  deps = [ ':sup-res' ],
  visibility = [ 'PUBLIC' ],
)

# BlackLight Source Code
android_library(
  name = 'bl-src',
  srcs = glob(['./src/**/*.java']),
  deps = [
    ':build_config',
    ':bl-res',
    ':sup-src',
    ':sbt-src',
    ':gson',
    ':android-support-v4',
  ],
)

# BlackLight Build Config
android_build_config(
  name = 'build_config',
  package = 'us.shandian.blacklight',
)

# Android Support Library v4
prebuilt_jar(
  name = 'android-support-v4',
  binary_jar = './libs/android-support-v4.jar',
  visibility = [ 'PUBLIC' ],
)

# Gson Library
prebuilt_jar(
  name = 'gson',
  binary_jar = './libs/gson-2.2.2.jar',
  visibility = [ 'PUBLIC' ],
)

# NineOldAndroids Libaray needed by SlidingUpPanel
prebuilt_jar(
  name = 'nineoldandroids',
  binary_jar = './libs/SlidingUpPanel/libs/nineoldandroids-2.4.0.jar',
  visibility = [ 'PUBLIC' ],
)

# SlidingUpPanel Resources
android_resource(
  name = 'sup-res',
  res = './libs/SlidingUpPanel/res',
  package = 'com.sothree.slidinguppanel.library',
  visibility = [ 'PUBLIC' ],
)

# SlidingUpPanel Source Code
android_library(
  name = 'sup-src',
  srcs = glob(['./libs/SlidingUpPanel/src/**/*.java']),
  deps = [
    ':sup-res',
    ':android-support-v4',
    ':nineoldandroids',
  ],
  visibility = [ 'PUBLIC' ],
)

# SystemBarTint Source Code
android_library(
  name = 'sbt-src',
  srcs = glob(['./libs/SystemBarTint/src/**/*.java']),
  visibility = [ 'PUBLIC' ],
)

# Debug Keystore
keystore(
  name = 'debug_keystore',
  store = './keystore/debug.keystore',
  properties = './keystore/debug.keystore.properties',
)

# Release Keystore (Private)
keystore(
  name = 'release_keystore',
  store = './keystore/publish.keystore',
  properties = './keystore/publish.keystore.properties',
)

# Config
project_config(
  src_target = ':debug',
)
