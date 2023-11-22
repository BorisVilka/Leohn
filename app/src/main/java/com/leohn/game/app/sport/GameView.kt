package com.leohn.game.app.sport


import android.R.attr
import android.R.attr.min
import android.R.attr.x
import android.R.attr.y
import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.lang.Math.max
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt


class GameView(val ctx: Context, val attributeSet: AttributeSet): SurfaceView(ctx,attributeSet) {

    var color = ctx.getColor(R.color.bg)
    var bg = BitmapFactory.decodeResource(ctx.resources,R.drawable.bg1)
    var center = BitmapFactory.decodeResource(ctx.resources,R.drawable.centr)
    var v1 = BitmapFactory.decodeResource(ctx.resources,R.drawable.v1)
    var v2 = BitmapFactory.decodeResource(ctx.resources,R.drawable.v2)
    var tap = BitmapFactory.decodeResource(ctx.resources,R.drawable.tap)
    var ball = BitmapFactory.decodeResource(ctx.resources,R.drawable.ball)
    var fish = mutableListOf(
        BitmapFactory.decodeResource(ctx.resources,R.drawable.t1),
        BitmapFactory.decodeResource(ctx.resources,R.drawable.t2),
        BitmapFactory.decodeResource(ctx.resources,R.drawable.t3),
        BitmapFactory.decodeResource(ctx.resources,R.drawable.t4),
        BitmapFactory.decodeResource(ctx.resources,R.drawable.t5),
    )

