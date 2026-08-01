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

# Don't rename/shrink the following class fields, as they are accessed by internal reflection tool *AtomicReferenceFieldUpdater*
# They should always be described by "volatile" modifier, if not, please report an issue
-keepclassmembers class com.alibaba.fastjson2.JSONFactory$CacheItem {
  volatile <fields>;
}
-keepclassmembers class com.alibaba.fastjson2.util.TypeUtils$Cache {
  volatile <fields>;
}
-keepclassmembers class com.alibaba.fastjson2.writer.FieldWriter {
  volatile com.alibaba.fastjson2.writer.ObjectWriter initObjectWriter;
}
-keepclassmembers class com.alibaba.fastjson2.writer.FieldWriterObject {
  volatile java.lang.Class initValueClass;
}
