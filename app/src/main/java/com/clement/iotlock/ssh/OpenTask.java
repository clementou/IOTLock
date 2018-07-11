package com.clement.iotlock.ssh;

import android.os.AsyncTask;

public class OpenTask extends AsyncTask <Void, Void, Void> {
    protected Void doInBackground(Void... command){
        System.out.println("opened");
        String user = "pi";
        String host = "10.244.1.180";
        String password = "raspberry";
        String knownHosts = "android_asset/known_hosts";
        int port = 22;
        SSHManager sshManager = new SSHManager(user, password, host, knownHosts);
        String errorMessage = sshManager.connect();
        if (errorMessage != null) System.out.println(errorMessage);
        String result = sshManager.sendCommand("python open.py");
        sshManager.close();
        System.out.println(result);
        return null;
    }
}