package com.korilin.samples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
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
import kotlin.properties.Delegates

class ListTestActivity : ComponentActivity() {

    companion object {
        fun start(
            context: Context,
            rv: Boolean,
            diffId: Boolean,
            type: NetTestImageType
        ) {
            val intent = Intent(context, ListTestActivity::class.java)
            intent.putExtra("use_rv", rv)
            intent.putExtra("diff_id", diffId)
            intent.putExtra("type", type)
            context.startActivity(intent)
        }
    }

    private var rv by Delegates.notNull<Boolean>()
    private var diffId by Delegates.notNull<Boolean>()
    private lateinit var type: NetTestImageType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rv = intent.getBooleanExtra("use_rv", false)
        diffId = intent.getBooleanExtra("diff_id", false)
        type = intent.getSerializableExtra("type") as NetTestImageType

        enableEdgeToEdge()

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


    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun NetAvatarImage(model: String) {
        GlideImage(
            model,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp),
            contentScale = ContentScale.Crop,
        )
    }


    abstract class Holder(val composer: TestImageComposeView) : ViewHolder(composer)
    class Holder1(composer: TestImageComposeView) : Holder(composer)
    class Holder2(composer: TestImageComposeView) : Holder(composer)

    inner class TestRvListAdapter(private val type: NetTestImageType) : Adapter<Holder>() {

        val list = List(500) { it }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = TestImageComposeView(parent.context)
            view.type = type
            return when (viewType) {
                0 -> Holder1(view)
                1 -> Holder2(view)
                else -> throw IllegalStateException("Unsupported viewType")
            }
        }

        override fun getItemViewType(position: Int): Int {
            return position % 2
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.composer.pos = position
        }

    }

    inner class TestImageComposeView(context: Context) : AbstractComposeView(context) {

        override val shouldCreateCompositionOnAttachedToWindow: Boolean
            get() = true

        var pos by mutableIntStateOf(0)
        var type by mutableStateOf(NetTestImageType.AsyncPainter)

        @Composable
        override fun Content() {
            TestItem(type, pos)
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
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun TestComposeList(type: NetTestImageType) {
        Column {

            Text(text = type.name, fontSize = 24.sp)

            LazyColumn {
                items(500) {
                    TestItem(type, it)
                }
            }
        }
    }

    @Composable
    fun TestItem(type: NetTestImageType, id: Int) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = id.toString()
            )

            Spacer(modifier = Modifier.width(10.dp))

            NetAvatarImage(STATIC_URL)

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(
                    horizontalArrangement = Arrangement.Start
                ) {

                    val models = remember {
                         List(4) { WEBP_URL }
                    }

                    for (index in models.indices) {
                        val model = models[index]
                        val idTag = "${id}_${index}"
                        val urlModel = if (diffId) "${model}?id=$idTag" else model
                        NetTestImage(
                            tag = idTag,
                            model = urlModel,
                            type = type
                        )
                    }
                }

                Text("Glide Image Test")
            }
        }
    }

}