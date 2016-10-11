package de.friedapps.aglights;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import interfaces.MainControllerInterface;
import interfaces.aiprotocol;

/**
 * AGLights - Copyright (C) 2016 Nils Friedchen <Nils.Friedchen@googlemail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * or visit https://www.gnu.org/licenses/gpl-2.0.txt
 */

public class Communicator implements Runnable {
    private MainControllerInterface controller;
    private Handler myHandler;
    private Socket[] socket;
    private BufferedWriter[] writers;
    private InputStreamReader reader;
    private int amountOfSockets;

    public Communicator(MainControllerInterface controller) {
        this.controller = controller;
        socket = new Socket[4];
        writers = new BufferedWriter[4];
    }

    public void connect(int amountOfDisplays) {
        if(amountOfDisplays < 1)
            return;


        try {
            for (int i = 0; i < amountOfDisplays; i++) {
                String address = "192.168.0.1" + i;

                socket[i] = new Connector().execute(address).get();
                writers[i] = new BufferedWriter(new OutputStreamWriter(socket[i].getOutputStream()));
                reader = new InputStreamReader(socket[0].getInputStream());
            }
            sync();
            controller.setCommEnabled(true);
            amountOfSockets = amountOfDisplays;
        } catch (IOException | InterruptedException | ExecutionException e) {
            controller.showError(e.getLocalizedMessage());
        }
    }

    public void disconnect() {
        try {
            controller.setCommEnabled(false);
            for (Socket s : socket)
                if(s != null)
                    s.close();

        } catch (IOException e) {
            controller.showError(e.getLocalizedMessage());
        }
        amountOfSockets = 0;
    }

    private  void sync() {
        Calendar now = Calendar.getInstance();
        StringBuilder date = new StringBuilder();
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);

        date.append(now.get(Calendar.YEAR));
        date.append((month < 10) ? "0" + month : month);
        date.append((day < 10) ? "0" + day : day);
        date.append((hour < 10) ? "0" + hour : hour);
        date.append((minute < 10) ? "0" + minute : minute);
        date.append((second < 10) ? "0" + second : second);

        Log.d("date", date.toString());
        sendMessage(aiprotocol.SYNC, date.toString());
    }

    public void sendMessage(byte command) {
        char[] c = new char[3];
        c[0] = 3;
        c[1] = ((char) command);
        c[2] = 127;

        send(c);
    }

    public void sendMessage(byte command, String extra) {
        int l = extra.length() + 3;
        char[] c = new char[l];
        c[0] = (char)l;
        c[1] = (char)command;
        for(int i = 0; i < extra.length(); i++) {
            c[i+2] = extra.charAt(i);
        }
        c[l-1] = 127;

        send(c);
    }
    
    private void send(char[] ca) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < ca[0]; i++) {
            sb.append((int)ca[i]);
        }

        for(int i = 0; i < amountOfSockets; i++) {
            Log.d("Communicator.send", sb.toString());
            try {
                    writers[i].write(ca);
                    writers[i].flush();
            } catch(IOException e) {
                controller.showError(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void run() {
        boolean keepRunning = true;

        while(keepRunning) {
            try {
                char buffer[] = new char[7];
                reader.read(buffer, 0, 7);
                switch(buffer[0]) {
                    case aiprotocol.UPDATE:
                        int timer = buffer[1] * 10 + buffer[2];
                        int group = buffer[4];
                        int curEnd = buffer[5] + 1;
                        int maxEnds = buffer[6];

                        controller.setCountdown(timer);
                        controller.setGroup(group);
                        controller.setEnd(curEnd, maxEnds);
                        if (buffer[3] == aiprotocol.COLOR_GREEN)
                            controller.setBackground(0, 255, 0);
                        else if (buffer[3] == aiprotocol.COLOR_YELLOW)
                            controller.setBackground(255, 255, 0);
                        else
                            controller.setBackground(255, 0, 0);
                        break;

                    case aiprotocol.TIMER_UPDATE:
                        int hours = buffer[1] % 127;
                        int minutes = buffer[2] % 127;
                        int seconds = buffer[3] % 127;
                        controller.setTimer(hours, minutes, seconds);
                        break;

                    default:
                        controller.showError("Some strange update was sent from the display: " + buffer);
                        break;
                }

                Thread.sleep(250);
            } catch (InterruptedException e) {
                keepRunning = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getAmountOfSockets() {
        return amountOfSockets;
    }
}
