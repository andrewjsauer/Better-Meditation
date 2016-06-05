package sauerapps.sauermeditation.features.meditation.soundcloud;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SCService {

    @GET("users/209482111/playlists?client_id=" + Config.CLIENT_ID)
    Call<List<Playlists>> getPlaylists();

    @GET("playlists/{playlistId}?client_id=" + Config.CLIENT_ID)
    Call<Tracks> getRecentTracks(
            @Path("playlistId") int playlistId
    );
}