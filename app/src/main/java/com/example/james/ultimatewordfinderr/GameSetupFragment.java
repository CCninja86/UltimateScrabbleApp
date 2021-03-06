package com.example.james.ultimatewordfinderr;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialOverlayLayout;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameSetupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameSetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameSetupFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ArrayList<Player> players;
    private PlayerListViewAdapter adapter;
    private ListView playerList;
    private EditText txtPlayerName;

    private static final int ADD_ACTION_POSITION = 1;

    public GameSetupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GameSetupFragment.
     */

    public static GameSetupFragment newInstance(String param1, String param2) {
        GameSetupFragment fragment = new GameSetupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game_setup, container, false);

        final SpeedDialView speedDialView = view.findViewById(R.id.speedDialGameSetup);
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_add_player, R.drawable.player_add_white)
                        .setLabel("Add Player")
                        .setLabelColor(Color.WHITE)
                        .setLabelBackgroundColor(Color.BLACK)
                        .setTheme(R.style.AppTheme)
                        .create()
        );

        SpeedDialOverlayLayout overlayLayout = view.findViewById(R.id.overlayGameSetup);
        speedDialView.setOverlayLayout(overlayLayout);

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.fab_add_player:
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("players", players);
                        AddPlayerFragment addPlayerFragment = new AddPlayerFragment();
                        addPlayerFragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.containerGameSetup, addPlayerFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                        return true;
                    case R.id.fab_remove_players:
                        AlertDialog.Builder alertRemovePlayers = new AlertDialog.Builder(getActivity());
                        alertRemovePlayers.setTitle("Remove selected players?");
                        alertRemovePlayers.setMessage("Are you sure you want to remove the selected players?");

                        alertRemovePlayers.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<Player> selectedPlayers = new ArrayList<>();
                                SparseBooleanArray checkedItems = playerList.getCheckedItemPositions();

                                for (int i = 0; i < playerList.getCount(); i++) {
                                    if (checkedItems.get(i)) {
                                        Player player = (Player) playerList.getItemAtPosition(i);
                                        selectedPlayers.add(player);
                                    }
                                }

                                if (selectedPlayers.size() > 0) {
                                    ListIterator iterator = players.listIterator();

                                    while (iterator.hasNext()) {
                                        Player player = (Player) iterator.next();

                                        if (selectedPlayers.contains(player)) {
                                            iterator.remove();
                                        }
                                    }

                                    adapter.notifyDataSetChanged();

                                    updateFABActionItems(speedDialView);
                                } else {
                                    Toast.makeText(getActivity(), "No players selected", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        alertRemovePlayers.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        alertRemovePlayers.show();

                    default:
                        return false;

                }
            }
        });

        speedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Player List", players);
                Intent intent = new Intent(getActivity(), ScoringTableActivity.class);
                intent.putExtra("Player Bundle", bundle);
                startActivity(intent);

                return false;
            }

            @Override
            public void onToggleChanged(boolean isOpen) {

            }
        });

        if (getArguments() != null) {
            players = (ArrayList<Player>) getArguments().getSerializable("players");
        } else {
            players = new ArrayList<>();
        }

        playerList = view.findViewById(R.id.listPlayer);

        playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.toggleSelected(position);
                adapter.notifyDataSetChanged();
            }
        });

        adapter = new PlayerListViewAdapter(getActivity(), players, R.layout.row_player);
        playerList.setAdapter(adapter);

        updateFABActionItems(speedDialView);

        return view;
    }

    private void updateFABActionItems(SpeedDialView speedDialView) {
        if (playerList.getCount() > 0) {
            speedDialView.addActionItem(
                    new SpeedDialActionItem.Builder(R.id.fab_remove_players, R.drawable.player_minus_white)
                            .setLabel("Remove Players")
                            .setLabelColor(Color.WHITE)
                            .setLabelBackgroundColor(Color.BLACK)
                            .setTheme(R.style.AppTheme)
                            .create(), ADD_ACTION_POSITION
            );
        } else {
            speedDialView.removeActionItemById(R.id.fab_remove_players);
        }
    }

    private ArrayList<String> getPlayerNames(ArrayList<Player> players) {
        ArrayList<String> playerNames = new ArrayList<>();

        for (Player player : players) {
            playerNames.add(player.getName());
        }

        return playerNames;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
