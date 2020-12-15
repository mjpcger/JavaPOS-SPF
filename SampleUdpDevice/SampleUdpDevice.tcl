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

# The setup part
set Setup .all.row1.co
pack [ttk::frame .all] -expand 1 -fill both
pack [ttk::frame .all.row1] -expand 1 -fill both
pack [ttk::labelframe $Setup -text Setup] -fill both -side left
pack [ttk::labelframe $Setup.port -text "Port"] -fill y -side left
pack [ttk::entry $Setup.port.e -textvariable Port -width 5] -fill both -side left
if {[catch {console hide}] == 0} {
    pack [ttk::button $Setup.debug -text "Debug On" -command debug] -fill y -side left
}
pack [ttk::button $Setup.startstop -text "Start" -command startstop] -expand 1 -side left -fill both

# The Drawer part
set Drawer .all.row1.drw
pack [ttk::labelframe $Drawer -text "Drawer Status"] -expand 1 -fill both -side left
pack [ttk::radiobutton $Drawer.off -text Closed -variable DrawerState -value 0] -expand 1 -side left
pack [ttk::radiobutton $Drawer.slow -text Opened -variable DrawerState -value 1] -expand 1 -side left

# The Belt part
set Belt .all.row2
pack [ttk::labelframe $Belt -text Belt] -expand 1 -fill both
pack [ttk::labelframe $Belt.status -text "Belt Status"] -expand 1 -fill both
pack [ttk::label $Belt.status.label -text Stopped -background red -anchor center] -expand 1 -fill both
pack [ttk::frame $Belt.status.fields] -expand 1 -fill both
pack [ttk::labelframe $Belt.status.fields.motor -text Motor] -expand 1 -fill both -side left
pack [ttk::radiobutton $Belt.status.fields.motor.ok -text OK -variable BeltMotor -value 1 -command updateBelt] -expand 1 -side left
pack [ttk::radiobutton $Belt.status.fields.motor.hot -text Overheat -variable BeltMotor -value 0 -command updateBelt] -expand 1 -side left
pack [ttk::radiobutton $Belt.status.fields.motor.ko -text Defect -variable BeltMotor -value -1 -command updateBelt] -expand 1 -side left
pack [ttk::labelframe $Belt.status.fields.safety -text "Security Flap"] -expand 1 -fill both -side left
pack [ttk::checkbutton $Belt.status.fields.safety.open -text Closed -onvalue 0 -offvalue 1 -variable BeltSecurityFlap -command updateBelt] -expand 1 -fill both
pack [ttk::labelframe $Belt.status.fields.light -text "Light Barrier"] -expand 1 -fill both -side left
pack [ttk::checkbutton $Belt.status.fields.light.free -text Free -onvalue 0 -offvalue 1 -variable BeltLightBarrier -command updateBelt] -expand 1 -fill both

# Procedure debug: Depending on whether debug has to be turned on or off, shows or hides console
proc debug {} {
	global Setup
	if [string equal [$Setup.debug cget -text] "Debug On"] {
		$Setup.debug configure -text "Debug Off"
		console show
		raise .
	} {
		$Setup.debug configure -text "Debug On"
		console hide
	}
}

# Procedure startStop: Depending on whether socket has been opened, the socket will be closed or a new socket
# will be created and bound to a message handler
proc startstop {} {
    global Socket Port Setup

    if {$Socket == ""} {
        set Socket [udp_open $Port]
        fconfigure $Socket -buffering none -translation binary
        fileevent $Socket readable messageHandler
        puts "Listening for messages on UDP port [fconfigure $Socket -myport]"
        $Setup.startstop configure -text Stop
    } {
        fileevent $Socket readable ""
        puts "Stop message handling on UDP port [fconfigure $Socket -myport]"
        close $Socket
        set Socket ""
        $Setup.startstop configure -text Start
    }
}

set Port 23765
set Socket ""

