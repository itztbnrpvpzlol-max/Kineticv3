package com.szkinetic
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
class MainActivity:ComponentActivity(){
private fun hide(){if(!getSharedPreferences("kinetic",0).getBoolean("hide",true))return;val c=WindowInsetsControllerCompat(window,window.decorView);c.hide(WindowInsetsCompat.Type.systemBars());c.systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE}
override fun onCreate(s:Bundle?){super.onCreate(s);WindowCompat.setDecorFitsSystemWindows(window,false);hide();setContent{App()}}
override fun onWindowFocusChanged(f:Boolean){super.onWindowFocusChanged(f);if(f)hide()}
}
