package sauerapps.betterbetterrx.features.SoundCloud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SoundCloud {
//
//    static Gson gson = new GsonBuilder()
//            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
//            .create();

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Config.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final SCService SERVICE = retrofit.create(SCService.class);

    public static SCService getService() {
        return SERVICE;
    }

}