#include "engine.h"
#include <cmath>
#include <algorithm>
namespace kin{
struct M{float a=1,b=0,c=0,d=1,e=0,f=0;};
static float cl(float v,float lo,float hi){return v<lo?lo:(v>hi?hi:v);}
static float bez(float x1,float y1,float x2,float y2,float x){float lo=0,hi=1,u=x;for(int i=0;i<24;i++){u=(lo+hi)*.5f;float m=1-u;float bx=3*m*m*u*x1+3*m*u*u*x2+u*u*u;if(bx<x)lo=u;else hi=u;}float m=1-u;return 3*m*m*u*y1+3*m*u*u*y2+u*u*u;}
static float ease(int m,float u){switch(m){case 2:return bez(.42f,0,.58f,1,u);case 3:return bez(.42f,0,1,1,u);case 4:return bez(0,0,.58f,1,u);default:return u;}}
float Track::at(float t)const{if(k.empty())return base;if(t<=k.front().t)return k.front().v;if(t>=k.back().t)return k.back().v;size_t i=1;while(k[i].t<t)i++;const Key&a=k[i-1];const Key&b=k[i];if(a.interp==1)return a.v;float u=(t-a.t)/(b.t-a.t);return a.v+(b.v-a.v)*ease(a.interp,u);}
void Track::set(float t,float v,int i){for(auto&x:k)if(std::fabs(x.t-t)<1e-4f){x.v=v;x.interp=i;return;}k.push_back({t,v,i});std::sort(k.begin(),k.end(),[](const Key&a,const Key&b){return a.t<b.t;});}
static M mul(const M&p,const M&q){return{p.a*q.a+p.c*q.b,p.b*q.a+p.d*q.b,p.a*q.c+p.c*q.d,p.b*q.c+p.d*q.d,p.a*q.e+p.c*q.f+p.e,p.b*q.e+p.d*q.f+p.f};}
static M local(const Layer&l,float t){float px=l.p[0].at(t),py=l.p[1].at(t),sc=l.p[2].at(t)/100.f,r=l.p[3].at(t)*0.0174532925f,ax=l.p[5].at(t),ay=l.p[6].at(t);float cs=std::cos(r),sn=std::sin(r);M m{sc*cs,sc*sn,-sc*sn,sc*cs,0,0};m.e=px-(m.a*ax+m.c*ay);m.f=py-(m.b*ax+m.d*ay);return m;}
static M world(const Comp&c,int i,float t){M m=local(c.L[i],t);int p=c.L[i].parent,g=0;while(p>=0&&p<(int)c.L.size()&&g<32){m=mul(local(c.L[p],t),m);p=c.L[p].parent;g++;}return m;}
static void put(uint32_t&d,float sr,float sg,float sb,float sa,int mode){float dr=(float)(d&255),dg=(float)((d>>8)&255),db=(float)((d>>16)&255);float br=sr,bg=sg,bb=sb;if(mode==1){br=std::min(255.f,dr+sr);bg=std::min(255.f,dg+sg);bb=std::min(255.f,db+sb);}else if(mode==2){br=dr*sr/255.f;bg=dg*sg/255.f;bb=db*sb/255.f;}else if(mode==3){br=255.f-(255.f-dr)*(255.f-sr)/255.f;bg=255.f-(255.f-dg)*(255.f-sg)/255.f;bb=255.f-(255.f-db)*(255.f-sb)/255.f;}
uint32_t r=(uint32_t)cl(dr+(br-dr)*sa+.5f,0,255),g=(uint32_t)cl(dg+(bg-dg)*sa+.5f,0,255),b=(uint32_t)cl(db+(bb-db)*sa+.5f,0,255);d=0xFF000000u|(b<<16)|(g<<8)|r;}
static void adjust(const Layer&l,float op,uint32_t*px,int stride,int W,int H){float br=l.a1*2.55f*op,ct=l.a2*2.55f*op;float f=(259.f*(ct+255.f))/(255.f*(259.f-ct));for(int y=0;y<H;y++)for(int x=0;x<W;x++){uint32_t&d=px[y*stride+x];float c0=(float)(d&255),c1=(float)((d>>8)&255),c2=(float)((d>>16)&255);c0=cl(f*(c0-128)+128+br,0,255);c1=cl(f*(c1-128)+128+br,0,255);c2=cl(f*(c2-128)+128+br,0,255);d=0xFF000000u|((uint32_t)c2<<16)|((uint32_t)c1<<8)|(uint32_t)c0;}}
void render(const Comp&c,float t,uint32_t*px,int stride,int W,int H){
for(int y=0;y<H;y++)for(int x=0;x<W;x++)px[y*stride+x]=0xFF000000u;
float sx=(float)W/(float)c.w,sy=(float)H/(float)c.h;
for(size_t i=0;i<c.L.size();i++){const Layer&l=c.L[i];if(!l.vis||t<l.in||t>=l.out||l.type==2)continue;
float op=cl(l.p[4].at(t)/100.f,0,1);if(op<=0)continue;
if(l.type==3){adjust(l,op,px,stride,W,H);continue;}
M m=world(c,(int)i,t);M sc{sx,0,0,sy,0,0};m=mul(sc,m);
float det=m.a*m.d-m.b*m.c;if(std::fabs(det)<1e-6f)continue;
float ia=m.d/det,ib=-m.b/det,ic=-m.c/det,id=m.a/det;float ie=-(ia*m.e+ic*m.f),jf=-(ib*m.e+id*m.f);
float xs[4]={0,l.w,0,l.w},ys[4]={0,0,l.h,l.h};float x0=1e9f,x1=-1e9f,y0=1e9f,y1=-1e9f;
for(int k=0;k<4;k++){float X=m.a*xs[k]+m.c*ys[k]+m.e,Y=m.b*xs[k]+m.d*ys[k]+m.f;x0=std::min(x0,X);x1=std::max(x1,X);y0=std::min(y0,Y);y1=std::max(y1,Y);}
int bx0=std::max(0,(int)std::floor(x0)-1),bx1=std::min(W-1,(int)std::ceil(x1)+1),by0=std::max(0,(int)std::floor(y0)-1),by1=std::min(H-1,(int)std::ceil(y1)+1);
float s=std::sqrt(std::fabs(det));
for(int y=by0;y<=by1;y++)for(int x=bx0;x<=bx1;x++){float X=x+.5f,Y=y+.5f;float lx=ia*X+ic*Y+ie,ly=ib*X+id*Y+jf;float cov;
if(l.type==1){float hw=l.w*.5f,hh=l.h*.5f;float nx=(lx-hw)/hw,ny=(ly-hh)/hh;float r=std::sqrt(nx*nx+ny*ny);cov=cl((1.f-r)*std::min(hw,hh)*s+.5f,0,1);}
else{float dm=std::min(std::min(lx,l.w-lx),std::min(ly,l.h-ly));cov=cl(dm*s+.5f,0,1);}
if(cov<=0)continue;put(px[y*stride+x],l.r,l.g,l.b,cov*op,l.blend);}}
}
}
