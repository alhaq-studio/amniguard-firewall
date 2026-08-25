package com.alhaq.amnshield.netblock;

/*
    This file is part of AmniGuard Firewall.

    AmniGuard Firewall is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AmniGuard Firewall is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AmniGuard Firewall.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015-2025 by Marcel Bokhorst (M66B)
*/

public class Allowed {
    public String raddr;
    public int rport;

    public Allowed() {
        this.raddr = null;
        this.rport = 0;
    }

    public Allowed(String raddr, int rport) {
        this.raddr = raddr;
        this.rport = rport;
    }
}
