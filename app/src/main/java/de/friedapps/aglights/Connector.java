package de.friedapps.aglights;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
public class Connector extends AsyncTask<String, Void, Socket> {

    @Override
    protected Socket doInBackground(String... url) {
        Socket s = null;

        try {
            s = new Socket();
            s.connect(new InetSocketAddress(url[0], aiprotocol.PORT), 1000);
        } catch (IOException e) {}

        return s;
    }
}
