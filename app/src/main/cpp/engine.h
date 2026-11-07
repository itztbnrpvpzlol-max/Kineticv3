#pragma once
#include <vector>
#include <cstdint>
namespace kin{
struct Key{float t,v;int interp;float ox,ix;};
struct Track{float base=0;std::vector<Key> k;float at(float t)const;void set(float t,float v,int i);void del(float t);void move(float t0,float t1,float v);void interp(float t,int i);void infl(float t,float ox,float ix);};
struct Layer{int type=0;uint8_t r=255,g=255,b=255;float w=100,h=100,in=0,out=1e9f,a1=0,a2=0;int parent=-1,blend=0;bool vis=true;Track p[7];};
struct Comp{int w=640,h=360;float fps=30,dur=10;std::vector<Layer> L;};
void removeLayer(Comp&c,int i);
void render(const Comp&c,float t,uint32_t*px,int stride,int W,int H);
}
