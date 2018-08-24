package mobi.acpm.inspeckage.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.log.LogService;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.webserver.InspeckageService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConfigFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigFragment extends Fragment {
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
    public ConfigFragment(Activity act) {
        // Required empty public constructor
        mainActivity = act;
        context = mainActivity.getApplicationContext();
    }

    public ConfigFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConfigFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfigFragment newInstance(String param1, String param2) {
        ConfigFragment fragment = new ConfigFragment();
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

        final View view = inflater.inflate(R.layout.fragment_config, container, false);

        String h = mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces");
        RadioGroup radioGroup = new RadioGroup(view.getContext());
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        final String[] address = mPrefs.getString(Config.SP_SERVER_INTERFACES, "--").split(",");;
        for (int i = 0; i < address.length; i++) {
            RadioButton rdbtn = new RadioButton(view.getContext());
            rdbtn.setId(i);
            rdbtn.setText(address[i]);
            if (h.equals(address[i])) {
                rdbtn.setChecked(true);
            }
            radioGroup.addView(rdbtn);
        }
        ((ViewGroup) view.findViewById(R.id.radiogroup)).addView(radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                View radioButton = radioGroup.findViewById(i);
                int index = radioGroup.indexOfChild(radioButton);
                String host = address[index];
                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putString(Config.SP_SERVER_HOST, host);
                edit.apply();
            }
        });

        TextView txtPort = (TextView) view.findViewById(R.id.txtPort);
        txtPort.setText(String.valueOf(mPrefs.getInt(Config.SP_SERVER_PORT, 8008)));

        TextView txtWSPort = (TextView) view.findViewById(R.id.txtWSPort);
        txtWSPort.setText(String.valueOf(mPrefs.getInt(Config.SP_WSOCKET_PORT, 8887)));

        final Button button = (Button) view.findViewById(R.id.btnNewPort);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView txtPort = (TextView) view.findViewById(R.id.txtPort);

                stopService();

                String host = null;
                if(!mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces").equals("All interfaces")){
                    host = mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces");
                }
                startService(host,Integer.parseInt(txtPort.getText().toString()));

                TextView txtWSPort = (TextView) view.findViewById(R.id.txtWSPort);

                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putInt(Config.SP_SERVER_PORT, Integer.valueOf(txtPort.getText().toString()));
                edit.putInt(Config.SP_WSOCKET_PORT, Integer.valueOf(txtWSPort.getText().toString()));
                edit.apply();
            }
        });


        // Inflate the layout for this fragment
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
            //at the moment OnFragmentInteractionListener is not necessary
            //throw new RuntimeException(context.toString()
            //        + " must implement OnFragmentInteractionListener");
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
