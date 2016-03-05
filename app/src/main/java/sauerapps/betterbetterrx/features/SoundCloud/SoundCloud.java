package sauerapps.betterbetterrx.features.soundcloud;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SoundCloud {

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Config.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final SCService SERVICE = retrofit.create(SCService.class);

    public static SCService getService() {
        return SERVICE;
    }

}