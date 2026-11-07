package com.szkinetic
import android.graphics.RectF
import android.os.Environment
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.min
@Composable fun Prow(t:String,sub:String,ctrl:@Composable ()->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){T(t,C.text,12,FontWeight.Medium);T(sub,C.faint,9)};ctrl()}}
@Composable fun Prefs(cfg:Cfg,close:()->Unit){
Column(Modifier.fillMaxSize()){
Row(Modifier.fillMaxWidth().height(32.dp).background(C.panel).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){T("‹",C.text,18,m=Modifier.clickable{cfg.save();close()}.padding(horizontal=6.dp));T("Preferences",C.text,13,FontWeight.SemiBold)}
Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
T("Interface",C.text,18,FontWeight.SemiBold)
Prow("Interface scale","Size of panels, rows and keyframes (${"%.2f".format(cfg.scale)}x)"){Sl((cfg.scale-0.8f)/0.6f,Modifier.width(200.dp)){cfg.scale=0.8f+0.6f*it;cfg.save()}}
Prow("Hide system bars","Fullscreen editing, swipe to reveal"){Tg(cfg.hide){cfg.hide=it;cfg.save()}}
Prow("Snap to keyframes","Playhead snaps to nearby keyframes of the selected layer"){Tg(cfg.snap){cfg.snap=it;cfg.save()}}
Prow("Auto-save","Save the project every 30 seconds"){Tg(cfg.auto){cfg.auto=it;cfg.save()}}
Prow("Show safe margins","Overlay title and action safe areas in the viewer"){Tg(cfg.safe){cfg.safe=it;cfg.save()}}
Prow("Preview resolution","Lower resolution renders faster"){Seg(listOf("Full","Half","Quarter"),listOf(1,2,4).indexOf(cfg.prev).coerceAtLeast(0),Modifier.width(200.dp)){cfg.prev=listOf(1,2,4)[it];cfg.save()}}}}}
@Composable fun ExportScr(ed:Ed,close:()->Unit){
val ctx=LocalContext.current;val sc=rememberCoroutineScope()
var fmt by remember{mutableIntStateOf(0)};var res by remember{mutableIntStateOf(0)};var q by remember{mutableIntStateOf(1)}
var prog by remember{mutableFloatStateOf(0f)};var run by remember{mutableStateOf(false)};var msg by remember{mutableStateOf("")}
var job by remember{mutableStateOf<Job?>(null)}
Column(Modifier.fillMaxSize()){
Row(Modifier.fillMaxWidth().height(32.dp).background(C.panel).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){T("‹",C.text,18,m=Modifier.clickable{if(!run)close()}.padding(horizontal=6.dp));T("Export",C.text,13,FontWeight.SemiBold)}
Row(Modifier.weight(1f).fillMaxWidth().padding(top=2.dp),horizontalArrangement=Arrangement.spacedBy(2.dp)){
Column(Modifier.weight(1f).fillMaxHeight().background(C.viewer),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
Canvas(Modifier.fillMaxWidth().weight(1f).padding(16.dp)){val b=ed.bitmap(2);ed.eng.render(ed.time,b);val s=min(size.width/b.width,size.height/b.height);val dw=b.width*s;val dh=b.height*s;val l0=(size.width-dw)/2;val t0=(size.height-dh)/2
drawIntoCanvas{c->c.nativeCanvas.drawBitmap(b,null,RectF(l0,t0,l0+dw,t0+dh),null)}}
T("${ed.name} · ${ed.cw}×${ed.ch} · ${ed.eng.fps.toInt()} fps · ${ed.eng.dur.toInt()} s",C.dim,9,m=Modifier.padding(bottom=8.dp))}
Pn("Render Settings",Modifier.width(240.dp).fillMaxHeight()){Column(Modifier.padding(12.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
T("Format",C.dim,10);Seg(listOf("MP4","PNG Sequence"),fmt,Modifier.fillMaxWidth()){if(!run)fmt=it}
T("Resolution",C.dim,10);Seg(listOf("Full","Half"),res,Modifier.fillMaxWidth()){if(!run)res=it}
if(fmt==0){T("Quality",C.dim,10);Seg(listOf("Low","Medium","High"),q,Modifier.fillMaxWidth()){if(!run)q=it}}
if(run||prog>0f)Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(C.hi)){Box(Modifier.fillMaxWidth(prog).fillMaxHeight().background(C.brand))}
if(msg.isNotEmpty())T(msg,C.dim,9,m=Modifier.fillMaxWidth())
if(!run)Btn("Export",C.brand,C.bg,12,Modifier.fillMaxWidth()){
val d=if(res==0)1 else 2;val w=(ed.cw/d) and 1.inv();val h=(ed.ch/d) and 1.inv()
val base=ctx.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?:ctx.filesDir
val stem=ed.name.replace(Regex("[^A-Za-z0-9_-]"),"_")+"_"+System.currentTimeMillis()
run=true;prog=0f;msg="Rendering…"
job=sc.launch{try{if(fmt==0){val f=File(base,"$stem.mp4");Exporter.mp4(ed,f,w,h,listOf(3_000_000,8_000_000,16_000_000)[q]){prog=it};msg="Saved: "+f.path}else{val f=File(base,stem);Exporter.png(ed,f,w,h){prog=it};msg="Saved: "+f.path}}catch(x:kotlin.coroutines.cancellation.CancellationException){msg="Cancelled"}catch(x:Exception){msg="Failed: "+(x.message?:x.javaClass.simpleName)};run=false}}
else Btn("Cancel",C.hi,C.text,12,Modifier.fillMaxWidth()){job?.cancel()}}}}}}
