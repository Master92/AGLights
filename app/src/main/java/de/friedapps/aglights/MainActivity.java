package de.friedapps.aglights;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

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

public class MainActivity extends AppCompatActivity {
    private TextView countDownText, groupText, endsText;
    private LinearLayout labelLayout;
    private Communicator comm;
    private View curView;
    private final Handler myHandler = new Handler(Looper.getMainLooper());
    private Thread readerThread;
    private Bundle states;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            states = savedInstanceState.getBundle("states");

        setContentView(R.layout.activity_main);
        countDownText = (TextView) findViewById(R.id.countDownText);
        groupText = (TextView) findViewById(R.id.groupText);
        endsText = (TextView) findViewById(R.id.endsText);
        labelLayout = (LinearLayout) findViewById(R.id.labelLayout);

        final MainControllerInterface controller = new Controller(this);
        comm = new Communicator(controller);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(states == null)
            states = new Bundle();

        states.putInt("amountOfCommunicators", comm.getAmountOfSockets());
        comm.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(states == null)
            return;
        int amount = states.getInt("amountOfCommunicators", 0);
        startConnecting(amount);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        states = savedInstanceState.getBundle("states");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle("states", states);
        super.onSaveInstanceState(outState);
    }

    public void onButtonDisconnect(View v) {
        comm.disconnect();
    }

    public void onButtonConnect(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inf = LayoutInflater.from(this);
        View dialogView = inf.inflate(R.layout.dialog_connect, null);
        final NumberPicker np = (NumberPicker) dialogView.findViewById(R.id.amountPicker);
        np.setMinValue(1);
        np.setMaxValue(4);
        np.setWrapSelectorWheel(false);

        builder
                .setTitle(R.string.connect_title)
                .setView(dialogView)

                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startConnecting(np.getValue());
                    }
                })

                .setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog d = builder.create();
        d.show();
    }

    public void onAddEnd(View v) {
        comm.sendMessage(aiprotocol.ADD_END);
    }

    public void onDelEnd(View v) {
        comm.sendMessage(aiprotocol.DEL_END);
    }

    public void onNext(View v) {
        comm.sendMessage(aiprotocol.NEXT);
    }

    public void onTime(View v) {
        comm.sendMessage(aiprotocol.TIME);
    }

    public void onTimer(View v) {
        LayoutInflater layout = getLayoutInflater();
        View view = layout.inflate(R.layout.dialog_timer, null);
        final NumberPicker hourPicker, minutePicker, secondPicker;
        hourPicker = (NumberPicker) view.findViewById(R.id.hoursPicker);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker = (NumberPicker) view.findViewById(R.id.minutesPicker);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        secondPicker = (NumberPicker) view.findViewById(R.id.secondsPicker);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setTitle(R.string.timer_title)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder sb = new StringBuilder();
                        sb.append((char)hourPicker.getValue());
                        sb.append((char)minutePicker.getValue());
                        sb.append((char)secondPicker.getValue());

                        comm.sendMessage(aiprotocol.TIMER, sb.toString());
                    }
                })
                .setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onNewRound(View v) {
        LayoutInflater layout = getLayoutInflater();
        curView = layout.inflate(R.layout.dialog_new_round, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(R.string.new_round_title)
                .setView(curView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText ends = (EditText)curView.findViewById(R.id.endsEditText);
                        EditText preparation = (EditText)curView.findViewById(R.id.preparationEditText);
                        EditText duration = (EditText)curView.findViewById(R.id.durationEditText);
                        CheckBox group = (CheckBox)curView.findViewById(R.id.groupsCheckbox);

                        StringBuilder sb = new StringBuilder();
                        int iEnds = Integer.parseInt(ends.getText().toString());
                        int iPrep = Integer.parseInt(preparation.getText().toString());
                        int iDur = Integer.parseInt(duration.getText().toString());
                        int du1 = iDur / 10;
                        int du2 = iDur - du1*10;
                        sb.append((char)iEnds).append((char)iPrep).append((char)du1).append((char)du2);
                        if(group.isChecked())
                            sb.append('1');
                        else
                            sb.append('0');

                        comm.sendMessage(aiprotocol.NEW_ROUND, sb.toString());
                    }
                })
                .setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onRuntextSend(View v) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_runtext, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText message = (EditText) view.findViewById(R.id.runtextEditText);

        builder
                .setTitle(R.string.runtext_title)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        comm.sendMessage(aiprotocol.RUNTEXT, message.getText().toString());
                    }
                })
                .setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }



    private void commEnabled(final boolean enabled) {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.disconnectButton).setEnabled(enabled);
                findViewById(R.id.connectButton).setEnabled(!enabled);
                findViewById(R.id.addEndButton).setEnabled(enabled);
                findViewById(R.id.delEndButton).setEnabled(enabled);
                findViewById(R.id.nextButton).setEnabled(enabled);
                findViewById(R.id.timeButton).setEnabled(enabled);
                findViewById(R.id.timerButton).setEnabled(enabled);
                findViewById(R.id.runtextButton).setEnabled(enabled);
                findViewById(R.id.newRoundButton).setEnabled(enabled);

                if(enabled) {
                    readerThread = new Thread(comm);
                    readerThread.start();
                } else if(readerThread != null) readerThread.interrupt();
            }
        });
    }

    public void startConnecting(final int amountOfDisplays) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                comm.connect(amountOfDisplays);
            }
        });
        t.start();
    }


    private class Controller implements MainControllerInterface {
        private final MainActivity main;

        public Controller(MainActivity main) {
            this.main = main;
        }

        @Override
        public void setCountdown(final int value) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    main.countDownText.setText(String.format(Locale.GERMANY, "%d", value));
                }
            });
        }

        @Override
        public void setGroup(int groupValue) {
            final CharSequence text = (groupValue == 0) ? "A/B" : "C/D";
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    main.groupText.setText(text);
                }
            });
        }

        @Override
        public void setEnd(int end, int maxEnds) {
            final CharSequence text = getString(R.string.main_end_first) + end + getString(R.string.main_end_second) + maxEnds;
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    endsText.setText(text);
                }
            });
        }

        @Override
        public void setBackground(final int r, final int g, final int b) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    labelLayout.setBackgroundColor(Color.rgb(r, g, b));
                }
            });
        }

        @Override
        public void setCommEnabled(boolean enable) {
            commEnabled(enable);
        }

        @Override
        public void showError(final String message) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "***ERROR*** " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void setTimer(final int hours, final int minutes, final int seconds) {
            myHandler.post(new Runnable() {

                @Override
                public void run() {
                    countDownText.setText(hours + ":" + minutes + ":" + seconds);
                    String s = (hours > 0 || minutes > 0 || seconds > 0) ? "Timer läuft..." : "Timer beendet";
                    groupText.setText(s);
                    endsText.setText("");
                }
            });
        }
    }

    public void onRadioChanged(View v) {
        EditText ends = (EditText) curView.findViewById(R.id.endsEditText);
        EditText preparation = (EditText) curView.findViewById(R.id.preparationEditText);
        EditText duration = (EditText) curView.findViewById(R.id.durationEditText);
        CheckBox groups = (CheckBox) curView.findViewById(R.id.groupsCheckbox);

        switch(v.getId()) {
            case R.id.indoorRadioButton:
                ends.setText("10");
                preparation.setText("10");
                duration.setText("120");
                groups.setChecked(true);
                customEndsEnabled(false);
                break;

            case R.id.outdoorRadioButton:
                ends.setText("6");
                preparation.setText("10");
                duration.setText("240");
                groups.setChecked(true);
                customEndsEnabled(false);
                break;

            case R.id.ligaRadioButton:
                ends.setText("3");
                preparation.setText("10");
                duration.setText("120");
                groups.setChecked(false);
                customEndsEnabled(false);
                break;

            case R.id.customRadioButton:
                customEndsEnabled(true);
                break;
        }
    }

    private void customEndsEnabled(boolean enable) {
        if(curView.getId() == R.id.newRoundDialog) {
            curView.findViewById(R.id.endsEditText).setEnabled(enable);
            curView.findViewById(R.id.preparationEditText).setEnabled(enable);
            curView.findViewById(R.id.durationEditText).setEnabled(enable);
            curView.findViewById(R.id.groupsCheckbox).setEnabled(enable);
        }
    }
}
