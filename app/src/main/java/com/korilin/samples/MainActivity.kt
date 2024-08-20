package com.korilin.samples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.korilin.samples.ui.theme.ComposeglideimageTheme


class MainActivity : ComponentActivity() {

    @Composable
    private fun ListTestItem(
        text: String,
        type: NetTestImageType,
        diffId: Boolean,
        useRv: Boolean
    ) {
        Button(
            onClick = {
                ListTestActivity.start(this, useRv, diffId, type)
            }
        ) {
            Text(text)
        }
    }

    private fun LazyListScope.spacerItem(content: @Composable () -> Unit) {
        item {
            Box(
                modifier = Modifier.padding(vertical = 5.dp)
            ) {
                content()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ComposeglideimageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {

                        spacerItem {
                            ListTestItem(
                                text = "NoCache Painter Rv List Test",
                                type = NetTestImageType.Painter,
                                useRv = true,
                                diffId = true
                            )
                        }


                        spacerItem {
                            ListTestItem(
                                text = "MemoryCache Painter Rv List Test",
                                type = NetTestImageType.Painter,
                                useRv = true,
                                diffId = false
                            )
                        }

                        spacerItem {
                            ListTestItem(
                                text = "NoCache Painter LazyList Test",
                                type = NetTestImageType.Painter,
                                useRv = false,
                                diffId = true
                            )
                        }


                        spacerItem {
                            ListTestItem(
                                text = "MemoryCache Painter LazyList Test",
                                type = NetTestImageType.Painter,
                                useRv = false,
                                diffId = false
                            )
                        }

                        spacerItem {
                            ListTestItem(
                                text = "NoCache GlideImage Rv List Test",
                                type = NetTestImageType.GlideImage,
                                useRv = true,
                                diffId = true
                            )
                        }

                        spacerItem {
                            ListTestItem(
                                text = "MemoryCache GlideImage Rv List Test",
                                type = NetTestImageType.GlideImage,
                                useRv = true,
                                diffId = false
                            )
                        }

                        spacerItem {
                            ListTestItem(
                                text = "NoCache GlideImage LazyList Test",
                                type = NetTestImageType.GlideImage,
                                useRv = false,
                                diffId = true
                            )
                        }

                        spacerItem {
                            ListTestItem(
                                text = "MemoryCache GlideImage LazyList Test",
                                type = NetTestImageType.GlideImage,
                                useRv = false,
                                diffId = false
                            )
                        }


                        spacerItem {
                            ListTestItem(
                                text = "NoCache Coil Rv List Test",
                                type = NetTestImageType.Coil,
                                useRv = true,
                                diffId = true
                            )
                        }

                        spacerItem {
                            ListTestItem(
                                text = "MemoryCache Coil Rv List Test",
                                type = NetTestImageType.Coil,
                                useRv = true,
                                diffId = false
                            )
                        }

                        spacerItem {
                            ListTestItem(
                                text = "NoCache Coil LazyList Test",
                                type = NetTestImageType.Coil,
                                useRv = false,
                                diffId = true
                            )
                        }

                        spacerItem {
                            ListTestItem(
                                text = "MemoryCache Coil LazyList Test",
                                type = NetTestImageType.Coil,
                                useRv = false,
                                diffId = false
                            )
                        }
                    }
                }
            }
        }
    }
}

