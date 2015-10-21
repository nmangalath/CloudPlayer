package com.nmangalath.cloudplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by Narayanan on 26/07/2015.
 */
public class RagasFragment extends Fragment {

    private ArrayAdapter<String> listAdapter ;
    DataStore ds;
    ViewGroup view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final DataStore cds = ((MainActivity)getActivity()).ds;

        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_ragas, container, false);
        ListView ragasView = (ListView) view.findViewById(R.id.id_ragas);
        listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.ragas_list_item, R.id.ragas_item, cds.ragas);
        ragasView.setAdapter(listAdapter);

        Log.i("RagaFrag", "OncreateView Entered");
        cds.prev_list.clear();
        cds.prev_list.addAll(cds.playlist);
        cds.prev_checked.clear();
        cds.prev_checked.addAll(cds.checked_positions);

        EditText raga_search = (EditText) view.findViewById(R.id.raga_search);
        raga_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                listAdapter.getFilter().filter(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ragasView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!cds.player_state.equals("stopped") && !cds.player_state.equals("null")){
                    Toast.makeText(getActivity(), "Please stop music first before changing the playlist", Toast.LENGTH_LONG).show();
                    return;
                }
                DataStore cds = ((MainActivity)getActivity()).ds;
                cds.play_list_type = "raga";
                cds.raga_selected = cds.ragas.get(position);
                Log.i("CP:ragaF",cds.raga_selected);
                Log.i("CP:RagaF", cds.play_list_type);
                getActivity().getActionBar().setSelectedNavigationItem(0);
                ((MainActivity)getActivity()).mAdapter.notifyDataSetChanged();

            }
        });

        return view;
    }

//    EditText raga_search = (EditText) view.findViewById(R.id.raga_search);


}
