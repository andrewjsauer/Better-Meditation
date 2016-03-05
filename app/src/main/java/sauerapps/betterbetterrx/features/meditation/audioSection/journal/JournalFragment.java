package sauerapps.betterbetterrx.features.meditation.audioSection.journal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.R;

/**
 * Created by andrewsauer on 3/4/16.
 */
public class JournalFragment extends Fragment {

    @Bind(R.id.journal_exit_button)
    protected ImageButton mExitButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.journal_fragment, container, false);
        ButterKnife.bind(this, view);

        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }



}
