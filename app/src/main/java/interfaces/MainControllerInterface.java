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

public interface MainControllerInterface {
    void setCountdown(int value);
    void setGroup(int groupValue);
    void setEnd(int end, int maxEnds);
    void setBackground(int r, int g, int b);
    void setCommEnabled(boolean enable);
    void showError(String message);
    void setTimer(int hours, int minutes, int seconds);
}
