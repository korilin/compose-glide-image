package com.korilin.samples

import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val rv = true
        val type = NetTestImageType.GlideImage

        setContent {
            ComposeglideimageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (rv) TestRvList(type)
                        else TestComposeList(type)
                    }
                }
            }
        }
    }
}

private const val WEBP_URL = "https://mathiasbynens.be/demo/animated-webp-supported.webp"

enum class NetTestImageType {
    Painter, GlideImage
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun NetTestImage(id: Int, type: NetTestImageType) {

    val model = "$WEBP_URL?id=$id"

    when (type) {
        NetTestImageType.Painter -> Image(
            painter = rememberGlideAsyncImagePainter(
                model
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(),
            contentScale = ContentScale.FillHeight,
        )

        NetTestImageType.GlideImage -> GlideImage(
            model = model,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp),
            contentScale = ContentScale.FillHeight,
        )
    }


}


class Holder(val composer: TestImageComposeView) : ViewHolder(composer)

class TestRvListAdapter(val type: NetTestImageType) : Adapter<Holder>() {

    val list = List(500) { it }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = TestImageComposeView(parent.context)
        view.type = type
        return Holder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.composer.pos = position
    }

}

class TestImageComposeView(context: Context) : AbstractComposeView(context) {

    override val shouldCreateCompositionOnAttachedToWindow: Boolean
        get() = true

    var pos by mutableIntStateOf(0)
    var type by mutableStateOf(NetTestImageType.Painter)

    @Composable
    override fun Content() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = pos.toString()
            )

            NetTestImage(
                id = pos,
                type = type
            )

            NetTestImage(
                id = pos,
                type = type
            )

            NetTestImage(
                id = pos,
                type = type
            )
        }
    }
}

@Composable
fun TestRvList(type: NetTestImageType) {
    val adapter = remember(type) { TestRvListAdapter(type) }
    AndroidView(
        factory = {
            RecyclerView(it).apply {
                setAdapter(adapter)
                layoutManager =
                    LinearLayoutManager(it).apply { orientation = RecyclerView.VERTICAL }
            }
        },
    )
}

@Composable
fun TestComposeList(type: NetTestImageType) {
    Column {

        Text(text = type.name, fontSize = 24.sp)

        LazyColumn {
            items(500) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = it.toString()
                    )

                    NetTestImage(
                        id = it,
                        type = type
                    )

                    NetTestImage(
                        id = it,
                        type = type
                    )

                    NetTestImage(
                        id = it,
                        type = type
                    )
                }

            }
        }
    }
}