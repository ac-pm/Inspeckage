package mobi.acpm.inspeckage.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.log.LogService;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.webserver.InspeckageService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AuthFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AuthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context context;
    private Activity mainActivity;
    private SharedPreferences mPrefs;

    private OnFragmentInteractionListener mListener;

    @SuppressLint("ValidFragment")
    public AuthFragment(Activity act) {
        mainActivity = act;
        context = mainActivity.getApplicationContext();
    }

    public AuthFragment() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AuthFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AuthFragment newInstance(String param1, String param2) {
        AuthFragment fragment = new AuthFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = context.getSharedPreferences(Module.PREFS, context.MODE_PRIVATE);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_auth, container, false);

        TextView txtLogin = (TextView) view.findViewById(R.id.txtLogin);
        TextView txtPass = (TextView) view.findViewById(R.id.txtPass);
        final Switch mSwitch = (Switch) view.findViewById(R.id.auth_switch);

        String login = mPrefs.getString(Config.SP_USER_PASS, "");
        if(!login.trim().equals("")) {
            txtLogin.setText(login.split(":")[0]);
            txtPass.setText(login.split(":")[1]);
        }

        final Boolean sw = mPrefs.getBoolean(Config.SP_SWITCH_AUTH, false);
        mSwitch.setChecked(sw);

        txtLogin.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                mSwitch.setChecked(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtPass.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                mSwitch.setChecked(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences.Editor edit = mPrefs.edit();

                if (isChecked) {
                    edit.putBoolean(Config.SP_SWITCH_AUTH, true);
                } else {
                    edit.putBoolean(Config.SP_SWITCH_AUTH, false);
                }

                TextView txtLogin = (TextView) view.findViewById(R.id.txtLogin);
                TextView txtPass = (TextView) view.findViewById(R.id.txtPass);

                edit.putString(Config.SP_USER_PASS, txtLogin.getText()+":"+txtPass.getText());
                edit.apply();

                stopService();

                String host = null;
                int port = mPrefs.getInt(Config.SP_SERVER_PORT, 8008);
                if(!mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces").equals("All interfaces")){
                    host = mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces");
                }
                startService(host,port);
            }
        });

        return view;

    }

    // TODO: Rename method, update argument and hook method into UI event
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
            //throw new RuntimeException(context.toString()
             //       + " must implement OnFragmentInteractionListener");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void startService(String host, int port) {
        Intent i = new Intent(context, InspeckageService.class);
        i.putExtra("port", port);
        i.putExtra("host", host);

        context.startService(i);
    }

    public void stopService() {
        context.stopService(new Intent(context, InspeckageService.class));
        context.stopService(new Intent(context, LogService.class));
    }
}
