
#include <jni.h>
#include <vector>
#include <thread>
#include <map>

#include "sink.h"

#include <android/log.h>
#define LOG_TAG  "******C_TAG"
#define PRINT(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT void JNICALL
Java_cc_ralee_filterplayer_BaseActivity_sinkstart(JNIEnv *env, jclass type, jstring id, jstring ip, jint port);

JNIEXPORT void JNICALL
Java_cc_ralee_filterplayer_BaseActivity_sinkstop(JNIEnv *env, jclass type, jstring id);

#ifdef __cplusplus
}
#endif

int isExit = 0;
std::thread *g_pthreadSink = NULL;
int ThreadSink(std::string stVncArgs, int port);
int g_count = 0;
std::map<std::string, std::thread *> g_mapthreadSinks;

JNIEXPORT void JNICALL
Java_cc_ralee_filterplayer_BaseActivity_sinkstart(JNIEnv *env, jclass type, jstring id, jstring ip, jint port)
{

    PRINT("sinkstart start!!!");

#if 1
    const char* pchId = env->GetStringUTFChars(id,NULL);
    if(pchId == NULL) {
        PRINT("sinkstart ID is NULL !!!");
        return; /* OutOfMemoryError already thrown */
    }
    const char* pchIp = env->GetStringUTFChars(ip,NULL);
    if(pchIp == NULL) {
        PRINT("sinkstart IP is NULL !!!");
        return; /* OutOfMemoryError already thrown */
    }
    std::string stId = pchId;
    std::string stIp = pchIp;
    auto it = g_mapthreadSinks.find(stId);
    if(it != g_mapthreadSinks.end())
    {
        PRINT("sinkstart already start !!!");
        return;
    }
    std::thread *pthreadSink = new std::thread(ThreadSink,std::string(pchIp),port);
    g_mapthreadSinks[stId] = pthreadSink;
#endif

    return;
}

JNIEXPORT void JNICALL
Java_cc_ralee_filterplayer_BaseActivity_sinkstop(JNIEnv *env, jclass type, jstring id) {

    PRINT("sinkstart stop!!!");
#if 1
    const char* pchId = env->GetStringUTFChars(id,NULL);
    if(pchId == NULL) {
        PRINT("sinkstart ID is NULL !!!");
        return;
    }
    std::string stId = pchId;
    auto it = g_mapthreadSinks.find(stId);
    if(it == g_mapthreadSinks.end())
    {
        PRINT("sinkstart already stop !!!");
        return;
    }
    std::thread *pthreadSink = it->second;
    g_mapthreadSinks.erase(it);
    pthreadSink->join();
    delete pthreadSink;
    PRINT("sinkstart stop finish!!!");
#endif
    return;
}


int ThreadSink(std::string stVncArgs, int port)
{
    PRINT("ThreadSink run!!!");
    int ret = 0;
    sink((char*)stVncArgs.c_str(),port);
    return ret;
}