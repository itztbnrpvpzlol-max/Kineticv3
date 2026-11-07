package com.szkinetic
import android.graphics.Bitmap
object Pr{const val X=0;const val Y=1;const val S=2;const val R=3;const val O=4;const val AX=5;const val AY=6}
class Engine(val w:Int,val h:Int,val fps:Float,val dur:Float){
companion object{init{System.loadLibrary("kinetic")}}
private var hd=nCreate(w,h,fps,dur)
fun close(){if(hd!=0L){nDestroy(hd);hd=0L}}
fun add(type:Int,r:Int,g:Int,b:Int,lw:Float,lh:Float)=nAddLayer(hd,type,r,g,b,lw,lh)
fun base(l:Int,p:Int,v:Float)=nSetBase(hd,l,p,v)
fun key(l:Int,p:Int,t:Float,v:Float,i:Int=0)=nSetKey(hd,l,p,t,v,i)
fun meta(l:Int,inT:Float,outT:Float,parent:Int,blend:Int,a1:Float,a2:Float)=nSetMeta(hd,l,inT,outT,parent,blend,a1,a2)
fun get(l:Int,p:Int,t:Float)=nGet(hd,l,p,t)
fun keys(l:Int,p:Int):FloatArray=nKeys(hd,l,p)
fun render(t:Float,b:Bitmap)=nRender(hd,t,b)
private external fun nCreate(w:Int,h:Int,fps:Float,dur:Float):Long
private external fun nDestroy(h:Long)
private external fun nAddLayer(h:Long,type:Int,r:Int,g:Int,b:Int,w:Float,hh:Float):Int
private external fun nSetBase(h:Long,l:Int,p:Int,v:Float)
private external fun nSetKey(h:Long,l:Int,p:Int,t:Float,v:Float,i:Int)
private external fun nSetMeta(h:Long,l:Int,inT:Float,outT:Float,parent:Int,blend:Int,a1:Float,a2:Float)
private external fun nGet(h:Long,l:Int,p:Int,t:Float):Float
private external fun nKeys(h:Long,l:Int,p:Int):FloatArray
private external fun nRender(h:Long,t:Float,b:Bitmap)
}
