#!/bin/sh
# In case of Unix-like OS, run it with xterm\
test "$Debug" = "" && exec wish $0 || exec xterm -e wish $0

# Copyright 2019 Martin Conrad
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if {[catch {package require udp}] != 0} {
    if [catch {load [glob udp*.dll]}] {
        tk_messageBox -icon error -message "UDP extension not present, cannot start UDP device simulator"
    }
}

# GUI
wm title . "UDPDevice Simulator"
pack [ttk::labelframe .co -text Setup] -fill both -side left
pack [ttk::labelframe .co.port -text "Port"] -fill y -side left
pack [ttk::entry .co.port.e -textvariable Port -width 5] -fill both -side left
if {[catch {console hide}] == 0} {
    pack [ttk::button .co.debug -text "Debug On" -command debug] -fill y -side left
}
pack [ttk::button .co.startstop -text "Start" -command startstop] -expand 1 -side left -fill both
pack [ttk::labelframe .dev -text "Device Status"] -fill both -side left
pack [ttk::radiobutton .dev.closed -text Closed -variable DrawerState -value 0] -expand 1 -side left
pack [ttk::radiobutton .dev.open -text Opened -variable DrawerState -value 1] -expand 1 -side left

# Procedure debug: Depending on whether debug has to be turned on or off, shows or hides console
proc debug {} {
	if [string equal [.co.debug cget -text] "Debug On"] {
		.co.debug configure -text "Debug Off"
		console show
		raise .
	} {
		.co.debug configure -text "Debug On"
		console hide
	}
}

# Procedure startStop: Depending on whether socket has been opened, the socket will be closed or a new socket
# will be created and bound to a message handler
proc startstop {} {
    global Socket Port

    if {$Socket == ""} {
        set Socket [udp_open $Port]
        fconfigure $Socket -buffering none -translation binary
        fileevent $Socket readable messageHandler
        puts "Listening for messages on UDP port [fconfigure $Socket -myport]"
        .co.startstop configure -text Stop
    } {
        fileevent $Socket readable ""
        puts "Stop message handling on UDP port [fconfigure $Socket -myport]"
        close $Socket
        set Socket ""
        .co.startstop configure -text Start
    }
}

# Prodecure messageHandler: Reads message from a socket and sends back the current drawer state in case of a valid
# command. Otherwise, the message will be ignored.
# Valid commands are "O" (command to open the drawer) and "S" (command to retrieve the current drawer state)
# Drawer state is either "0" (closed) or "1" (opened).
proc messageHandler {} {
    global Socket DrawerState

    set frame [read $Socket]
    fconfigure $Socket -remote [set peer [fconfigure $Socket -peer]]
    puts -nonewline "Got >$frame< from $peer, "
    if {$frame == "S"} {
        puts -nonewline $Socket $DrawerState
        puts " put $DrawerState"
    } elseif {$frame == "O"} {
        set DrawerState 1
        puts -nonewline $Socket $DrawerState
        puts " put $DrawerState"
    } {
        puts "ignore invalid command"
    }
}

set DrawerState 0
set Port 23765
set Socket ""

