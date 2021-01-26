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

# The common part
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

set Port 23765
set Socket ""

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

# Procedure messageHandler: Reads message from a socket and calls sub-device specific frame handler until a handler
# returns a non-empty value.
# It is assumed that all commands have the format <subdev>:<subcommand>[,<subdev>:<subcommand>...], where <subdev> is one of
# DRAWER, BELT and CASHBOX.
# and <subcommand> is a sub-device specific sub-command.
# Currently, we have the following handlers for sub-devices:
# drawerMessage, beltMessage and cashBoxMessage
# If a handler returns a non-empty string <resp>, messageHandler sends back <subdev>:<resp>.
# If the frame format is wrong or <subdev> is invalid, invalid command handling occurs.
proc messageHandler {} {
    global Socket

	set devlist {{DRAWER drawerMessage} {BELT beltMessage} {CASHBOX cashBoxMessage}}
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

# The Drawer part

set Drawer .all.row1.drw
pack [ttk::labelframe $Drawer -text "Drawer Status"] -expand 1 -fill both -side left
pack [ttk::radiobutton $Drawer.off -text Closed -variable DrawerState -value 0] -expand 1 -side left
pack [ttk::radiobutton $Drawer.slow -text Opened -variable DrawerState -value 1] -expand 1 -side left

# Command handler for CashDrawer simulation. Commands start with "DRAWER:", followed by
# - "Open": Command to open the drawer or
# - "GetState": Command to retrieve the current drawer state.
# Returns the frame to be sent as response: "DRAWER:" followed by the command and the current drawer state (0 = closed, 1 = open)
# or an empty string if the frame is no valid drawer command.
proc drawerMessage {frame} {
	global DrawerState
	if {$frame == "Open"} {
		set DrawerState 1
	}
	if {$frame == "Open" || $frame == "GetState"} {
		return "$frame$DrawerState"
	}
	return ""
}

set DrawerState 0

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

# Command handler for Belt simulation. Commands start with "BELT:", followed by
# - "SpeedX": Command to set the belt speed, where X is 0 (off), 1 (slow) or 2 (fast). Returns "Speed" followed by belt state.
# - "GetState": Command to retrieve the current belt state. Returns "GetState" followed by the belt state.
# The format of the belt state is WXYZ, where
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
	} elseif {$frame != [set cmd "GetState"]} {
		return ""
	}
	return "$cmd[expr $BeltLightBarrier ? 0 : $BeltState][expr 1 - $BeltMotor]$BeltSecurityFlap$BeltLightBarrier"
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

# The Cash box (Device to accept and dispense coins and bills

set CashBox .all.row3

# Set cash slot variables
set CashBoxSlotValues {1 2 5 10 20 50 100 200 500 1000 2000 5000 10000 20000 50000}
foreach i $CashBoxSlotValues {
	set CashBoxSlot($i) 10
}
set CashBoxJam 0
set CashBoxInput 0
set CashBoxFilling 0
set CashBoxSavedSlots ""

pack [ttk::labelframe $CashBox -text CashBox] -expand 1 -fill both
pack [ttk::frame $CashBox.u] -expand 1 -fill both
foreach i $CashBoxSlotValues {
	if {$i % 100 == 0} {
		pack [ttk::labelframe $CashBox.u.c$i -text "[expr $i / 100] [format %c 8364]"] -expand 1 -fill both -side left
	} {
		pack [ttk::labelframe $CashBox.u.c$i -text "$i ct"] -expand 1 -fill both -side left
	}
	pack [ttk::entry $CashBox.u.c$i.l -text CashBoxSlot($i) -width 3 -state disabled -validate focusout -validatecommand "leaveSlot $i %P"] -expand 1 -fill both
	pack [ttk::button $CashBox.u.c$i.b -text "+" -width 1 -state disabled -command "incrCash $i 1"] -expand 1 -fill both
}
pack [ttk::frame $CashBox.i] -expand 1 -fill both
pack [ttk::labelframe $CashBox.i.tx -text "Payment"] -fill both -side left -expand 1
pack [ttk::label $CashBox.i.tx.l -text "" -width 80] -expand 1 -fill y
pack [ttk::labelframe $CashBox.i.st -text "State"] -fill both -side left -expand 1
pack [ttk::label $CashBox.i.st.l -text "OK" -width 20] -expand 1 -fill y -side left
pack [ttk::checkbutton $CashBox.i.st.c -text "Jam" -variable CashBoxJam -command changeJam] -expand 1 -fill y -side left
pack [ttk::checkbutton $CashBox.i.st.b -text "Filling" -variable CashBoxFilling -command changeFilling] -expand 1 -fill y -side left

