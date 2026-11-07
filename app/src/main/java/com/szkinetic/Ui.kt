package com.szkinetic
import android.graphics.RectF
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.min
object C{val bg=Color(0xFF121212);val panel=Color(0xFF1D1D1D);val hi=Color(0xFF262626);val row=Color(0xFF202020);val sel=Color(0xFF2B2745);val text=Color(0xFFD6D6D6);val dim=Color(0xFF8C8C8C);val faint=Color(0xFF5A5A5A);val accent=Color(0xFF2D8CEB);val brand=Color(0xFF9B8CFF);val viewer=Color(0xFF0A0A0A);val track=Color(0xFF181818)
val lbl=listOf(Color(0xFF8E7CF0),Color(0xFFD0605E),Color(0xFFA8A8A8),Color(0xFF4F94DB),Color(0xFF5DB070))
val pc=listOf(Color(0xFFD0605E),Color(0xFF5DB070),Color(0xFF4F94DB),Color(0xFF8E7CF0),Color(0xFFA8A8A8))}
val PN=listOf("X Position","Y Position","Scale","Rotation","Opacity")
val STEP=listOf(1f,1f,0.5f,0.5f,0.5f)
@Composable fun T(s:String,c:Color=C.text,sz:Int=11,w:FontWeight=FontWeight.Normal,m:Modifier=Modifier)=Text(s,color=c,fontSize=sz.sp,fontWeight=w,modifier=m,maxLines=1)
@Composable fun Pn(title:String,m:Modifier,body:@Composable ColumnScope.()->Unit){Column(m.background(C.panel)){Box(Modifier.fillMaxWidth().height(22.dp).background(C.hi),Alignment.CenterStart){Box(Modifier.fillMaxHeight().background(C.panel).padding(horizontal=10.dp),Alignment.Center){T(title,C.text,10,FontWeight.Medium)}};body()}}
@Composable fun Btn(s:String,bg:Color=C.hi,fg:Color=C.text,sz:Int=10,m:Modifier=Modifier,on:()->Unit){Box(m.clip(RoundedCornerShape(5.dp)).background(bg).clickable{on()}.padding(horizontal=9.dp,vertical=5.dp),Alignment.Center){T(s,fg,sz,FontWeight.Medium)}}
@Composable fun Seg(items:List<String>,act:Int,m:Modifier=Modifier,on:(Int)->Unit){Row(m.clip(RoundedCornerShape(7.dp)).background(C.row).padding(2.dp),horizontalArrangement=Arrangement.spacedBy(2.dp)){items.forEachIndexed{i,s->Box(Modifier.weight(1f).clip(RoundedCornerShape(5.dp)).background(if(i==act)C.hi else Color.Transparent).clickable{on(i)}.padding(vertical=6.dp),Alignment.Center){T(s,if(i==act)C.text else C.dim,10,if(i==act)FontWeight.Medium else FontWeight.Normal)}}}}
@Composable fun Tg(on:Boolean,set:(Boolean)->Unit){Box(Modifier.width(36.dp).height(20.dp).clip(RoundedCornerShape(10.dp)).background(if(on)C.brand else C.hi).clickable{set(!on)}.padding(2.dp),if(on)Alignment.CenterEnd else Alignment.CenterStart){Box(Modifier.size(16.dp).clip(CircleShape).background(Color.White))}}
@Composable fun Sl(v:Float,m:Modifier=Modifier,on:(Float)->Unit){val cb by rememberUpdatedState(on)
Canvas(m.height(18.dp).pointerInput(Unit){detectTapGestures{o->cb((o.x/size.width).coerceIn(0f,1f))}}.pointerInput(Unit){detectDragGestures(onDragStart={o->cb((o.x/size.width).coerceIn(0f,1f))}){c,_->cb((c.position.x/size.width).coerceIn(0f,1f))}}){val y=size.height/2;drawLine(C.hi,Offset(0f,y),Offset(size.width,y),4.dp.toPx());drawLine(C.brand,Offset(0f,y),Offset(size.width*v,y),4.dp.toPx());drawCircle(Color.White,7.dp.toPx(),Offset(size.width*v,y))}}
@Composable fun Scrub(s:String,col:Color=C.accent,m:Modifier=Modifier,start:()->Unit={},d:(Float)->Unit){val cd by rememberUpdatedState(d);val cs by rememberUpdatedState(start)
T(s,col,9,FontWeight.Medium,m.pointerInput(Unit){detectHorizontalDragGestures(onDragStart={cs()}){c,x->c.consume();cd(x/density)}})}
@Composable fun App(){
val ctx=LocalContext.current
val cfg=remember{Cfg(ctx)}
var scr by remember{mutableIntStateOf(0)}
var back by remember{mutableIntStateOf(0)}
var ed by remember{mutableStateOf<Ed?>(null)}
var dlg by remember{mutableStateOf(false)}
val dn=LocalDensity.current
remember{Store.seed(ctx)}
LaunchedEffect(cfg.hide){val w=(ctx as ComponentActivity).window;val c=WindowInsetsControllerCompat(w,w.decorView);if(cfg.hide)c.hide(WindowInsetsCompat.Type.systemBars()) else c.show(WindowInsetsCompat.Type.systemBars())}
LaunchedEffect(ed,cfg.auto){while(ed!=null&&cfg.auto){delay(30000);ed?.let{Store.save(ctx,it)}}}
fun leave(){ed?.let{it.playing=false;Store.save(ctx,it);it.eng.close()};ed=null;scr=0}
BackHandler(scr!=0||dlg){if(dlg)dlg=false else if(scr==1)leave() else scr=back}
CompositionLocalProvider(LocalDensity provides Density(dn.density*cfg.scale,dn.fontScale)){
MaterialTheme(colorScheme=darkColorScheme()){
Box(Modifier.fillMaxSize().background(C.bg).displayCutoutPadding()){
val e=ed
when{
scr==0->Home(open={ed=it;scr=1},newP={dlg=true},prefs={back=0;scr=3})
e==null->{scr=0}
scr==1->Editor(e!!,cfg,go={n->back=1;scr=n},leave={leave()})
scr==2->Graph(e!!,go={scr=1})
scr==3->Prefs(cfg,close={scr=back})
else->ExportScr(e!!,close={scr=1})}
if(dlg)NewComp(onCancel={dlg=false},onCreate={n,w,h,f,d->val x=Ed("p"+System.currentTimeMillis(),n,w,h,f,d);Store.save(ctx,x);ed=x;scr=1;dlg=false})
}}}}
@Composable fun Editor(ed:Ed,cfg:Cfg,go:(Int)->Unit,leave:()->Unit){
LaunchedEffect(ed.playing){if(ed.playing){var last=0L;while(true){val n=withFrameNanos{it};if(last!=0L){val nt=ed.time+(n-last)/1e9f;ed.time=if(nt>=ed.eng.dur-0.01f)0f else nt};last=n}}}
var menu by remember{mutableStateOf(false)}
var tool by remember{mutableIntStateOf(0)}
var zi by remember{mutableIntStateOf(0)}
val zs=listOf(1f,1.5f,2f,0.5f)
val rv=ed.rev
val has=ed.sel in ed.ly.indices
Column(Modifier.fillMaxSize()){
Row(Modifier.fillMaxWidth().height(32.dp).background(C.panel).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
T("‹",C.text,18,m=Modifier.clickable{leave()}.padding(horizontal=6.dp));T("Kinetic",C.brand,12,FontWeight.SemiBold);T(ed.name,C.dim,10)
Box{Btn("+ Layer"){menu=true};DropdownMenu(expanded=menu,onDismissRequest={menu=false}){listOf("Solid","Shape","Null Object","Adjustment Layer").forEachIndexed{i,s->DropdownMenuItem(text={T(s,C.text,11)},onClick={ed.snap();ed.addLayer(i);menu=false})}}}
Btn("Delete",C.hi,if(has)C.text else C.faint){if(has){ed.snap();ed.removeLayer(ed.sel)}}
Spacer(Modifier.weight(1f))
Btn("↶",C.hi,if(ed.canUndo)C.text else C.faint){ed.undo()};Btn("↷",C.hi,if(ed.canRedo)C.text else C.faint){ed.redo()}
Btn("Graph"){go(2)};Btn("⚙"){go(3)};Btn("Export",C.brand,C.bg){go(4)}}
Row(Modifier.weight(1f).fillMaxWidth()){
Column(Modifier.width(36.dp).fillMaxHeight().background(C.panel).padding(vertical=6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(3.dp)){listOf("V","R","S","Z").forEachIndexed{i,s->Box(Modifier.size(26.dp).clip(RoundedCornerShape(5.dp)).background(if(i==tool)C.hi else Color.Transparent).clickable{if(i==3)zi=(zi+1)%zs.size else tool=i},Alignment.Center){T(s,if(i==tool)C.brand else C.dim,11,FontWeight.Medium)}}}
Column(Modifier.weight(1f).fillMaxHeight()){
Row(Modifier.weight(1.1f).fillMaxWidth().padding(top=2.dp),horizontalArrangement=Arrangement.spacedBy(2.dp)){
Pn("Project",Modifier.width(130.dp).fillMaxHeight()){Column(Modifier.padding(8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
T(ed.name,C.text,11,FontWeight.Medium);T("${ed.cw} × ${ed.ch}",C.dim,9);T("${ed.eng.fps.toInt()} fps · ${ed.eng.dur.toInt()} s",C.dim,9);T("${ed.ly.size} layers",C.dim,9)
Spacer(Modifier.height(4.dp))
listOf("Solid","Shape","Null Object","Adjustment").forEachIndexed{i,s->Btn("+ $s",C.row,C.dim,9,Modifier.fillMaxWidth()){ed.snap();ed.addLayer(i)}}}}
Pn("Composition",Modifier.weight(1f).fillMaxHeight()){
Canvas(Modifier.weight(1f).fillMaxWidth().background(C.viewer).clipToBounds().pointerInput(tool,ed){detectDragGestures(onDragStart={ed.snap()}){c,d->
if(ed.sel in ed.ly.indices){val sc=min(size.width/ed.cw.toFloat(),size.height/ed.ch.toFloat())*0.94f;val l=ed.sel
when(tool){0->{ed.setValue(l,Pr.X,ed.value(l,Pr.X)+d.x/sc);ed.setValue(l,Pr.Y,ed.value(l,Pr.Y)+d.y/sc)};1->ed.setValue(l,Pr.R,ed.value(l,Pr.R)+d.x*0.3f);else->ed.setValue(l,Pr.S,(ed.value(l,Pr.S)+d.x*0.3f).coerceAtLeast(1f))}}}}){
val r0=ed.rev;val b=ed.bitmap(cfg.prev);ed.eng.render(ed.time,b)
val sc=min(size.width/b.width,size.height/b.height)*0.94f;val dw=b.width*sc;val dh=b.height*sc;val l0=(size.width-dw)/2;val t0=(size.height-dh)/2
drawIntoCanvas{c->c.nativeCanvas.drawBitmap(b,null,RectF(l0,t0,l0+dw,t0+dh),null)}
if(cfg.safe){drawRect(Color.White,Offset(l0+dw*0.05f,t0+dh*0.05f),Size(dw*0.9f,dh*0.9f),alpha=0.5f,style=Stroke(1f));drawRect(Color.White,Offset(l0+dw*0.1f,t0+dh*0.1f),Size(dw*0.8f,dh*0.8f),alpha=0.5f,style=Stroke(1f))}}
Row(Modifier.fillMaxWidth().height(20.dp).background(C.hi).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){T(listOf("Move","Rotate","Scale","Move")[tool],C.dim,9);T("Preview 1/${cfg.prev}",C.dim,9);Spacer(Modifier.weight(1f));T(ed.tc(),C.text,9,FontWeight.Medium)}}
Column(Modifier.width(130.dp).fillMaxHeight(),verticalArrangement=Arrangement.spacedBy(2.dp)){
Pn("Info",Modifier.weight(1f).fillMaxWidth()){Column(Modifier.padding(horizontal=10.dp,vertical=6.dp),verticalArrangement=Arrangement.spacedBy(3.dp)){
if(has)for(p in 0..4)Row{T(PN[p]+" :",C.dim,9,m=Modifier.width(60.dp));T("%.1f".format(ed.value(ed.sel,p)),C.text,9)}}}
Pn("Layer",Modifier.weight(1f).fillMaxWidth()){Column(Modifier.padding(horizontal=8.dp,vertical=4.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
if(has){val inf=ed.eng.info(ed.sel);val bn=listOf("Normal","Add","Multiply","Screen")
Btn("Blend: "+bn[inf[9].toInt().coerceIn(0,3)],C.row,C.text,9,Modifier.fillMaxWidth()){ed.snap();ed.edit(ed.sel){it[9]=((it[9].toInt()+1)%4).toFloat()}}
if(inf[0].toInt()==3){Row{T("Brightness",C.dim,9,m=Modifier.width(60.dp));Scrub("%.0f".format(inf[10]),start={ed.snap()}){d->ed.edit(ed.sel){it[10]=(it[10]+d*0.5f).coerceIn(-100f,100f)}}}
Row{T("Contrast",C.dim,9,m=Modifier.width(60.dp));Scrub("%.0f".format(inf[11]),start={ed.snap()}){d->ed.edit(ed.sel){it[11]=(it[11]+d*0.5f).coerceIn(-100f,100f)}}}}
Btn("+ Brightness/Contrast",C.row,C.dim,8,Modifier.fillMaxWidth()){ed.snap();ed.addLayer(3)}}}}}}
Timeline(ed,cfg,zs[zi],Modifier.weight(1f).fillMaxWidth().padding(top=2.dp))
}}}}
@Composable fun Timeline(ed:Ed,cfg:Cfg,zoom:Float,m:Modifier){
val rv=ed.rev
val vs=rememberScrollState()
var sx by remember{mutableFloatStateOf(0f)}
val rows=remember(ed.open,ed.ly.size){val r=mutableListOf<Pair<Int,Int>>();for(i in ed.ly.indices.reversed()){r.add(i to -1);if(ed.ly[i] in ed.open)for(p in 0..4)r.add(i to p)};r.toList()}
val rowsS by rememberUpdatedState(rows)
val paint=remember{android.graphics.Paint().apply{color=0xFF8C8C8C.toInt();isAntiAlias=true}}
Column(m.background(C.panel)){
Row(Modifier.fillMaxWidth().height(24.dp).background(C.hi).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
T(ed.tc(),C.accent,13,FontWeight.SemiBold);T("${ed.eng.fps.toInt()} fps",C.faint,8)
Btn("Linear",C.row,C.dim,8){ed.setInterp(0)};Btn("Easy Ease",C.row,C.dim,8){ed.setInterp(2)};Btn("Hold",C.row,C.dim,8){ed.setInterp(1)}
Spacer(Modifier.weight(1f))
Box(Modifier.clickable{ed.seek(0f)}.padding(4.dp)){T("⏮",C.dim,12)}
Box(Modifier.clickable{ed.playing=!ed.playing}.padding(4.dp)){T(if(ed.playing)"⏸" else "▶",C.text,13)}
Box(Modifier.clickable{ed.seek(ed.eng.dur)}.padding(4.dp)){T("⏭",C.dim,12)}}
Row(Modifier.weight(1f).fillMaxWidth()){
Column(Modifier.width(210.dp).fillMaxHeight()){
Row(Modifier.fillMaxWidth().height(18.dp).background(C.hi).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){T("#",C.dim,9,m=Modifier.width(48.dp));T("Layer Name",C.dim,9,m=Modifier.weight(1f));T("Parent",C.dim,9)}
Column(Modifier.verticalScroll(vs)){rows.forEachIndexed{ri,r->val li=r.first;val l=ed.ly[li]
if(r.second<0){val inf=ed.eng.info(li);val s=ed.sel==li
Row(Modifier.fillMaxWidth().height(22.dp).background(if(s)C.sel else if(ri%2==1)C.row else C.panel).clickable{ed.sel=li;ed.open=if(l in ed.open)ed.open-l else ed.open+l}.padding(horizontal=6.dp),verticalAlignment=Alignment.CenterVertically){
Box(Modifier.size(16.dp).clickable{ed.toggleVis(li)},Alignment.Center){T(if(inf[12]!=0f)"●" else "○",C.dim,9)}
Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(l.col));Spacer(Modifier.width(4.dp))
T((li+1).toString(),C.dim,9,m=Modifier.width(14.dp));T(if(l in ed.open)"▾" else "▸",C.dim,9,m=Modifier.width(12.dp))
T(l.name,C.text,10,if(s)FontWeight.Medium else FontWeight.Normal,Modifier.weight(1f))
val pi=inf[8].toInt();Box(Modifier.clickable{ed.cycleParent(li)}.padding(2.dp)){T(if(pi in ed.ly.indices)ed.ly[pi].name.take(8) else "None",C.dim,9)}}}
else{val p=r.second;val kk=ed.eng.kfs(li,p)
Row(Modifier.fillMaxWidth().height(18.dp).background(if(ri%2==1)C.row else C.panel).padding(start=30.dp,end=6.dp),verticalAlignment=Alignment.CenterVertically){
Box(Modifier.clickable{ed.toggleWatch(li,p)}.padding(horizontal=3.dp)){T("◷",if(kk.isNotEmpty())C.accent else C.faint,9)}
T(PN[p],C.dim,9,m=Modifier.weight(1f))
Box(Modifier.clickable{ed.toggleKey(li,p)}.padding(horizontal=4.dp)){T(if(ed.at(li,p)!=null)"◆" else "◇",C.accent,10)}
Scrub("%.1f".format(ed.value(li,p)),start={ed.snap()}){d->ed.setValue(li,p,ed.value(li,p)+d*STEP[p])}}}}}}
Column(Modifier.weight(1f).fillMaxHeight()){
Canvas(Modifier.fillMaxWidth().height(18.dp).background(C.hi).clipToBounds().pointerInput(ed,zoom){val pp=80.dp.toPx()*zoom
detectTapGestures{o->ed.seek((o.x+sx)/pp,cfg.snap)}}.pointerInput(ed,zoom){val pp=80.dp.toPx()*zoom
detectDragGestures(onDragStart={o->ed.seek((o.x+sx)/pp,cfg.snap)}){c,_->ed.seek((c.position.x+sx)/pp,cfg.snap)}}){
val pp=80.dp.toPx()*zoom;paint.textSize=8.dp.toPx()
translate(-sx,0f){for(i in 0..(ed.eng.dur*2).toInt()){val x=i*pp/2;val hh=if(i%2==0)8.dp.toPx() else 4.dp.toPx();drawLine(C.faint,Offset(x,size.height-hh),Offset(x,size.height),1f)
if(i%2==0)drawIntoCanvas{c->c.nativeCanvas.drawText((i/2).toString()+"s",x+3.dp.toPx(),9.dp.toPx(),paint)}}
val px=ed.time*pp;val tri=Path();tri.moveTo(px-5.dp.toPx(),0f);tri.lineTo(px+5.dp.toPx(),0f);tri.lineTo(px,9.dp.toPx());tri.close();drawPath(tri,C.accent)}}
Canvas(Modifier.fillMaxWidth().weight(1f).background(C.track).clipToBounds().pointerInput(ed,zoom){
val pp=80.dp.toPx()*zoom
detectTapGestures{o->val h=hitAt(o,rowsS,ed,pp,sx,vs.value.toFloat(),density);if(h!=null){ed.sel=h.first;ed.selKey=if(h.second>=0)h else null}else ed.selKey=null;ed.rev++}}.pointerInput(ed,zoom){
val pp=80.dp.toPx()*zoom;var tgt:Triple<Int,Int,Float>?=null
detectDragGestures(onDragStart={o->tgt=hitAt(o,rowsS,ed,pp,sx,vs.value.toFloat(),density);ed.snap();tgt?.let{ed.sel=it.first;if(it.second>=0)ed.selKey=it}},onDragEnd={tgt=null},onDragCancel={tgt=null}){c,d->
val t=tgt
if(t==null){sx=(sx-d.x).coerceIn(0f,maxOf(0f,ed.eng.dur*pp-size.width+40f))}
else if(t.second>=0){val nt=ed.fr((c.position.x+sx)/pp).coerceIn(0f,ed.eng.dur-0.01f);if(abs(nt-t.third)>1e-4f){val kk=ed.eng.kfs(t.first,t.second).firstOrNull{abs(it.t-t.third)<1e-3f};if(kk!=null){ed.eng.moveKey(t.first,t.second,t.third,nt,kk.v);tgt=Triple(t.first,t.second,nt);ed.selKey=tgt;ed.rev++}}}
else ed.shift(t.first,d.x/pp)}}){
val r0=ed.rev;val pp=80.dp.toPx()*zoom;val lhp=22.dp.toPx();val php=18.dp.toPx();var y=-vs.value.toFloat()
translate(-sx,0f){
rows.forEachIndexed{ri,r->val l=ed.ly[r.first];val h=if(r.second<0)lhp else php;val isSel=ed.sel==r.first
drawRect(if(r.second<0&&isSel)C.sel else if(ri%2==1)C.row else C.panel,Offset(sx,y),Size(size.width,h),alpha=0.4f)
if(r.second<0){val f=ed.eng.info(r.first);val a=f[6];val z=min(f[7],ed.eng.dur);drawRoundRect(l.col,Offset(a*pp,y+3.dp.toPx()),Size((z-a)*pp,h-6.dp.toPx()),CornerRadius(3.dp.toPx()),alpha=if(isSel)0.9f else 0.55f)}
else{val sk=ed.selKey;ed.eng.kfs(r.first,r.second).forEach{k->val cx=k.t*pp;val cy=y+h/2;val on=sk!=null&&sk.first==r.first&&sk.second==r.second&&abs(sk.third-k.t)<1e-3f;val d=(if(on)5.5f else 4f).dp.toPx();val pa=Path();pa.moveTo(cx,cy-d);pa.lineTo(cx+d,cy);pa.lineTo(cx,cy+d);pa.lineTo(cx-d,cy);pa.close();drawPath(pa,if(on)Color.White else C.accent)}}
y+=h}
val px=ed.time*pp;drawLine(C.accent,Offset(px,0f),Offset(px,size.height),1.5f)}}
}}}}
fun hitAt(o:Offset,rows:List<Pair<Int,Int>>,ed:Ed,pp:Float,sx:Float,vy:Float,dn:Float):Triple<Int,Int,Float>?{
var y=-vy;val x=o.x+sx
for(r in rows){val h=(if(r.second<0)22f else 18f)*dn
if(o.y>=y&&o.y<y+h){if(r.second>=0){for(k in ed.eng.kfs(r.first,r.second))if(abs(k.t*pp-x)<12*dn)return Triple(r.first,r.second,k.t)}else{val f=ed.eng.info(r.first);if(x>=f[6]*pp&&x<=min(f[7],ed.eng.dur)*pp)return Triple(r.first,-1,0f)};return null}
y+=h}
return null}
