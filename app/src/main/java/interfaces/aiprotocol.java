package interfaces;

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

public interface aiprotocol {
    int PORT = 13337;

    byte NEXT = 1;
    byte UPDATE = 2;
    byte TIMER_UPDATE = 3;

    byte COLOR_RED = 100;
    byte COLOR_YELLOW = 101;
    byte COLOR_GREEN = 102;

    byte NEW_ROUND = 10;
    byte RUNTEXT = 11;
    byte TIMER = 12;
    byte SYNC = 13;
    byte TIME = 14;
    byte ADD_END = 15;
    byte DEL_END = 16;
}
