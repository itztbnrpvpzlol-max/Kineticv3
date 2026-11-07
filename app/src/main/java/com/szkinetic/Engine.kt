package com.szkinetic
import android.graphics.Bitmap
object Pr{const val X=0;const val Y=1;const val S=2;const val R=3;const val O=4;const val AX=5;const val AY=6}
class KF(val t:Float,val v:Float,val i:Int,val ox:Float,val ix:Float)
class Engine(val w:Int,val h:Int,val fps:Float,val dur:Float){
companion object{init{System.loadLibrary("kinetic")}}
private var hd=nCreate(w,h,fps,dur)
fun close(){if(hd!=0L){nDestroy(hd);hd=0L}}
fun add(type:Int,r:Int,g:Int,b:Int,lw:Float,lh:Float)=if(hd==0L)0 else nAddLayer(hd,type,r,g,b,lw,lh)
fun base(l:Int,p:Int,v:Float){if(hd!=0L)nSetBase(hd,l,p,v)}
fun getBase(l:Int,p:Int)=if(hd==0L)0f else nGetBase(hd,l,p)
fun key(l:Int,p:Int,t:Float,v:Float,i:Int=0){if(hd!=0L)nSetKey(hd,l,p,t,v,i)}
fun delKey(l:Int,p:Int,t:Float){if(hd!=0L)nDelKey(hd,l,p,t)}
fun moveKey(l:Int,p:Int,t0:Float,t1:Float,v:Float){if(hd!=0L)nMoveKey(hd,l,p,t0,t1,v)}
fun interp(l:Int,p:Int,t:Float,i:Int){if(hd!=0L)nSetInterp(hd,l,p,t,i)}
fun infl(l:Int,p:Int,t:Float,ox:Float,ix:Float){if(hd!=0L)nSetInfl(hd,l,p,t,ox,ix)}
fun meta(l:Int,inT:Float,outT:Float,parent:Int,blend:Int,a1:Float,a2:Float){if(hd!=0L)nSetMeta(hd,l,inT,outT,parent,blend,a1,a2)}
fun vis(l:Int,v:Boolean){if(hd!=0L)nSetVis(hd,l,v)}
fun remove(l:Int){if(hd!=0L)nRemove(hd,l)}
fun clear(){if(hd!=0L)nClear(hd)}
fun count()=if(hd==0L)0 else nCount(hd)
fun get(l:Int,p:Int,t:Float)=if(hd==0L)0f else nGet(hd,l,p,t)
fun info(l:Int):FloatArray=if(hd==0L)FloatArray(13) else nLayerInfo(hd,l)
fun kfs(l:Int,p:Int):List<KF>{if(hd==0L)return emptyList();val a=nKeyVals(hd,l,p);return List(a.size/5){KF(a[it*5],a[it*5+1],a[it*5+2].toInt(),a[it*5+3],a[it*5+4])}}
fun render(t:Float,b:Bitmap){if(hd!=0L)nRender(hd,t,b)}
private external fun nCreate(w:Int,h:Int,fps:Float,dur:Float):Long
private external fun nDestroy(h:Long)
private external fun nAddLayer(h:Long,type:Int,r:Int,g:Int,b:Int,w:Float,hh:Float):Int
private external fun nSetBase(h:Long,l:Int,p:Int,v:Float)
private external fun nGetBase(h:Long,l:Int,p:Int):Float
private external fun nSetKey(h:Long,l:Int,p:Int,t:Float,v:Float,i:Int)
private external fun nDelKey(h:Long,l:Int,p:Int,t:Float)
private external fun nMoveKey(h:Long,l:Int,p:Int,t0:Float,t1:Float,v:Float)
private external fun nSetInterp(h:Long,l:Int,p:Int,t:Float,i:Int)
private external fun nSetInfl(h:Long,l:Int,p:Int,t:Float,ox:Float,ix:Float)
private external fun nSetMeta(h:Long,l:Int,inT:Float,outT:Float,parent:Int,blend:Int,a1:Float,a2:Float)
private external fun nSetVis(h:Long,l:Int,v:Boolean)
private external fun nRemove(h:Long,l:Int)
private external fun nClear(h:Long)
private external fun nCount(h:Long):Int
private external fun nGet(h:Long,l:Int,p:Int,t:Float):Float
private external fun nLayerInfo(h:Long,l:Int):FloatArray
private external fun nKeyVals(h:Long,l:Int,p:Int):FloatArray
private external fun nRender(h:Long,t:Float,b:Bitmap)
}
