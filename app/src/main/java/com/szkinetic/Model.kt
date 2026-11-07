package com.szkinetic
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.io.File
import kotlin.math.abs
class Ly(var name:String,val col:Color)
class Cfg(c:Context){
private val sp=c.getSharedPreferences("kinetic",0)
var scale by mutableFloatStateOf(sp.getFloat("scale",1f))
var hide by mutableStateOf(sp.getBoolean("hide",true))
var snap by mutableStateOf(sp.getBoolean("snap",true))
var auto by mutableStateOf(sp.getBoolean("auto",true))
var safe by mutableStateOf(sp.getBoolean("safe",false))
var prev by mutableIntStateOf(sp.getInt("prev",2))
fun save(){sp.edit().putFloat("scale",scale).putBoolean("hide",hide).putBoolean("snap",snap).putBoolean("auto",auto).putBoolean("safe",safe).putInt("prev",prev).apply()}
}
class Ed(val id:String,var name:String,val cw:Int,val ch:Int,fps:Float,dur:Float){
val eng=Engine(cw,ch,fps,dur)
val ly=mutableStateListOf<Ly>()
var time by mutableFloatStateOf(0f)
var playing by mutableStateOf(false)
var sel by mutableIntStateOf(0)
var open by mutableStateOf(setOf<Ly>())
var selKey by mutableStateOf<Triple<Int,Int,Float>?>(null)
var rev by mutableIntStateOf(0)
private val us=mutableListOf<String>()
private val rs=mutableListOf<String>()
val canUndo get()=rev>=0&&us.isNotEmpty()
val canRedo get()=rev>=0&&rs.isNotEmpty()
private var bm:Bitmap?=null
private var bd=0
fun bitmap(div:Int):Bitmap{val c=bm;if(c==null||bd!=div){val w=(640/div).coerceAtLeast(16);val h=(w*ch/cw).coerceAtLeast(2);val n=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);bm=n;bd=div;return n};return c}
fun fr(t:Float)=Math.round(t*eng.fps)/eng.fps
fun seek(t:Float,snap:Boolean=false){var x=t.coerceIn(0f,eng.dur-0.01f);if(snap&&sel in ly.indices){for(p in 0..4)for(k in eng.kfs(sel,p))if(abs(k.t-x)<0.05f)x=k.t};time=x}
fun tc():String{val fp=eng.fps.toInt();val f=(time*eng.fps).toInt();val s=f/fp;return "0:%02d:%02d:%02d".format(s/60,s%60,f%fp)}
fun snap(){us.add(Store.dump(this));if(us.size>50)us.removeAt(0);rs.clear();rev++}
fun undo(){if(us.isEmpty())return;rs.add(Store.dump(this));Store.fill(this,us.removeAt(us.size-1));rev++}
fun redo(){if(rs.isEmpty())return;us.add(Store.dump(this));Store.fill(this,rs.removeAt(rs.size-1));rev++}
private fun pre(k:Int)=when(k){0->"Solid";1->"Shape Layer";2->"Null";else->"Adjustment Layer"}
fun addLayer(k:Int){
val n=ly.count{it.name.startsWith(pre(k))}+1
val c=C.lbl[ly.size%5]
val w=when(k){0->cw.toFloat();1->ch/3f;2->100f;else->0f}
val h=when(k){0->ch.toFloat();1->ch/3f;2->100f;else->0f}
val a=c.toArgb();val i=eng.add(k,(a shr 16) and 255,(a shr 8) and 255,a and 255,w,h)
eng.meta(i,0f,eng.dur,-1,0,if(k==3)10f else 0f,if(k==3)15f else 0f)
ly.add(Ly(pre(k)+" "+n,c));sel=i;open=open+ly[i];rev++}
fun removeLayer(i:Int){if(i !in ly.indices)return;eng.remove(i);open=open-ly[i];ly.removeAt(i);sel=sel.coerceIn(0,maxOf(0,ly.size-1));selKey=null;rev++}
fun value(l:Int,p:Int)=eng.get(l,p,time)
fun at(l:Int,p:Int):KF?=eng.kfs(l,p).firstOrNull{abs(it.t-time)<0.5f/eng.fps}
fun setValue(l:Int,p:Int,v:Float){val ks=eng.kfs(l,p);if(ks.isEmpty())eng.base(l,p,v) else eng.key(l,p,at(l,p)?.t?:fr(time),v,at(l,p)?.i?:0);rev++}
fun toggleKey(l:Int,p:Int){snap();val e=at(l,p);if(e!=null){if(eng.kfs(l,p).size==1)eng.base(l,p,e.v);eng.delKey(l,p,e.t)}else eng.key(l,p,fr(time),value(l,p),0);selKey=null;rev++}
fun toggleWatch(l:Int,p:Int){snap();val ks=eng.kfs(l,p);if(ks.isNotEmpty()){eng.base(l,p,value(l,p));ks.forEach{eng.delKey(l,p,it.t)}}else eng.key(l,p,fr(time),value(l,p),0);selKey=null;rev++}
fun shift(l:Int,dt:Float){val f=eng.info(l);val len=minOf(f[7],eng.dur)-f[6];val ni=(f[6]+dt).coerceIn(0f,maxOf(0f,eng.dur-len));eng.meta(l,ni,ni+len,f[8].toInt(),f[9].toInt(),f[10],f[11]);rev++}
fun edit(l:Int,fn:(FloatArray)->Unit){val f=eng.info(l);fn(f);eng.meta(l,f[6],f[7],f[8].toInt(),f[9].toInt(),f[10],f[11]);rev++}
fun toggleVis(l:Int){snap();eng.vis(l,eng.info(l)[12]==0f);rev++}
private fun desc(c:Int,l:Int):Boolean{var p=c;var g=0;while(p>=0&&g<64){if(p==l)return true;p=eng.info(p)[8].toInt();g++};return false}
fun cycleParent(l:Int){snap();val cur=eng.info(l)[8].toInt();val c=listOf(-1)+ly.indices.filter{it!=l&&!desc(it,l)};val n=c[(c.indexOf(cur)+1)%c.size];edit(l){it[8]=n.toFloat()}}
fun setInterp(i:Int){snap();val s=selKey;if(s!=null)eng.interp(s.first,s.second,s.third,i) else if(sel in ly.indices)for(p in 0..4)for(k in eng.kfs(sel,p))if(abs(k.t-time)<0.5f/eng.fps)eng.interp(sel,p,k.t,i);rev++}
}
object Store{
fun dir(c:Context)=File(c.filesDir,"projects").apply{mkdirs()}
fun save(c:Context,e:Ed){File(dir(c),e.id+".kin").writeText(dump(e))}
fun list(c:Context):List<File> = dir(c).listFiles{f->f.extension=="kin"}?.sortedByDescending{it.lastModified()}?:emptyList()
fun header(f:File):List<String> =f.useLines{it.first()}.split('\t')
fun open(f:File):Ed{val t=f.readText();val h=t.lineSequence().first().split('\t');val e=Ed(f.nameWithoutExtension,h[5],h[1].toInt(),h[2].toInt(),h[3].toFloat(),h[4].toFloat());fill(e,t);return e}
fun dump(e:Ed):String{val sb=StringBuilder();sb.append("C\t${e.cw}\t${e.ch}\t${e.eng.fps}\t${e.eng.dur}\t${e.name.replace('\t',' ')}\n")
for(i in e.ly.indices){val f=e.eng.info(i);sb.append("L\t${e.ly[i].name.replace('\t',' ')}\t${e.ly[i].col.toArgb()}\t"+f.joinToString("\t")+"\n")
for(p in 0..6){sb.append("B\t$p\t${e.eng.getBase(i,p)}\n");for(k in e.eng.kfs(i,p))sb.append("K\t$p\t${k.t}\t${k.v}\t${k.i}\t${k.ox}\t${k.ix}\n")}}
return sb.toString()}
fun fill(e:Ed,text:String){e.eng.clear();e.ly.clear();e.open=emptySet();e.selKey=null;var idx=-1
for(line in text.lineSequence()){val p=line.split('\t');when(p[0]){
"L"->{val f=FloatArray(13){p[3+it].toFloat()};idx=e.eng.add(f[0].toInt(),f[1].toInt(),f[2].toInt(),f[3].toInt(),f[4],f[5]);e.eng.meta(idx,f[6],f[7],f[8].toInt(),f[9].toInt(),f[10],f[11]);e.eng.vis(idx,f[12]!=0f);e.ly.add(Ly(p[1],androidx.compose.ui.graphics.Color(p[2].toInt())))}
"B"->e.eng.base(idx,p[1].toInt(),p[2].toFloat())
"K"->{e.eng.key(idx,p[1].toInt(),p[2].toFloat(),p[3].toFloat(),p[4].toInt());e.eng.infl(idx,p[1].toInt(),p[2].toFloat(),p[5].toFloat(),p[6].toFloat())}}}
e.sel=e.sel.coerceIn(0,maxOf(0,e.ly.size-1))}
fun seed(c:Context):Int{if(list(c).isNotEmpty())return 0
val e=Ed("demo","Neon Logo Reveal",1280,720,30f,10f)
fun mk(name:String,col:androidx.compose.ui.graphics.Color,type:Int,r:Int,g:Int,b:Int,w:Float,h:Float,a:Float,z:Float,a1:Float=0f,a2:Float=0f):Int{val i=e.eng.add(type,r,g,b,w,h);e.eng.meta(i,a,z,-1,0,a1,a2);e.ly.add(Ly(name,col));return i}
mk("Backdrop",C.lbl[2],0,20,20,28,1280f,720f,0f,10f)
val el=mk("Shape Layer 1",C.lbl[1],1,208,96,94,240f,240f,0f,8f)
val ti=mk("Title",C.lbl[0],0,155,140,255,520f,160f,0.3f,6.2f)
mk("Adjustment Layer 1",C.lbl[2],3,0,0,0,0f,0f,1f,9f,10f,15f)
e.eng.key(el,Pr.X,0f,160f,2);e.eng.key(el,Pr.X,4f,1120f,2);e.eng.key(el,Pr.Y,0f,500f,2);e.eng.key(el,Pr.Y,4f,240f,2)
e.eng.key(ti,Pr.O,0.5f,0f,0);e.eng.key(ti,Pr.O,1.5f,100f,0);e.eng.key(ti,Pr.S,0.5f,80f,2);e.eng.key(ti,Pr.S,3.5f,100f,2);e.eng.key(ti,Pr.R,0.5f,-8f,2);e.eng.key(ti,Pr.R,2.4f,0f,2)
e.time=2.4f;e.sel=ti;e.open=setOf(e.ly[ti])
save(c,e);e.eng.close();return 1}
}
