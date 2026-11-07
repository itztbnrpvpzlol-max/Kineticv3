package com.szkinetic
import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
@Composable fun Home(open:(Ed)->Unit,newP:()->Unit,prefs:()->Unit){
val ctx=LocalContext.current
var tick by remember{mutableIntStateOf(0)}
val files=remember(tick){Store.list(ctx)}
var del by remember{mutableStateOf<File?>(null)}
Row(Modifier.fillMaxSize()){
Column(Modifier.width(150.dp).fillMaxHeight().background(C.panel).padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
T("Kinetic",C.brand,16,FontWeight.Bold,Modifier.padding(bottom=12.dp))
Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(C.hi).padding(10.dp)){T("Projects",C.text,12,FontWeight.Medium)}
Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable{prefs()}.padding(10.dp)){T("Settings",C.dim,12)}}
Column(Modifier.weight(1f).fillMaxHeight().padding(18.dp)){
Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){T("Projects",C.text,20,FontWeight.SemiBold);Spacer(Modifier.weight(1f));Btn("New Project",C.brand,C.bg,11){newP()}}
Spacer(Modifier.height(14.dp))
LazyVerticalGrid(GridCells.Adaptive(170.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
item{Box(Modifier.height(130.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp,C.faint,RoundedCornerShape(12.dp)).clickable{newP()},Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){T("+",C.brand,26);T("New Project",C.dim,11,FontWeight.Medium)}}}
items(files,key={it.name}){f->
val h=remember(f.lastModified()){Store.header(f)}
val th by produceState<Bitmap?>(null,f.lastModified()){value=withContext(Dispatchers.Default){try{val e=Store.open(f);val w=340;val hh=(w*e.ch/e.cw).coerceAtLeast(2);val b=Bitmap.createBitmap(w,hh,Bitmap.Config.ARGB_8888);e.eng.render(e.eng.dur*0.25f,b);e.eng.close();b}catch(x:Exception){null}}}
Column(Modifier.clip(RoundedCornerShape(12.dp)).background(C.panel).pointerInput(f){detectTapGestures(onTap={open(Store.open(f))},onLongPress={del=f})}){
Box(Modifier.fillMaxWidth().height(96.dp).background(Color.Black),Alignment.Center){val b=th;if(b!=null)Image(b.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)}
Column(Modifier.padding(10.dp)){T(h.getOrElse(5){"Project"},C.text,11,FontWeight.Medium);T("${h[1]}×${h[2]} · "+SimpleDateFormat("MMM d, HH:mm").format(Date(f.lastModified())),C.faint,9)}}}}}}
val d=del
if(d!=null)AlertDialog(onDismissRequest={del=null},title={T("Delete project?",C.text,14)},text={T("This cannot be undone.",C.dim,11)},confirmButton={TextButton({d.delete();del=null;tick++}){T("Delete",C.accent,12)}},dismissButton={TextButton({del=null}){T("Cancel",C.dim,12)}})
}
@Composable fun NewComp(onCancel:()->Unit,onCreate:(String,Int,Int,Float,Float)->Unit){
var name by remember{mutableStateOf("Comp 1")}
var pi by remember{mutableIntStateOf(0)}
var fi by remember{mutableIntStateOf(1)}
var di by remember{mutableIntStateOf(1)}
val ps=listOf("HD 1080" to (1920 to 1080),"720p" to (1280 to 720),"Vertical 9:16" to (1080 to 1920),"Square" to (1080 to 1080))
val fs=listOf(24f,30f,60f);val ds=listOf(5f,10f,30f)
Box(Modifier.fillMaxSize().background(Color(0xCC000000)).pointerInput(Unit){detectTapGestures{}},Alignment.Center){
Column(Modifier.width(380.dp).clip(RoundedCornerShape(16.dp)).background(C.panel).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
T("New Composition",C.text,16,FontWeight.SemiBold)
T("Name",C.dim,10)
Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(C.row).padding(10.dp)){BasicTextField(name,{name=it.take(40)},textStyle=TextStyle(color=C.text,fontSize=12.sp),cursorBrush=SolidColor(C.brand),singleLine=true,modifier=Modifier.fillMaxWidth())}
T("Preset · ${ps[pi].second.first} × ${ps[pi].second.second}",C.dim,10)
Seg(ps.map{it.first},pi,Modifier.fillMaxWidth()){pi=it}
T("Frame rate",C.dim,10);Seg(fs.map{it.toInt().toString()+" fps"},fi,Modifier.fillMaxWidth()){fi=it}
T("Duration",C.dim,10);Seg(ds.map{it.toInt().toString()+" s"},di,Modifier.fillMaxWidth()){di=it}
Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp,Alignment.End)){Btn("Cancel",C.hi,C.text,11){onCancel()};Btn("Create",C.brand,C.bg,11){onCreate(name.ifBlank{"Comp 1"},ps[pi].second.first,ps[pi].second.second,fs[fi],ds[di])}}}}}