proc leaveSlot {i value} {
    global CashBoxSlot CashBox

    if {[scan $value "%d%s" val x] == 1 && $val >= 0 && $val <= 100} {
        set CashBoxSlot($i) $val
    } {
        $CashBox.u.c$i.l delete 0 end
        $CashBox.u.c$i.l $CashBoxSlot($i)
        bell
    }
    setState
    return 1
}
# Increment slot i by val. Val can be positive to increment the amount per slot or negative to decrement the amount
# Returns 1 on success, 0 if increment or decrement would set the amount outside the allowed range (0 - 100).
proc incrCash {i val} {
	global CashBoxSlot CashBox

	set newval [expr $CashBoxSlot($i) + $val]
	if {$newval < 0 || $newval > 100} {
		return 0
	}
	incr CashBoxSlot($i) $val
	$CashBox.u.c$i.l delete 0 end
	$CashBox.u.c$i.l insert 0 $val
	setPaied [expr $val * $i]
	setState
	return 1
}

proc changeFilling {} {
	global CashBoxFilling CashBox CashBoxSlotValues
	
	if $CashBoxFilling {
		resetSlots
		foreach i $CashBoxSlotValues {
			$CashBox.u.c$i.b configure -state disabled		
		}
		set newstate normal
	} {
		set newstate disabled
	}
	foreach i $CashBoxSlotValues {
		$CashBox.u.c$i.l configure -state $newstate
	}
	setState
}

proc changeJam {} {
	global CashBoxJam CashBoxSlotValues CashBox
	if $CashBoxJam {
		resetSlots
		foreach i $CashBoxSlotValues {
			$CashBox.u.c$i.b configure -state disabled		
		}
	}
	setState
}

proc setState {} {
	global CashBoxFilling CashBoxJam CashBoxSlotValues CashBoxSlot CashBox CashBoxSavedSlots
	set state "OK"
	if $CashBoxFilling {
		set state "Opened"
	} elseif $CashBoxJam {
		set state "Jam"
	} {
		if {$CashBoxSavedSlots == ""} {
			foreach i $CashBoxSlotValues {
				if {$CashBoxSlot($i) <= 2} {
					if {$CashBoxSlot($i) > 0} {
						set state "Low"
					} {
						set state "Empty"
						break
					}
				}
			}
			if {$state == "OK"} {
                foreach i $CashBoxSlotValues {
                    if {$CashBoxSlot($i) >= 98} {
                        if {$CashBoxSlot($i) < 100} {
                            set state "High"
                        } {
                            set state "Full"
                            break
                        }
                    }
                }
			}
		} {
			set state "Input"
		}
	}
	$CashBox.i.st.l configure -text $state
}

