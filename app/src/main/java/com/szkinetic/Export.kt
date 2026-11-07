package com.szkinetic
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
object Exporter{
suspend fun png(ed:Ed,dir:File,w:Int,h:Int,prog:(Float)->Unit){withContext(Dispatchers.Default){
dir.mkdirs();val fps=ed.eng.fps;val n=(ed.eng.dur*fps).toInt();val b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
for(f in 0 until n){ensureActive();ed.eng.render(f/fps,b);FileOutputStream(File(dir,"frame_%04d.png".format(f))).use{b.compress(Bitmap.CompressFormat.PNG,100,it)};prog((f+1f)/n)}}}
suspend fun mp4(ed:Ed,out:File,w:Int,h:Int,bitrate:Int,prog:(Float)->Unit){withContext(Dispatchers.Default){
val fps=ed.eng.fps.toInt();val n=(ed.eng.dur*fps).toInt()
val fmt=MediaFormat.createVideoFormat("video/avc",w,h)
fmt.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
fmt.setInteger(MediaFormat.KEY_BIT_RATE,bitrate);fmt.setInteger(MediaFormat.KEY_FRAME_RATE,fps);fmt.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1)
val enc=MediaCodec.createEncoderByType("video/avc");enc.configure(fmt,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);enc.start()
val mux=MediaMuxer(out.path,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
var tr=-1;var started=false
val bmp=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);val px=IntArray(w*h);val info=MediaCodec.BufferInfo()
fun drain(eos:Boolean){while(true){val o=enc.dequeueOutputBuffer(info,if(eos)10000L else 0L)
if(o==MediaCodec.INFO_TRY_AGAIN_LATER){if(!eos)return}
else if(o==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){tr=mux.addTrack(enc.outputFormat);mux.start();started=true}
else if(o>=0){val buf=enc.getOutputBuffer(o)!!
if((info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0)info.size=0
if(info.size>0&&started){buf.position(info.offset);buf.limit(info.offset+info.size);mux.writeSampleData(tr,buf,info)}
enc.releaseOutputBuffer(o,false)
if((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0)return}}}
try{
for(f in 0 until n){ensureActive()
var idx=enc.dequeueInputBuffer(10000L);while(idx<0){drain(false);idx=enc.dequeueInputBuffer(10000L)}
ed.eng.render(f.toFloat()/fps,bmp);bmp.getPixels(px,0,w,0,0,w,h)
val img=enc.getInputImage(idx)!!;val pl=img.planes;val yb=pl[0].buffer;val ub=pl[1].buffer;val vb=pl[2].buffer
val ys=pl[0].rowStride;val us=pl[1].rowStride;val up=pl[1].pixelStride;val vs=pl[2].rowStride;val vp=pl[2].pixelStride
for(y in 0 until h){for(x in 0 until w){val c=px[y*w+x];val r=(c shr 16) and 255;val g=(c shr 8) and 255;val b=c and 255
yb.put(y*ys+x,(((66*r+129*g+25*b+128) shr 8)+16).toByte())
if((y and 1)==0&&(x and 1)==0){val uy=y shr 1;val ux=x shr 1;ub.put(uy*us+ux*up,(((-38*r-74*g+112*b+128) shr 8)+128).toByte());vb.put(uy*vs+ux*vp,(((112*r-94*g-18*b+128) shr 8)+128).toByte())}}}
enc.queueInputBuffer(idx,0,w*h*3/2,f*1000000L/fps,0);drain(false);prog((f+1f)/n)}
var e=enc.dequeueInputBuffer(10000L);while(e<0){drain(false);e=enc.dequeueInputBuffer(10000L)}
enc.queueInputBuffer(e,0,0,n*1000000L/fps,MediaCodec.BUFFER_FLAG_END_OF_STREAM);drain(true)
}finally{try{enc.stop()}catch(x:Exception){};enc.release();try{if(started)mux.stop()}catch(x:Exception){};mux.release()}}}
}
