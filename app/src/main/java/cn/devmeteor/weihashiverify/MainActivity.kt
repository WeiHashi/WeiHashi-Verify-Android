package cn.devmeteor.weihashiverify

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.DeviceUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class MainActivity : AppCompatActivity() {

    private lateinit var log: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val uri = intent.data
        if (uri == null) {
            append("非法操作")
            delayFinish()
            return
        }
        val deviceId = uri.getQueryParameter("deviceId")
        if (deviceId == null) {
            append("非法操作")
            delayFinish()
            return
        }
        log = findViewById(R.id.log)
        progressBar = findViewById(R.id.progressBar)
//        var mac: String? = null
//        try {
//            val process = Runtime.getRuntime().exec("su")
//            val os = DataOutputStream(process.outputStream)
//            val ins = process.inputStream
//            os.writeBytes("cat /sys/class/net/wlan0/address")
//            os.flush()
//            os.close()
//            val scanner = Scanner(ins)
//            mac = scanner.nextLine()
//            scanner.close()
//            ins.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        mac = mac ?: DeviceUtils.getMacAddress()
        val model = DeviceUtils.getModel()
        if (model.isNotBlank()) {
            append("获取设备信息成功，正在验证授权...")
            Retrofit.Builder()
                .baseUrl("https://devmeteor.cn:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder().apply {
                        if (BuildConfig.DEBUG) {
                            hostnameVerifier { _, _ -> true }
                            sslSocketFactory(SSLSocketClient.SSLSocketFactory, object :
                                X509TrustManager {
                                override fun checkClientTrusted(
                                    chain: Array<out X509Certificate>?,
                                    authType: String?
                                ) {

                                }

                                override fun checkServerTrusted(
                                    chain: Array<out X509Certificate>?,
                                    authType: String?
                                ) {

                                }

                                override fun getAcceptedIssuers(): Array<X509Certificate> {
                                    return arrayOf()
                                }
                            })
                        }
                    }.build()
                )
                .build()
                .create(Verify::class.java)
                .verify(deviceId, model)
                .enqueue(object : Callback<Response> {
                    override fun onResponse(
                        call: Call<Response>,
                        response: retrofit2.Response<Response>
                    ) {
                        val res = response.body()
                        if (res==null){
                            append("请求服务器失败，请检查网络连接")
                            delayFinish()
                            return
                        }
                        append(res.msg)
                        progressBar.visibility = View.GONE
                        delayFinish()
                    }

                    override fun onFailure(call: Call<Response>, t: Throwable) {
                        println(t.message)
                        append("请求服务器失败，请检查网络连接")
                        progressBar.visibility = View.GONE
                        delayFinish()
                    }
                })
        } else {
            append("获取设备信息失败，请重试")
            progressBar.visibility = View.GONE
            delayFinish()
        }
    }

    private fun append(content: String) {
        log.append("\n$content")
    }

    private fun delayFinish() {
        append("5秒后自动关闭")
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 5000)
    }
}
