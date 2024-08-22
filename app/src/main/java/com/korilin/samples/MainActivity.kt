package com.korilin.samples

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                                type = NetTestImageType.AsyncPainter,
                                useRv = true,
                                diffId = true
                            )
                        }


                        spacerItem {
                            ListTestItem(
                                text = "MemoryCache Painter Rv List Test",
                                type = NetTestImageType.AsyncPainter,
                                useRv = true,
                                diffId = false
                            )
                        }

                        spacerItem {
                            ListTestItem(
                                text = "NoCache Painter LazyList Test",
                                type = NetTestImageType.AsyncPainter,
                                useRv = false,
                                diffId = true
                            )
                        }


                        spacerItem {
                            ListTestItem(
                                text = "MemoryCache Painter LazyList Test",
                                type = NetTestImageType.AsyncPainter,
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