proc getStateNum {} {
	global CashBox CashBoxSlots CashBoxJam CashBoxFilling CashBoxSavedSlots CashBoxSlotValues CashBoxSlot

	set hi [set lo 2]
	foreach {i j} [array get CashBoxSlot] {
	    if {$j <= 2 && $lo != 0} {
	        if {$j == 0} {
	            set lo 0
	        } {
	            set lo 1
	        }
	    } elseif {$j >= 98 && $hi != 0} {
	        if {$j == 100} {
	            set hi 0
	        } {
	            set hi 1
	        }
	    }
	}
	if $CashBoxFilling {
	    set ok 4
	} elseif $CashBoxJam {
	    set ok 3
	} elseif {$CashBoxSavedSlots == ""} {
	    set ok 0
	} {
	    set ok 2
		foreach i $CashBoxSlotValues {
			if {[$CashBox.u.c$i.b cget -state] != "disabled"} {
			    set ok 1
			    break
			}
		}
	}
	return "$lo$hi$ok"
}

proc setPaied {val} {
	global CashBoxInput CashBox
	
	incr CashBoxInput $val
	$CashBox.i.tx.l configure -text [format "Input: %3.2f %c" [expr $CashBoxInput / 100.0] 8364 ]
}
setPaied 0

proc saveSlots {} {
	global CashBoxSavedSlots CashBoxSlot CashBoxInput
	
	set CashBoxSavedSlots [array get CashBoxSlot]
	set CashBoxInput 0
	setPaied 0
}

proc fixSlots {} {
	global CashBoxSavedSlots CashBoxInput
	
	set CashBoxSavedSlots ""
	set CashBoxInput 0
	setPaied 0
}

proc resetSlots {} {
	global CashBoxSavedSlots CashBoxSlot CashBoxInput
	
	if {$CashBoxSavedSlots != ""} {
		array set CashBoxSlot $CashBoxSavedSlots
		foreach {i j} $CashBoxSavedSlots {
		    incrCash $i 0
		}
		fixSlots
	}
}

