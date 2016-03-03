package sauerapps.betterbetterrx.features.SoundCloud;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SCService {

    @GET("users/209482111/tracks?client_id=" + Config.CLIENT_ID)
    Call<List<Track>> getRecentTracks();


//    public void getRecentTracks(@Query("created_at[from]") String date, Callback<List<Track>> cb);

}