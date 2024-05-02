-keep, allowoptimization, allowobfuscation
  @com.alibaba.fastjson2.annotation.JSONType class *
# Keep the fields and methods with @JSONField
-keepclassmembers, allowobfuscation class * {
  @com.alibaba.fastjson2.annotation.JSONField <fields>;
  @com.alibaba.fastjson2.annotation.JSONField <methods>;
}

# Ignore warning
-dontwarn java.beans.Transient
-dontwarn com.alibaba.fastjson.*

# Keep the fields and methods annotated with
# @JSONField for classes which are referenced
-if class * {
  @com.alibaba.fastjson2.annotation.JSONField <fields>;
}
-keep, allowobfuscation, allowoptimization class <1>
-if class * {
  @com.alibaba.fastjson2.annotation.JSONField <methods>;
}
-keep, allowobfuscation, allowoptimization class <1>

# Keep any (anonymous) classes extending TypeReference
-keep, allowobfuscation class com.alibaba.fastjson2.TypeReference
-keep, allowobfuscation class * extends com.alibaba.fastjson2.TypeReference
