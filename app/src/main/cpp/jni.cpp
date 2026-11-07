#include <jni.h>
#include <android/bitmap.h>
#include "engine.h"
using namespace kin;
#define J(n) Java_com_szkinetic_Engine_##n
extern "C"{
JNIEXPORT jlong JNICALL J(nCreate)(JNIEnv*,jobject,jint w,jint h,jfloat fps,jfloat dur){Comp*c=new Comp();c->w=w;c->h=h;c->fps=fps;c->dur=dur;return (jlong)c;}
JNIEXPORT void JNICALL J(nDestroy)(JNIEnv*,jobject,jlong h){delete (Comp*)h;}
JNIEXPORT jint JNICALL J(nAddLayer)(JNIEnv*,jobject,jlong h,jint type,jint r,jint g,jint b,jfloat w,jfloat hh){Comp*c=(Comp*)h;Layer l;l.type=type;l.r=(uint8_t)r;l.g=(uint8_t)g;l.b=(uint8_t)b;l.w=w;l.h=hh;l.p[0].base=c->w*.5f;l.p[1].base=c->h*.5f;l.p[2].base=100;l.p[3].base=0;l.p[4].base=100;l.p[5].base=w*.5f;l.p[6].base=hh*.5f;c->L.push_back(l);return (jint)c->L.size()-1;}
JNIEXPORT void JNICALL J(nSetBase)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat v){((Comp*)h)->L[l].p[p].base=v;}
JNIEXPORT void JNICALL J(nSetKey)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat t,jfloat v,jint i){((Comp*)h)->L[l].p[p].set(t,v,i);}
JNIEXPORT void JNICALL J(nSetMeta)(JNIEnv*,jobject,jlong h,jint l,jfloat in,jfloat out,jint parent,jint blend,jfloat a1,jfloat a2){Layer&y=((Comp*)h)->L[l];y.in=in;y.out=out;y.parent=parent;y.blend=blend;y.a1=a1;y.a2=a2;}
JNIEXPORT jfloat JNICALL J(nGet)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat t){return ((Comp*)h)->L[l].p[p].at(t);}
JNIEXPORT jfloatArray JNICALL J(nKeys)(JNIEnv*e,jobject,jlong h,jint l,jint p){const Track&k=((Comp*)h)->L[l].p[p];jfloatArray a=e->NewFloatArray((jsize)k.k.size());for(size_t i=0;i<k.k.size();i++){jfloat v=k.k[i].t;e->SetFloatArrayRegion(a,(jsize)i,1,&v);}return a;}
JNIEXPORT void JNICALL J(nRender)(JNIEnv*e,jobject,jlong h,jfloat t,jobject bmp){AndroidBitmapInfo i;if(AndroidBitmap_getInfo(e,bmp,&i)<0)return;void*p;if(AndroidBitmap_lockPixels(e,bmp,&p)<0)return;render(*(Comp*)h,t,(uint32_t*)p,(int)(i.stride/4),(int)i.width,(int)i.height);AndroidBitmap_unlockPixels(e,bmp);}
}
