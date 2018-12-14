#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <jni.h>
#include <assert.h>
#include <android/log.h>
#include "traceroute.h"

#define JNI_REG_CLASS "network/path/mobilenode/data/runner/mrt/MTR"
#define JNI_RESULT_CLASS "network/path/mobilenode/data/runner/mrt/MtrResult"

JNIEXPORT jobjectArray JNICALL native_trace(JNIEnv *env, jclass clazz, jstring server) {
    const char *c_str;
    c_str = (*env)->GetStringUTFChars(env, server, NULL);
    if (c_str == NULL) {
        return NULL;
    }

    // after using it, remember to release the memory
    const char *argv[] = {"traceroute", "-4", c_str};
    int count = 0;
    probe_result *results = NULL;
    int res = traceroute(3, (char **) &argv, &count, &results);
    (*env)->ReleaseStringUTFChars(env, server, c_str);

    if (res) {
        return NULL;
    }

    jclass cls = (*env)->FindClass(env, JNI_RESULT_CLASS);
    jmethodID constructorId = (*env)->GetMethodID(env, cls, "<init>",
                                                  "(ILjava/lang/String;Ljava/lang/String;ZILjava/lang/String;DLjava/lang/String;)V");

    jobjectArray array = (*env)->NewObjectArray(env, count, cls, NULL);
    for (int i = 0; i < count; ++i) {
        probe_result *probe_res = &results[i];
        if (probe_res->ttl == 0) continue;

        if (probe_res->err[0]) {
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "%2u %s (%s) %s",
                                probe_res->ttl,
                                probe_res->host,
                                probe_res->ip,
                                probe_res->err);
        } else if (probe_res->timeout) {
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "%2u %s (%s) *",
                                probe_res->ttl,
                                probe_res->host,
                                probe_res->ip);
        } else {
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "%2u %s (%s) %.3f ms",
                                probe_res->ttl,
                                probe_res->host,
                                probe_res->ip,
                                probe_res->delay * 1000.0);
        }

        jstring host = (*env)->NewStringUTF(env, probe_res->host);
        jstring ip = (*env)->NewStringUTF(env, probe_res->ip);
        jstring ext = probe_res->err[0] ? (*env)->NewStringUTF(env, probe_res->ext) : NULL;
        jstring err = probe_res->ext[0] ? (*env)->NewStringUTF(env, probe_res->err) : NULL;
        jobject o = (*env)->NewObject(env, cls, constructorId,
                                      (jint) probe_res->ttl,
                                      host,
                                      ip,
                                      (jboolean) probe_res->timeout,
                                      (jint) probe_res->recv_ttl,
                                      ext,
                                      (jdouble) probe_res->delay,
                                      err);
        (*env)->SetObjectArrayElement(env, array, i, o);
    }
    if (results != NULL) {
        free(results);
    }
    return array;
}


static JNINativeMethod gMethods[] = {
        {"trace", "(Ljava/lang/String;)[L" JNI_RESULT_CLASS ";", (void *) native_trace},
};

static int registerNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;
    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    jint res = (*env)->RegisterNatives(env, clazz, gMethods, numMethods);
    if (res < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, JNI_REG_CLASS, gMethods, sizeof(gMethods) / sizeof(gMethods[0]))) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    assert(env != NULL);
    if (!registerNatives(env)) {
        return -1;
    }
    result = JNI_VERSION_1_4;
    return result;
}
