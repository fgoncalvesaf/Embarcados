package com.example.goncalves.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static int ENABLE_BLUETOOTH = 1;
    ArrayAdapter<String> disp;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ConnectThread connect;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        disp = new ArrayAdapter<String>(getActivity(),
                R.layout.avaliable_devices,
                R.id.list_item_avaliable_devices,
                new ArrayList<String>());

        //verificar se o dispositivo possui bluetooth
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        //habilitar bluetooth
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH);
        }

        //listar dispositivos que ja foram pareados
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                disp.add(device.getName() + "\n" + device.getAddress());
            }
        }

        //deixar visivel
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_bluetooth);

        listView.setAdapter(disp);


        //clicar no dispositivo
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String itemData = disp.getItem(position);
                String devAddress = itemData.substring(itemData.indexOf("\n") + 1, itemData.length());
                String devName = itemData.substring(0, itemData.indexOf("\n"));
                devName = "Connected to " + devName;
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(devAddress);
                connect = new ConnectThread(device);
                connect.start();
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(getActivity(), devName, duration).show();
                Intent intent = new Intent(getActivity(), APP.class);
                intent.putExtra(Intent.EXTRA_TEXT, devAddress);
                startActivity(intent);

            }
        });

        return rootView;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        InputStream mmInStream;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            InputStream tmpIn = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;

            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;

        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            byte[] bf;
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    //---bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    // Read from the InputStream
                    // Send the obtained bytes to the UI Activity
                    buffer[bytes] = (byte) mmInStream.read();
                    if (buffer[bytes] == '#') {
                        toAPP(Arrays.copyOfRange(buffer, 0, bytes));
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                    //toAPP(Arrays.copyOfRange(buffer, 0, bytes));
                } catch (IOException e) {
                    break;
                }
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void toAPP(byte[] data) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        message.setData(bundle);
        APP.handler.sendMessage(message);
    }
}
