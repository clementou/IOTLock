package com.clement.iotlock.ssh;

import android.os.AsyncTask;

public class CloseTask extends AsyncTask <Void, Void, Void> {
    protected Void doInBackground(Void... command){
        System.out.println("closed");
        String user = "pi";
        String host = "10.244.1.180";
        String password = "raspberry";
        String knownHosts = "android_asset/known_hosts";
        int port = 22;
        SSHManager sshManager = new SSHManager(user, password, host, knownHosts);
        String errorMessage = sshManager.connect();
        if (errorMessage != null) System.out.println(errorMessage);
        String result = sshManager.sendCommand("python close.py");
        sshManager.close();
        System.out.println(result);
        return null;
    }
}