# Procedure messageHandler: Reads message from a socket and calls sub-device specific frame handler until a handler
# returns a non-empty value.
# It is assumed tat all commands have the format <subdev>:<subcommand>[,<subdev>:<subcommand>...], where <subdev> is one of
# DRAWER and Belt
# and <subcommand> is a sub-device specific sub-command.
# Currently, we have the following handlers for sub-devices:
# drawerMessage and beltMessage
# If a handler returns a non-empty string <resp>, messageHandler sends back <subdev>:<resp>.
# If the frame format is wrong or <subdev> is invalid, invalid command handling occurs.
proc messageHandler {} {
    global Socket DrawerState

	set devlist {{DRAWER drawerMessage} {BELT beltMessage}}
    set frame [read $Socket]
    fconfigure $Socket -remote [set peer [fconfigure $Socket -peer]]
    puts -nonewline "Got >$frame< from $peer, "
	set resp [set separator ""]
	foreach subframe [split $frame ","] {
		foreach subdev $devlist {
			set cmdparts [split $subframe ":"]
			if {[lindex $subdev 0] == [lindex $cmdparts 0]} {
				set subresp "[[lindex $subdev 1] [lindex $cmdparts 1]]"
				if {$subresp == ""} {
					if {[set cmd [string length [lindex $cmdparts 1]]] < 20} {
						set subresp "Inv!$cmd"
					} {
						set subresp "Inv![string range $cmd 0 16]..."
					}
				}
				set resp "$resp$separator[lindex $subdev 0]:$subresp"
				set separator ","
			}
		}
	}
	if {$resp != ""} {
		puts -nonewline $Socket $resp
		puts "put $resp"
		return ""
	}
	set resp ""
	set len [string length $frame]
	for {set i 0} {$i < $len} {incr i} {
		if {($i == 0 && [scan $frame "%c" asc] == 1) || ($i > 0 && [scan $frame "%[set i]s%c" x asc] == 2)} {
			if {$asc >= 0x20 && $asc != 92 && $asc != 58 && $asc != 44} {
				set resp "$resp[format %c $asc]"
			} {
				# Response may not contain additional "\" (92), "," (44) or ":" (58)
				set resp "$resp[format "\\%03o" $asc]"
			}
		}
		if {$i == 19} {
			set resp "$resp..."
			break;
		}
	}
	puts -nonewline $Socket "INVALID:$resp"
	puts "put INVALID:$resp"
}

# Command handler for CashDrawer simulation. Commands start with "DRAWER:", followed by
# - "Open": Command to open the drawer or
# - "GetState": Command to retrieve the current drawer state.
# Returns the frame to be sent as response: "DRAWER:" followed by the current drawer state (0 = closed, 1 = open)
# or an empty string if the frame is no valid drawer command.
proc drawerMessage {frame} {
	global DrawerState
	if {$frame == "Open"} {
		set DrawerState 1
	}
	if {$frame == "Open" || $frame == "GetState"} {
		return $DrawerState
	}
	return ""
}

set DrawerState 0

# Command handler for Belt simulation. Commands start with "BELT:", followed by
# - "SpeedX": Command to set the belt speed, where X is 0 (off), 1 (slow) or 2 (fast)
# - "GetState": Command to retrieve the current belt state.
# Returns the frame to be sent as response: "BELT:" followed by the current belt state WXYZ, where
# W is the current belt speed (0 = off, 1 = slow, 2 = fast),
# X is the current motor state (0 = OK, 1 = overheat, 2 = defective),
# Y is the current security flap state (0 = closed, 1 = open),
# Z is the current light barrier state (0 = free, 1 = interrupted)
# or an empty string if the frame is no valid belt command.
proc beltMessage {frame} {
	global BeltState BeltMotor BeltLightBarrier BeltSecurityFlap
	if {[scan $frame "%5s%d%s" cmd val s] == 2 && $cmd == "Speed" && $val >= 0 && $val <= 2} {
		set BeltState $val
		updateBelt
	} elseif {$frame != "GetState"} {
		return ""
	}
	return "[expr $BeltLightBarrier ? 0 : $BeltState][expr 1 - $BeltMotor]$BeltSecurityFlap$BeltLightBarrier"
}

proc updateBelt {} {
	global BeltState BeltMotor BeltLightBarrier BeltSecurityFlap Belt
	if {$BeltMotor > 0 && $BeltLightBarrier == 0 && $BeltSecurityFlap == 0 && $BeltState > 0} {
		set vals [lindex {{} {green Slow} {{light green} Fast}} $BeltState]
		$Belt.status.label configure -text [lindex $vals 1] -background [lindex $vals 0]
	} {
		$Belt.status.label configure -text Stopped -background red
		if {$BeltMotor <= 0 || $BeltSecurityFlap == 1} {
			set BeltState 0
		}
	}
}
set BeltState 0
set BeltMotor 1
set BeltLightBarrier 0
set BeltSecurityFlap 0