    var millis = 0
    var paused = false
    private var paintB: Paint = Paint(Paint.DITHER_FLAG)
    private var paintT: Paint = Paint(Paint.DITHER_FLAG).apply {
        textSize = 80f
        color = ctx.getColor(R.color.text)
        typeface = ctx.resources.getFont(R.font.font)
    }
    var destroy = false
    private var listener: EndListener? = null
    val updateThread = Thread {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if(!destroy) {
                    if(!paused) millis++
                    update.run()
                }
            }
        }, 500, 16)
    }

    var bx = 0f
    var by = 0f

    var cy = 0f
    var cx = 0f

    var height1 = 0f

    val music1 = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("music",false)
    val sounds = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("sounds",false)

    var music = MediaPlayer.create(ctx,R.raw.music)
    var kick = MediaPlayer.create(ctx,R.raw.tap)
    var st = MediaPlayer.create(ctx,R.raw.start)

    init {
        music.setOnCompletionListener { it.start() }
        if(music1) music.start()
        for(i in fish.indices) fish[i] = Bitmap.createScaledBitmap(fish[i],fish[i].width/3,fish[i].height/3,true)
        ball = Bitmap.createScaledBitmap(ball,ball.width/3,ball.height/3,true)
        tap = Bitmap.createScaledBitmap(tap,tap.width/3,tap.height/3,true)
        v1 = Bitmap.createScaledBitmap(v1,v1.width/3,v1.height/3,true)
        v2 = Bitmap.createScaledBitmap(v2,v2.width/3,v2.height/3,true)
        center = Bitmap.createScaledBitmap(center,center.width/2,center.height/2,true)
        holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                val canvas = holder.lockCanvas()
                if(canvas!=null) {
                    cx = canvas.width/2f-ball.width/2f
                    cy = canvas.height/5f*3
                    bx = canvas.width/2f-tap.width/2f
                    by = canvas.height/5f*4
                    width1 = canvas.width/2f
                    height1 = (canvas.height-90-v1.height*2)/2f+35f+v1.height
                    bg = Bitmap.createScaledBitmap(bg,canvas.width-50,canvas.height-90-v1.height*2,true)
                    draw(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                music.stop()
                music.release()
                kick.release()
                st.release()
                destroy = true
                updateThread.interrupt()
            }

        })

        updateThread.start()
    }

    var started = false
    var x1 = 0f
    var y1 = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(!paused) {
            when(event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    x1 = event!!.x
                    y1 = event!!.y
                    if(x1>=bx && x1<=bx+tap.width && y1>=by && y1<by+tap.height) {
                        started = true

                    }
                }
                MotionEvent.ACTION_UP -> {
                    started = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if(started) {
                        x1 = event!!.x
                        y1 = event!!.y
                        bx =x1-tap.width/2
                        by =y1-tap.height/2
                        by = max(by,height1)
                        bx = max(bx,35f)
                        bx = min(bx,25f+bg.width-tap.width)
                        by = min(by,35f+v1.height+bg.height-tap.height)
                    }
                }
            }
        }
        return true
    }


    var score = 0
    var width1 = 0f
    val random = Random()
    var deltaX = 0f
    var deltaY = 0f
    val delta = 25f

    var list = mutableListOf<Model>()
    var time = false

    var timer = 60
    var isEnd = false

    val update = Runnable{
        try {
            val canvas = holder.lockCanvas()
            if(!paused) {
                val tx1 = bx+tap.width/2f
                val ty1 = by+tap.height/2f
                val tx2 = cx+ball.width/2f
                val ty2 = cy+ball.height/2f
                if(
                    sqrt((tx1-tx2)*(tx1-tx2)+(ty1-ty2)*(ty1-ty2)) >(tap.width/2f+ball.width/2f)*0.95
                    && sqrt((tx1-tx2)*(tx1-tx2)+(ty1-ty2)*(ty1-ty2))<=(tap.width/2f+ball.width/2f)*1.15
                ) {
                    var angle =
                        Math.toDegrees(Math.atan2((by - cy).toDouble(), (bx - cx).toDouble())).toFloat()
                    if (angle < 0) {
                        angle += 360f
                    }
                    deltaX = (delta+abs(deltaX)/2)* cos(angle)

                    deltaY = (delta+ abs(deltaY)/2)* sin(angle)
                    if(sounds) {
                        kick.seekTo(0)
                        kick.start()
                    }
                }
                cx += deltaX
                cy += deltaY
                if(cx+ball.width+deltaX>=25+bg.width || cx+deltaX<=25f) {
                    deltaX = -deltaX * 0.8f
                    if(sounds) {
                        kick.seekTo(0)
                        kick.start()
                    }
                }
                if(cy+deltaY<=35f+v1.height ||
                    (cy+deltaY+ball.height>=35f+v1.height+bg.height &&
                            (cx+deltaX+ball.width/2f<=canvas.width/2-v2.width/2f || cx+deltaX>canvas.width/2+v2.width/2f-ball.width/3f)
                            )
                ) {
                    deltaY = -deltaY * 0.8f
                    if(sounds) {
                        kick.seekTo(0)
                        kick.start()
                    }
                }
                if(cy>35f+v1.height+bg.height) {
                    deltaX = 0f
                    deltaY = 0f
                    cx = canvas.width/2f-ball.width/2f
                    cy = canvas.height/5f*3
                    if(sounds) {
                        st.seekTo(0)
                        st.start()
                    }
                }
                while(list.size<3) {
                    val ind = random.nextInt(fish.size)
                    if(time && ind==4) continue
                    if(ind==4) {
                        time = true
                    }
                    list.add(Model(25f+random.nextInt(bg.width-fish[0].width),35f+v1.height+random.nextInt(bg.height-fish[0].height),ind))
                }
                var j = 0
                while(j<list.size) {
                    val h = list[j]
                    if(
                        (h.x>=cx && h.x<=cx+ball.width || h.x<=cx && h.x+fish[h.ind].width>=cx)
                        && (h.y>=cy && h.y<=cy+ball.height || h.y<=cy && h.y+fish[h.ind].height>=cy)
                    ) {
                        if(h.ind==4) {
                            timer += 30
                            fish.removeAt(4)
                        } else score++
                        list.removeAt(j)
                    } else j++
                }
            }
            canvas.drawColor(color)
            canvas.drawBitmap(bg,25f,35f+v1.height,paintB)
            canvas.drawBitmap(center,canvas.width/2-center.width/2f,bg.height/2f-center.height/2f+35f+v1.height,paintB)
            canvas.drawBitmap(v1,canvas.width/2-v1.width/2f,25f,paintB)
            canvas.drawBitmap(v2,canvas.width/2-v2.width/2f,canvas.height-v2.height-45f,paintB)
            val tmp = timer-millis/50
            val s = if(paused) "PAUSE" else "${tmp/60}:${String.format("%02d",tmp%60)}"
            if(tmp<=0) isEnd = true
            if(!isEnd) canvas.drawText(s,canvas.width/2f-paintT.measureText(s)/2f,bg.height/2f+55f+v1.height,paintT)
            if(!paused) {
                try {
                    for(i in list) canvas.drawBitmap(fish[i.ind],i.x,i.y,paintB)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                canvas.drawBitmap(tap,bx,by,paintB)
                canvas.drawBitmap(ball,cx,cy,paintB)
            }
            holder.unlockCanvasAndPost(canvas)
              if(isEnd) {
                 paused = true
                //Log.d("TAG","END")
                if(listener!=null) listener!!.end()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

     fun setEndListener(list: EndListener) {
        this.listener = list
    }
    fun togglePause() {
        paused = !paused

    }
   companion object {
        data class Model(var x: Float, var y: Float,var ind: Int)
        interface EndListener {
            fun end();
            fun score(score: Int);
        }

    }
}