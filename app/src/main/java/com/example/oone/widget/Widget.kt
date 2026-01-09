package com.example.oone.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import com.example.oone.MainActivity
import com.example.oone.R
import com.example.oone.ui.theme.colorRed

class Widget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MyWidget()
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun MyWidget() {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(Color.White)
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.voice_w),
                    contentDescription = "Add",
                    colorFilter = ColorFilter.tint(ColorProvider(Color.Black)),
                    modifier = GlanceModifier
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(10.dp)
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(Color.White)
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.image_w),
                    contentDescription = "Add",
                    colorFilter = ColorFilter.tint(ColorProvider(Color.Black)),
                    modifier = GlanceModifier
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(10.dp)
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(colorRed)
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.add_w),
                    contentDescription = "Add",
                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                    modifier = GlanceModifier
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(10.dp)
                )
            }
        }
    }
}