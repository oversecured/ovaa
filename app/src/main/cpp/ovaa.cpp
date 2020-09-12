#include <jni.h>
#include <malloc.h>

extern "C"
JNIEXPORT void JNICALL
Java_oversecured_ovaa_objects_MemoryCorruptionSerializable_freePtr(JNIEnv *env, jobject thiz,
                                                                   jlong ptr) {
    free((void*) ptr);
}