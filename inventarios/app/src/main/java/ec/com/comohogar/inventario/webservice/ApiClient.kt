package ec.com.comohogar.inventario.webservice

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {


    var BASE_URL_INTERNET:String="http://200.105.234.42:8080/"
    var BASE_URL_WIFI:String="http://192.168.4.75:8080/"
    var BASE_URL:String="http://192.168.4.75:8080/"
    val getClient: APIService

        get() {

            return getRetrofit.create(APIService::class.java)

        }


    val getRetrofit: Retrofit

        get() {

            val gson = GsonBuilder()
                .setLenient()
                .create()
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder().addInterceptor(interceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit

        }

}