package sauerapps.sauermeditation.features.journal.journalList;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;

import sauerapps.sauermeditation.R;

/**
 * Created by andrewsauer on 3/8/16.
 */
public class JournalListAdapter extends FirebaseListAdapter<JournalList> {
    private String mEncodedEmail;


    public JournalListAdapter(Activity activity, Class<JournalList> modelClass, int modelLayout,
                              Query ref, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mEncodedEmail = encodedEmail;
        this.mActivity = activity;
    }

    @Override
    protected void populateView(View view, JournalList list) {
        TextView textViewListName = (TextView) view.findViewById(R.id.text_view_list_name);
        textViewListName.setText(list.getEntryTitle());
        }
    }

