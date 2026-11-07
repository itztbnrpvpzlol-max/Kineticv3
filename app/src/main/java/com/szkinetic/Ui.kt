package com.szkinetic
import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
object C{val bg=Color(0xFF121212);val panel=Color(0xFF1D1D1D);val hi=Color(0xFF262626);val row=Color(0xFF202020);val sel=Color(0xFF2B2745);val text=Color(0xFFD6D6D6);val dim=Color(0xFF8C8C8C);val faint=Color(0xFF5A5A5A);val accent=Color(0xFF2D8CEB);val brand=Color(0xFF9B8CFF);val viewer=Color(0xFF0A0A0A);val track=Color(0xFF181818)}
class Ly(val i:Int,val name:String,val col:Color,val inT:Float,val outT:Float)
class Ed{
val eng=Engine(640,360,30f,10f)
val bmp:Bitmap=Bitmap.createBitmap(640,360,Bitmap.Config.ARGB_8888)
val ly=mutableListOf<Ly>()
var time by mutableFloatStateOf(2.4f)
var playing by mutableStateOf(false)
var sel by mutableIntStateOf(0)
var open by mutableStateOf(setOf(0))
fun seek(t:Float){time=t.coerceIn(0f,eng.dur-0.01f)}
fun tc():String{val fp=eng.fps.toInt();val f=(time*eng.fps).toInt();val s=f/fp;return "0:%02d:%02d:%02d".format(s/60,s%60,f%fp)}
init{
val bg=eng.add(0,20,20,28,640f,360f);ly.add(Ly(bg,"Backdrop",C.faint,0f,10f))
val el=eng.add(1,208,96,94,120f,120f);ly.add(Ly(el,"Shape Layer 1",Color(0xFFD0605E),0f,8f))
eng.key(el,Pr.X,0f,80f,2);eng.key(el,Pr.X,4f,560f,2);eng.key(el,Pr.Y,0f,250f,2);eng.key(el,Pr.Y,4f,120f,2)
val ti=eng.add(0,155,140,255,260f,80f);ly.add(Ly(ti,"Title",Color(0xFF8E7CF0),0.3f,6.2f))
eng.key(ti,Pr.O,0.5f,0f,0);eng.key(ti,Pr.O,1.5f,100f,0);eng.key(ti,Pr.S,0.5f,80f,2);eng.key(ti,Pr.S,3.5f,100f,2);eng.key(ti,Pr.R,0.5f,-8f,2);eng.key(ti,Pr.R,2.4f,0f,2)
eng.meta(ti,0.3f,6.2f,-1,0,0f,0f)
val ad=eng.add(3,0,0,0,0f,0f);ly.add(Ly(ad,"Adjustment Layer 1",Color(0xFFA8A8A8),1f,9f))
eng.meta(ad,1f,9f,-1,0,10f,15f)
sel=ly.size-2
open=setOf(ty())
}
fun ty()=ly.size-2
}
@Composable fun T(s:String,c:Color=C.text,sz:Int=11,w:FontWeight=FontWeight.Normal,m:Modifier=Modifier)=Text(s,color=c,fontSize=sz.sp,fontWeight=w,modifier=m,maxLines=1)
@Composable fun Pn(title:String,m:Modifier,body:@Composable ColumnScope.()->Unit){Column(m.background(C.panel)){Box(Modifier.fillMaxWidth().height(22.dp).background(C.hi),Alignment.CenterStart){Box(Modifier.fillMaxHeight().background(C.panel).padding(horizontal=10.dp),Alignment.Center){T(title,C.text,10,FontWeight.Medium)}};body()}}
@Composable fun App(){
val ed=remember{Ed()}
DisposableEffect(Unit){onDispose{ed.eng.close()}}
LaunchedEffect(ed.playing){if(ed.playing){var last=0L;while(true){val n=withFrameNanos{it};if(last!=0L){val nt=ed.time+(n-last)/1e9f;ed.time=if(nt>=ed.eng.dur-0.01f)0f else nt};last=n}}}
MaterialTheme(colorScheme=darkColorScheme()){
Column(Modifier.fillMaxSize().background(C.bg).displayCutoutPadding()){
Row(Modifier.fillMaxWidth().height(32.dp).background(C.panel).padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
T("≡",C.text,16);T("Kinetic",C.brand,12,FontWeight.SemiBold);T("Untitled Project",C.dim,10)
Row(horizontalArrangement=Arrangement.spacedBy(2.dp)){listOf("Standard","Animation","Effects","Text").forEachIndexed{i,s->Box(Modifier.clip(RoundedCornerShape(5.dp)).background(if(i==1)C.hi else Color.Transparent).padding(horizontal=9.dp,vertical=4.dp)){T(s,if(i==1)C.text else C.dim,10,if(i==1)FontWeight.Medium else FontWeight.Normal)}}}
Spacer(Modifier.weight(1f))
Box(Modifier.clip(RoundedCornerShape(5.dp)).background(C.brand).padding(horizontal=10.dp,vertical=4.dp)){T("Export",C.bg,10,FontWeight.SemiBold)}}
Row(Modifier.weight(1f).fillMaxWidth()){
Column(Modifier.width(36.dp).fillMaxHeight().background(C.panel).padding(vertical=6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(3.dp)){listOf("V","H","Z","R","□","P","T","A").forEachIndexed{i,s->Box(Modifier.size(26.dp).clip(RoundedCornerShape(5.dp)).background(if(i==0)C.hi else Color.Transparent),Alignment.Center){T(s,if(i==0)C.brand else C.dim,11,FontWeight.Medium)}}}
Column(Modifier.weight(1f).fillMaxHeight()){
Row(Modifier.weight(1.1f).fillMaxWidth().padding(top=2.dp),horizontalArrangement=Arrangement.spacedBy(2.dp)){
Pn("Project",Modifier.width(130.dp).fillMaxHeight()){Column(Modifier.padding(6.dp),verticalArrangement=Arrangement.spacedBy(2.dp)){
Box(Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)).background(C.row).padding(horizontal=6.dp),Alignment.CenterStart){T("Search",C.faint,10)}
listOf("Comp 1" to "Composition","Footage_01.mp4" to "Video","Logo.png" to "Image","Ambient.wav" to "Audio","Solids" to "Folder").forEach{r->Row(Modifier.fillMaxWidth().height(20.dp).padding(horizontal=3.dp),verticalAlignment=Alignment.CenterVertically){T(r.first,C.text,10,m=Modifier.weight(1f));T(r.second,C.faint,8)}}}}
Pn("Composition: Comp 1",Modifier.weight(1f).fillMaxHeight()){
Canvas(Modifier.weight(1f).fillMaxWidth().background(C.viewer)){
val t=ed.time;ed.eng.render(t,ed.bmp)
val sc=minOf(size.width/640f,size.height/360f)*0.94f;val dw=640*sc;val dh=360*sc
drawIntoCanvas{c->c.nativeCanvas.drawBitmap(ed.bmp,null,RectF((size.width-dw)/2,(size.height-dh)/2,(size.width+dw)/2,(size.height+dh)/2),null)}}
Row(Modifier.fillMaxWidth().height(20.dp).background(C.hi).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){T("50%",C.dim,9);T("Full",C.dim,9);T("Active Camera",C.dim,9);Spacer(Modifier.weight(1f));T(ed.tc(),C.text,9,FontWeight.Medium)}}
Column(Modifier.width(130.dp).fillMaxHeight(),verticalArrangement=Arrangement.spacedBy(2.dp)){
Pn("Info",Modifier.weight(1f).fillMaxWidth()){Column(Modifier.padding(horizontal=10.dp,vertical=6.dp),verticalArrangement=Arrangement.spacedBy(3.dp)){
val l=ed.ly[ed.sel].i
listOf("X" to Pr.X,"Y" to Pr.Y,"Scale" to Pr.S,"Rot" to Pr.R,"Opacity" to Pr.O).forEach{p->Row{T(p.first+" :",C.dim,9,m=Modifier.width(44.dp));T("%.1f".format(ed.eng.get(l,p.second,ed.time)),C.text,9)}}}}
Pn("Effects & Presets",Modifier.weight(1f).fillMaxWidth()){Column(Modifier.padding(horizontal=8.dp,vertical=4.dp)){listOf("Animation Presets","Blur & Sharpen","Color Correction","Distort","Generate").forEach{s->Row(Modifier.height(18.dp),verticalAlignment=Alignment.CenterVertically){T("›",C.dim,10);Spacer(Modifier.width(5.dp));T(s,C.text,9)}}}}}}
Timeline(ed,Modifier.weight(1f).fillMaxWidth().padding(top=2.dp))
}}}}}
@Composable fun Timeline(ed:Ed,m:Modifier){
val vs=rememberScrollState();val hs=rememberScrollState()
val order=ed.ly.reversed()
val rows=remember(ed.open){val r=mutableListOf<Pair<Int,Int>>();order.forEach{l->r.add(l.i to -1);if(l.i in ed.open){listOf(Pr.X,Pr.Y,Pr.S,Pr.R,Pr.O).forEach{p->r.add(l.i to p)}}};r}
val lh=22.dp;val ph=18.dp
Column(m.background(C.panel)){
Row(Modifier.fillMaxWidth().height(24.dp).background(C.hi).padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
T("Comp 1",C.text,10,FontWeight.Medium);T(ed.tc(),C.accent,13,FontWeight.SemiBold);T("30.00 fps",C.faint,8);Spacer(Modifier.weight(1f))
Box(Modifier.clickable{ed.seek(0f)}.padding(4.dp)){T("⏮",C.dim,12)}
Box(Modifier.clickable{ed.playing=!ed.playing}.padding(4.dp)){T(if(ed.playing)"⏸" else "▶",C.text,13)}
Box(Modifier.clickable{ed.seek(ed.eng.dur)}.padding(4.dp)){T("⏭",C.dim,12)}}
Row(Modifier.weight(1f).fillMaxWidth()){
Column(Modifier.width(210.dp).fillMaxHeight()){
Row(Modifier.fillMaxWidth().height(18.dp).background(C.hi).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){T("#",C.dim,9,m=Modifier.width(34.dp));T("Layer Name",C.dim,9,m=Modifier.weight(1f));T("Parent",C.dim,9)}
Column(Modifier.verticalScroll(vs)){rows.forEachIndexed{ri,r->val l=ed.ly.first{it.i==r.first}
if(r.second<0){val s=ed.sel==ed.ly.indexOf(l)
Row(Modifier.fillMaxWidth().height(lh).background(if(s)C.sel else if(ri%2==1)C.row else C.panel).clickable{ed.sel=ed.ly.indexOf(l);ed.open=if(l.i in ed.open)ed.open-l.i else ed.open+l.i}.padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){
Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(l.col));Spacer(Modifier.width(5.dp));T((ed.ly.size-ed.ly.indexOf(l)).toString(),C.dim,9,m=Modifier.width(16.dp));T(if(l.i in ed.open)"▾" else "▸",C.dim,9,m=Modifier.width(12.dp));T(l.name,C.text,10,if(s)FontWeight.Medium else FontWeight.Normal,Modifier.weight(1f));T("None",C.dim,9)}}
else{val nm=when(r.second){Pr.X->"X Position";Pr.Y->"Y Position";Pr.S->"Scale";Pr.R->"Rotation";else->"Opacity"}
Row(Modifier.fillMaxWidth().height(ph).background(if(ri%2==1)C.row else C.panel).padding(start=34.dp,end=8.dp),verticalAlignment=Alignment.CenterVertically){T("◷",C.accent,9);Spacer(Modifier.width(5.dp));T(nm,C.dim,9,m=Modifier.weight(1f));T("%.1f".format(ed.eng.get(l.i,r.second,ed.time)),C.accent,9,FontWeight.Medium)}}}}}
Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)){
val wd=(ed.eng.dur*80f+40f).dp
Column(Modifier.width(wd).fillMaxHeight()){
Canvas(Modifier.fillMaxWidth().height(18.dp).background(C.hi).pointerInput(Unit){val pp=80.dp.toPx();detectDragGestures(onDragStart={o->ed.seek(o.x/pp)}){c,_->ed.seek(c.position.x/pp)}}){
val pp=80.dp.toPx()
for(i in 0..(ed.eng.dur*2).toInt()){val x=i*pp/2;val hh=if(i%2==0)8.dp.toPx() else 4.dp.toPx();drawLine(C.faint,Offset(x,size.height-hh),Offset(x,size.height),1f)}}
Canvas(Modifier.fillMaxWidth().weight(1f).background(C.track)){
val pp=80.dp.toPx();val lhp=lh.toPx();val php=ph.toPx();var y=-vs.value.toFloat()
rows.forEachIndexed{ri,r->val l=ed.ly.first{it.i==r.first};val h=if(r.second<0)lhp else php
val isSel=ed.ly[ed.sel].i==l.i
drawRect(if(r.second<0&&isSel)C.sel else if(ri%2==1)C.row else C.panel,Offset(0f,y),Size(size.width,h),alpha=0.4f)
if(r.second<0){drawRoundRect(l.col,Offset(l.inT*pp,y+3.dp.toPx()),Size((l.outT-l.inT)*pp,h-6.dp.toPx()),androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),alpha=if(isSel)0.9f else 0.55f)}
else{ed.eng.keys(l.i,r.second).forEach{k->val cx=k*pp;val cy=y+h/2;val d=4.dp.toPx();val pa=Path();pa.moveTo(cx,cy-d);pa.lineTo(cx+d,cy);pa.lineTo(cx,cy+d);pa.lineTo(cx-d,cy);pa.close();drawPath(pa,C.accent)}}
y+=h}
val px=ed.time*pp;drawLine(C.accent,Offset(px,0f),Offset(px,size.height),1.5f)}
}
}
}}}
