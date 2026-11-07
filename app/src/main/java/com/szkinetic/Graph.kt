package com.szkinetic
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs
private fun samples(ed:Ed,l:Int,p:Int,speed:Boolean):List<Float>{val n=160;val d=ed.eng.dur;val v=List(n+1){ed.eng.get(l,p,d*it/n)};return if(!speed)v else List(n+1){if(it==0)0f else abs(v[it]-v[it-1])/(d/n)}}
private fun range(ed:Ed,l:Int,p:Int,speed:Boolean):Pair<Float,Float>{val s=samples(ed,l,p,speed);var a=s.min();var b=s.max();if(b-a<1e-3f){a-=1f;b+=1f};val pd=(b-a)*0.08f;return (a-pd) to (b+pd)}
@Composable fun Graph(ed:Ed,go:()->Unit){
val rv=ed.rev
var vis by remember{mutableStateOf(setOf(0,1,2,3,4))}
var speed by remember{mutableStateOf(false)}
var frozen by remember{mutableStateOf<Map<Int,Pair<Float,Float>>?>(null)}
val l=ed.sel
val ok=l in ed.ly.indices
val rng=remember(rv,l,speed){if(!ok)emptyMap<Int,Pair<Float,Float>>() else (0..4).associateWith{range(ed,l,it,speed)}}
val rngS by rememberUpdatedState(frozen?:rng)
val visS by rememberUpdatedState(vis)
val spS by rememberUpdatedState(speed)
Column(Modifier.fillMaxSize()){
Row(Modifier.fillMaxWidth().height(32.dp).background(C.panel).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
T("‹",C.text,18,m=Modifier.clickable{go()}.padding(horizontal=6.dp));T("Graph Editor · "+(if(ok)ed.ly[l].name else "No layer"),C.text,12,FontWeight.SemiBold);Spacer(Modifier.weight(1f))
Btn("Value Graph",if(!speed)C.hi else C.panel,if(!speed)C.text else C.dim){speed=false};Btn("Speed Graph",if(speed)C.hi else C.panel,if(speed)C.text else C.dim){speed=true}}
Row(Modifier.weight(1f).fillMaxWidth().padding(top=2.dp),horizontalArrangement=Arrangement.spacedBy(2.dp)){
Pn("Properties",Modifier.width(150.dp).fillMaxHeight()){if(ok)for(p in 0..4){val on=p in vis;Row(Modifier.fillMaxWidth().height(28.dp).clickable{vis=if(on)vis-p else vis+p}.padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(if(on)C.pc[p] else C.hi));T(PN[p],if(on)C.text else C.dim,10,m=Modifier.weight(1f));T("%.1f".format(ed.value(l,p)),C.accent,9)}}}
Pn("Graph",Modifier.weight(1f).fillMaxHeight()){
Row(Modifier.fillMaxWidth().height(26.dp).background(C.hi).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){
Btn("Linear",C.row,C.dim,9){ed.setInterp(0)};Btn("Hold",C.row,C.dim,9){ed.setInterp(1)};Btn("Easy Ease",C.brand,C.bg,9){ed.setInterp(2)};Btn("Ease In",C.row,C.dim,9){ed.setInterp(3)};Btn("Ease Out",C.row,C.dim,9){ed.setInterp(4)}}
Canvas(Modifier.weight(1f).fillMaxWidth().background(C.viewer).clipToBounds().pointerInput(l){
fun pos(t:Float,v:Float,p:Int):Offset{val r=rngS[p]?:return Offset(0f,0f);val pad=20*density;return Offset(t/ed.eng.dur*size.width,size.height-pad-(v-r.first)/(r.second-r.first)*(size.height-2*pad))}
var tgt:Triple<Int,Int,Float>?=null
detectTapGestures{o->var best:Triple<Int,Int,Float>?=null;var bd=22*density
if(ok)for(p in visS)for(k in ed.eng.kfs(l,p)){val q=pos(k.t,k.v,p);val dd=(q-o).getDistance();if(dd<bd){bd=dd;best=Triple(l,p,k.t)}};ed.selKey=best;ed.rev++}}
.pointerInput(l){
fun pos(t:Float,v:Float,p:Int):Offset{val r=rngS[p]?:return Offset(0f,0f);val pad=20*density;return Offset(t/ed.eng.dur*size.width,size.height-pad-(v-r.first)/(r.second-r.first)*(size.height-2*pad))}
var tgt:Triple<Int,Int,Float>?=null
detectDragGestures(onDragStart={o->var best:Triple<Int,Int,Float>?=null;var bd=22*density
if(ok&&!spS)for(p in visS)for(k in ed.eng.kfs(l,p)){val q=pos(k.t,k.v,p);val dd=(q-o).getDistance();if(dd<bd){bd=dd;best=Triple(l,p,k.t)}}
tgt=best;if(best!=null){ed.snap();ed.selKey=best;frozen=rngS}},onDragEnd={tgt=null;frozen=null},onDragCancel={tgt=null;frozen=null}){c,_->
val t=tgt;if(t!=null){val r=rngS[t.second]!!;val pad=20*density;val nt=ed.fr(c.position.x/size.width*ed.eng.dur).coerceIn(0f,ed.eng.dur-0.01f);val nv=r.first+(size.height-pad-c.position.y)/(size.height-2*pad)*(r.second-r.first)
ed.eng.moveKey(t.first,t.second,t.third,nt,nv);tgt=Triple(t.first,t.second,nt);ed.selKey=tgt;ed.rev++}}}){
val r0=ed.rev;val d=ed.eng.dur
for(i in 0..d.toInt()){val x=i/d*size.width;drawLine(C.hi,Offset(x,0f),Offset(x,size.height),1f)}
if(ok){val rg=rngS
for(p in vis.sorted()){val r=rg[p]?:continue;val s=samples(ed,l,p,speed);val pad=20.dp.toPx();val pa=Path()
s.forEachIndexed{i,v->val x=i.toFloat()/(s.size-1)*size.width;val y=size.height-pad-(v-r.first)/(r.second-r.first)*(size.height-2*pad);if(i==0)pa.moveTo(x,y) else pa.lineTo(x,y)}
drawPath(pa,C.pc[p],style=Stroke(2.dp.toPx()))
if(!speed){val ks=ed.eng.kfs(l,p);val sk=ed.selKey
ks.forEachIndexed{i,k->val q=Offset(k.t/d*size.width,size.height-pad-(k.v-r.first)/(r.second-r.first)*(size.height-2*pad));val on=sk!=null&&sk.first==l&&sk.second==p&&abs(sk.third-k.t)<1e-3f
if(on&&k.i>=2){val nx=ks.getOrNull(i+1);val pv=ks.getOrNull(i-1);if(nx!=null){val h=Offset(q.x+k.ox*(nx.t-k.t)/d*size.width,q.y);drawLine(C.dim,q,h,1f);drawCircle(C.dim,3.dp.toPx(),h)};if(pv!=null){val h=Offset(q.x-k.ix*(k.t-pv.t)/d*size.width,q.y);drawLine(C.dim,q,h,1f);drawCircle(C.dim,3.dp.toPx(),h)}}
drawCircle(if(on)Color.White else C.pc[p],(if(on)6f else 4.5f).dp.toPx(),q)}}}}
val px=ed.time/d*size.width;drawLine(C.accent,Offset(px,0f),Offset(px,size.height),1.5f)}}
Pn("Keyframe",Modifier.width(170.dp).fillMaxHeight()){Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
val sk=ed.selKey;val kf=if(sk!=null&&sk.first in ed.ly.indices)ed.eng.kfs(sk.first,sk.second).firstOrNull{abs(it.t-sk.third)<1e-3f} else null
if(sk==null||kf==null)T("Select a keyframe",C.faint,10) else{
val acc=remember{floatArrayOf(0f)}
Row{T("Time",C.dim,10,m=Modifier.weight(1f));Scrub("%.2f s".format(kf.t),start={ed.snap();acc[0]=0f}){d->acc[0]+=d;val st=(acc[0]/4f).toInt();if(st!=0){acc[0]-=st*4f;val nt=ed.fr(kf.t+st/ed.eng.fps).coerceIn(0f,ed.eng.dur-0.01f);ed.eng.moveKey(sk.first,sk.second,kf.t,nt,kf.v);ed.selKey=Triple(sk.first,sk.second,nt);ed.rev++}}}
Row{T("Value",C.dim,10,m=Modifier.weight(1f));Scrub("%.1f".format(kf.v),start={ed.snap()}){d->ed.eng.moveKey(sk.first,sk.second,kf.t,kf.t,kf.v+d*STEP[sk.second]);ed.rev++}}
T("Interpolation",C.dim,10)
Seg(listOf("Linear","Bezier","Hold"),when(kf.i){0->0;1->2;else->1},Modifier.fillMaxWidth()){ed.setInterp(listOf(0,2,1)[it])}
if(kf.i>=2){T("Influence In  ${(kf.ix*100).toInt()}%",C.dim,9);Sl(kf.ix,Modifier.fillMaxWidth()){v->ed.eng.infl(sk.first,sk.second,kf.t,kf.ox,v.coerceAtLeast(0.01f));ed.rev++}
T("Influence Out  ${(kf.ox*100).toInt()}%",C.dim,9);Sl(kf.ox,Modifier.fillMaxWidth()){v->ed.eng.infl(sk.first,sk.second,kf.t,v.coerceAtLeast(0.01f),kf.ix);ed.rev++}}}}}}}}
