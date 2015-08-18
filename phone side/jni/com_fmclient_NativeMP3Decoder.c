#define TAG "native_libmad"


#include "FileSystem.h"
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include "NativeMP3Decoder.h"

JNIEXPORT jint JNICALL Java_com_fmclient_NativeMP3Decoder_initAudioPlayer(JNIEnv *env, jobject obj, jstring file,jint startAddr)
{
    
    const char* fileString = (*env)->GetStringUTFChars(env,file, NULL);
    
    return  NativeMP3Decoder_init(fileString,startAddr);

}

JNIEXPORT jint JNICALL Java_com_fmclient_NativeMP3Decoder_getAudioBuf(JNIEnv *env, jobject obj ,jshortArray audioBuf,jint len)
{
    int bufsize = 0;
    int ret = 0;
    if (audioBuf != NULL) {
        bufsize = (*env)->GetArrayLength(env, audioBuf);
        jshort *_buf = (*env)->GetShortArrayElements(env, audioBuf, 0);
        memset(_buf, 0, bufsize*2);
        ret = NativeMP3Decoder_readSamples(_buf, len);
        (*env)->ReleaseShortArrayElements(env, audioBuf, _buf, 0);
    }
    else{

            __android_log_print(ANDROID_LOG_DEBUG, TAG, "getAudio failed");
        }
    return ret;
}
 
JNIEXPORT void JNICALL Java_com_fmclient_NativeMP3Decoder_closeAduioFile(JNIEnv *env, jobject obj)
{
	NativeMP3Decoder_closeAduioFile();
}


JNIEXPORT jint JNICALL Java_com_fmclient_NativeMP3Decoder_getAudioSamplerate(JNIEnv *env, jobject obj)

{
    return NativeMP3Decoder_getAduioSamplerate();

}

