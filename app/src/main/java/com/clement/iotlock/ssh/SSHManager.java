package com.clement.iotlock.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHManager{
    private static final Logger LOGGER = Logger.getLogger(SSHManager.class.getName());
    private JSch jschChannel;
    private Session session;
    private String user, host, password;
    private int port, timeout;

    private void Construct(String user, String password, String host, String knownHosts) {
        JSch.setConfig("StrictHostKeyChecking", "no");
        try {
            if (null != knownHosts && new File(knownHosts).exists()){
                jschChannel.setKnownHosts(knownHosts);
            }
        }
        catch(JSchException jschX) {
            logError(jschX.getMessage());
        }

        jschChannel = new JSch();
        this.user = user;
        this.password = password;
        this.host = host;
    }

    SSHManager(String user, String password, String host, String knownHosts) {
        Construct(user, password, host, knownHosts);
        port = 22;
        timeout = 60000;
    }

    public String connect() {
        String errorMessage = null;
        try {
            session = jschChannel.getSession(user, host, port);
            session.setPassword(password);
            // UNCOMMENT THIS FOR TESTING PURPOSES, BUT DO NOT USE IN PRODUCTION
            // sesConnection.setConfig("StrictHostKeyChecking", "no");
            session.connect(timeout);
            System.out.println("connected");
        }
        catch(JSchException jschX) {
            errorMessage = jschX.getMessage();
        }
        return errorMessage;
    }

    private String logError(String errorMessage) {
        if(errorMessage != null) {
            LOGGER.log(Level.SEVERE, "{0}:{1} - {2}",
                    new Object[]{host, port, errorMessage});
        }
        return errorMessage;
    }

    private String logWarning(String warnMessage) {
        if(warnMessage != null) {
            LOGGER.log(Level.WARNING, "{0}:{1} - {2}",
                    new Object[]{host, port, warnMessage});
        }
        return warnMessage;
    }
    public String sendCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();
        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            InputStream commandOutput = channel.getInputStream();
            channel.connect();
            int readByte = commandOutput.read();

            while(readByte != 0xffffffff) {
                outputBuffer.append((char)readByte);
                readByte = commandOutput.read();
            }

            channel.disconnect();
        }
        catch(IOException | JSchException ioX) {
            logWarning(ioX.getMessage());
            return null;
        }

        return outputBuffer.toString();
    }

    public void close() {
        session.disconnect();
    }
}
