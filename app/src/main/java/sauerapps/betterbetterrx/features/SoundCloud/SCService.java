package sauerapps.betterbetterrx.features.soundcloud;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SCService {

    @GET("users/209482111/tracks?client_id=" + Config.CLIENT_ID)
    Call<List<Track>> getRecentTracks();
}