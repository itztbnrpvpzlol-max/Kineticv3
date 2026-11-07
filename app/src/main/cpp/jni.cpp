#include <jni.h>
#include <android/bitmap.h>
#include "engine.h"
using namespace kin;
static Layer& G(jlong h,jint l){static Layer d;Comp*c=(Comp*)h;if(l>=0&&l<(int)c->L.size())return c->L[l];d=Layer();return d;}
#define J(n) Java_com_szkinetic_Engine_##n
extern "C"{
JNIEXPORT jlong JNICALL J(nCreate)(JNIEnv*,jobject,jint w,jint h,jfloat fps,jfloat dur){Comp*c=new Comp();c->w=w;c->h=h;c->fps=fps;c->dur=dur;return (jlong)c;}
JNIEXPORT void JNICALL J(nDestroy)(JNIEnv*,jobject,jlong h){delete (Comp*)h;}
JNIEXPORT jint JNICALL J(nAddLayer)(JNIEnv*,jobject,jlong h,jint type,jint r,jint g,jint b,jfloat w,jfloat hh){Comp*c=(Comp*)h;Layer l;l.type=type;l.r=(uint8_t)r;l.g=(uint8_t)g;l.b=(uint8_t)b;l.w=w;l.h=hh;l.p[0].base=c->w*.5f;l.p[1].base=c->h*.5f;l.p[2].base=100;l.p[3].base=0;l.p[4].base=100;l.p[5].base=w*.5f;l.p[6].base=hh*.5f;c->L.push_back(l);return (jint)c->L.size()-1;}
JNIEXPORT void JNICALL J(nSetBase)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat v){G(h,l).p[p].base=v;}
JNIEXPORT void JNICALL J(nSetKey)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat t,jfloat v,jint i){G(h,l).p[p].set(t,v,i);}
JNIEXPORT void JNICALL J(nSetMeta)(JNIEnv*,jobject,jlong h,jint l,jfloat in,jfloat out,jint parent,jint blend,jfloat a1,jfloat a2){Layer&y=G(h,l);y.in=in;y.out=out;y.parent=parent;y.blend=blend;y.a1=a1;y.a2=a2;}
JNIEXPORT jfloat JNICALL J(nGet)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat t){return G(h,l).p[p].at(t);}
JNIEXPORT jfloatArray JNICALL J(nKeyVals)(JNIEnv*e,jobject,jlong h,jint l,jint p){const Track&k=G(h,l).p[p];jfloatArray a=e->NewFloatArray((jsize)k.k.size()*5);for(size_t i=0;i<k.k.size();i++){jfloat v[5]={k.k[i].t,k.k[i].v,(jfloat)k.k[i].interp,k.k[i].ox,k.k[i].ix};e->SetFloatArrayRegion(a,(jsize)i*5,5,v);}return a;}
JNIEXPORT void JNICALL J(nDelKey)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat t){G(h,l).p[p].del(t);}
JNIEXPORT void JNICALL J(nMoveKey)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat t0,jfloat t1,jfloat v){G(h,l).p[p].move(t0,t1,v);}
JNIEXPORT void JNICALL J(nSetInterp)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat t,jint i){G(h,l).p[p].interp(t,i);}
JNIEXPORT void JNICALL J(nSetInfl)(JNIEnv*,jobject,jlong h,jint l,jint p,jfloat t,jfloat ox,jfloat ix){G(h,l).p[p].infl(t,ox,ix);}
JNIEXPORT jfloat JNICALL J(nGetBase)(JNIEnv*,jobject,jlong h,jint l,jint p){return G(h,l).p[p].base;}
JNIEXPORT jint JNICALL J(nCount)(JNIEnv*,jobject,jlong h){return (jint)((Comp*)h)->L.size();}
JNIEXPORT void JNICALL J(nRemove)(JNIEnv*,jobject,jlong h,jint l){removeLayer(*(Comp*)h,l);}
JNIEXPORT void JNICALL J(nClear)(JNIEnv*,jobject,jlong h){((Comp*)h)->L.clear();}
JNIEXPORT void JNICALL J(nSetVis)(JNIEnv*,jobject,jlong h,jint l,jboolean v){G(h,l).vis=v!=0;}
JNIEXPORT jfloatArray JNICALL J(nLayerInfo)(JNIEnv*e,jobject,jlong h,jint l){const Layer&y=G(h,l);jfloat v[13]={(jfloat)y.type,(jfloat)y.r,(jfloat)y.g,(jfloat)y.b,y.w,y.h,y.in,y.out,(jfloat)y.parent,(jfloat)y.blend,y.a1,y.a2,y.vis?1.f:0.f};jfloatArray a=e->NewFloatArray(13);e->SetFloatArrayRegion(a,0,13,v);return a;}
JNIEXPORT void JNICALL J(nRender)(JNIEnv*e,jobject,jlong h,jfloat t,jobject bmp){AndroidBitmapInfo i;if(AndroidBitmap_getInfo(e,bmp,&i)<0)return;void*p;if(AndroidBitmap_lockPixels(e,bmp,&p)<0)return;render(*(Comp*)h,t,(uint32_t*)p,(int)(i.stride/4),(int)i.width,(int)i.height);AndroidBitmap_unlockPixels(e,bmp);}
}
