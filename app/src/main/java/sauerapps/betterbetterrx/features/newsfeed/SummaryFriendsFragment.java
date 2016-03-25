package sauerapps.betterbetterrx.features.newsfeed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

import butterknife.ButterKnife;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.meditation.audioSection.AudioList;
import sauerapps.betterbetterrx.utils.Constants;

public class SummaryFriendsFragment extends Fragment {

    private String mEncodedEmail;

    private Firebase mRef;


    private RecyclerView mRecyclerView;
    private FirebaseRecyclerViewAdapter<AudioList, AudioListHolder> mRecycleViewAdapter;


    public SummaryFriendsFragment() {
        /* Required empty public constructor */
    }

    public static SummaryFriendsFragment newInstance(String encodedEmail) {
        SummaryFriendsFragment fragment = new SummaryFriendsFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
        }

        mRef = new Firebase(Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS_SHARED_WITH).child(mEncodedEmail);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_summary, container, false);
        ButterKnife.bind(this, view);

        initializeScreen(view);

        return view;
    }

    private void initializeScreen(View view) {

        mRecyclerView = (RecyclerView) view.findViewById(R.id.friends_audio_list);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setReverseLayout(false);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(manager);


        mRecycleViewAdapter = new FirebaseRecyclerViewAdapter<AudioList, AudioListHolder>(AudioList.class, R.layout.audio_list_item, AudioListHolder.class, mRef) {
            @Override
            protected void populateViewHolder(AudioListHolder viewHolder, AudioList model, int position) {
                super.populateViewHolder(viewHolder, model, position);
            }
        };


//        mRecycleViewAdapter = new FirebaseRecyclerAdapter<AudioList, AudioListHolder>(AudioList.class, R.layout.message, AudioListHolder.class, mRef) {
//            @Override
//            public void populateViewHolder(AudioListHolder audioView, AudioList audioList, int position) {
//                chatView.setName(chat.getName());
//                chatView.setText(chat.getText());
//
//                if (getAuth() != null && chat.getUid().equals(getAuth().getUid())) {
//                    chatView.setIsSender(true);
//                } else {
//                    chatView.setIsSender(false);
//                }
//            }
//        };

        mRecyclerView.setAdapter(mRecycleViewAdapter);
    }
}