# Command handler for CashBox simulation. Commands start with "CASHBOX:", followed by
# - "GetState": Command to retrieve the current state: Returns "GetStateNMO" where
#   N is 0: (one slot)Empty, 1: (one slot) Low, 2: OK,
#   M is 0: (one slot) Full, 1: (one slot) High, 2: OK and
#   O is 0: Operational, 1: Waiting for input, 2: Input stopped, 3: Jam, 4: Opened
# - "GetInput": Command to retrieve current input amount in minimum units. Returns "GetInputN" where N is current input in minimum units.
# - "OutputXN: Command to output N minimum X-units, where X =C for coins, =B for bills and =A for coins or bills.
#   Returns "OutputM" where M is the minimum units that could be output (If M < N, at least one slot is empty)
# - "StartInputX": Command to start cash input. Enables the '+' buttons. X specifies whether coins (1), bills (2) or both (3) shall be accepted.
#   Returns "StartInputN" where N is the current input (normally 0).
#   in minimum units.
# - "StopInput": Command to stop cash input. Disables enabled '+' buttons. Returns "StopInputN" where N is the final input in minimum units.
# - "EndInput": Command to finish cash input. Disables enabled '+' buttons. Returns "EndInputN" where N is the final input in minimum units.
# - "CancelInput": Command to cancel input. Disables enabled '+' buttons and restores previous slot values. Returns "CancelInputN" where N is the
#   cancelled input (normally 0) in minimum units.
# - "GetSlots": Command to retrieve the current slot amounts. Returns "GetSlotsL", where L is a list of value pairs separated by spaces where the
#   first value of each pair specifies the cash value and the second value the amount of cash units in a slot.
# - "AddSlotsK": Command to add cash units to the corresponding slots. K is a list of value pairs separated by spaces, two values per slot, 
#   where the first value of each pair specifies the cash value and the second value the amount of cash units to be added. Returns "AddSlotsL", where
#   L is a list of value pairs separated by spaces where the first value of each pair specifies the cash value and the second value the amount
#   of cash units in a slot.
proc cashBoxMessage {frame} {
	global CashBoxSlot CashBoxSlotValues CashBoxInput CashBox CashBoxJam CashBoxFilling CashBoxSavedSlots
	if {$frame == "GetState"} {
		return "GetState[getStateNum]"
	} elseif {$frame == "GetInput"} {
		return "GetInput$CashBoxInput"
	} elseif {[scan $frame "%10s%d%s" cmd val x] == 2 && $cmd == "StartInput"} {
		if {$CashBoxJam || $CashBoxFilling || $CashBoxSavedSlots != "" || $val < 1 || $val > 3} {
			return ""
		}
		saveSlots
		foreach i $CashBoxSlotValues {
		    if {(($val & 1) && $i < 500) || (($val & 2) && $i > 200)} {
			    $CashBox.u.c$i.b configure -state normal
			}
		}
		setState
		return "StartInput$CashBoxInput"
	} elseif {$frame == "StopInput"} {
		if {$CashBoxJam || $CashBoxFilling || $CashBoxSavedSlots == ""} {
			return ""
		}
		foreach i $CashBoxSlotValues {
			$CashBox.u.c$i.b configure -state disabled		
		}
		return "StopInput$CashBoxInput"
	} elseif {$frame == "EndInput"} {
		if {$CashBoxJam || $CashBoxFilling || $CashBoxSavedSlots == ""} {
			return ""
		}
		set ret "EndInput$CashBoxInput"
		fixSlots
		foreach i $CashBoxSlotValues {
			$CashBox.u.c$i.b configure -state disabled
		}
		setState
		return $ret
	} elseif {$frame == "CancelInput"} {
		if {$CashBoxJam || $CashBoxFilling || $CashBoxSavedSlots == ""} {
			return ""
		}
		resetSlots
		foreach i $CashBoxSlotValues {
			$CashBox.u.c$i.b configure -state disabled		
		}
		setState
		return "CancelInput$CashBoxInput"
	} elseif {$frame == "GetSlots"} {
		set l ""
		foreach i $CashBoxSlotValues {
			lappend l $i $CashBoxSlot($i)
		}
		return "GetSlots$l"
	} elseif {[scan $frame "%6s%1s%d%s" cmd what val x] == 3 && $cmd == "Output"} {
		if {$CashBoxJam || $CashBoxFilling || $val < 0 || [lsearch "A B C" $what] < 0} {
			return ""
		}
		set l ""
		set currentinput $CashBoxInput
		foreach i $CashBoxSlotValues {
			if {($i < 500 && $what != "B") || ($i > 200 && $what != "C")} {
			    set l [linsert $l 0 $i]
			}
		}
		set res 0
		foreach i $l {
			while {$val >= $i && $CashBoxSlot($i) > 0} {
				incr val -$i
				incr res $i
				incrCash $i -1
			}
			if {$val == 0} break
		}
        if {$CashBoxSavedSlots == ""} {
            set CashBoxInput $currentinput
            setPaied 0
        }
		setState
		return "Output$res"
	} elseif {[string range $frame 0 7] == "AddSlots"} {
		if {$CashBoxJam || $CashBoxFilling || [llength [set pairs [string range $frame 8 end]]] % 2 == 1} {
			return ""
		}
		set slots [array get CashBoxSlot]
		set currentinput $CashBoxInput
		set tobereset ""
		foreach {i j} $pairs {
			puts "$i $j"
			if {[lsearch $CashBoxSlotValues $i] < 0 || [scan $j "%d%s" val x] != 1} {
				array set CashBoxSlot $slots
				foreach k $tobereset {
				    incrCash $k 0
				}
                if {$CashBoxSavedSlots == ""} {
                    set CashBoxInput $currentinput
                    setPaied 0
                }
				return ""
			}
			incrCash $i $j
			lappend tobereset $i
		}
        if {$CashBoxSavedSlots == ""} {
            set CashBoxInput $currentinput
            setPaied 0
        }
		setState
		set l ""
		foreach i $CashBoxSlotValues {
			lappend l $i $CashBoxSlot($i)
		}
		return "AddSlots$l"
	}
	return ""
}
