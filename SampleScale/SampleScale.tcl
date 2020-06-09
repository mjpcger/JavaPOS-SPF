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

# Scale simulator, can be used if no read scale is available. The simulator
#  simulates a scale that supports Checkout Dialog 02/04
#  Communication with the scales application is via COM port or via TCP/IP.
#  In case of TCP/IP, Port must be filled with the service port number, a values
#  between 0 and 65535, and Scale Dialog will be ignored.
#  In case of a COM port, Port must be filled with the port name and Scale Dialog
#  will be used as follows:
#  - Under Windows, COM ports > COM9 must be specified with prefix \\.\. e.g.
#    \\.\COM10
#  - Scale Dialog 02 and Dialog 04 are the same protocols, only with different
#	 baud rates. Mode for Dialog 02 is 2400 baud, odd parity, 7 bit data, 1 stop bit.
#	 Mode for Dialog 04 is 4800 baud, odd parity, 7 bit data, 1 stop bit.

# Procedure that adjusts weight and price whenever the scale widget changes its value
proc setWeightPrice x {
	global Time
	updateWeightPrice
	set Time [clock milliseconds]
}

# Procedure that adjusts the price whenever weight or unit price have been changed
proc updateWeightPrice {} {
	global Weight UnitPrice Price Tara WeightTx PriceTx TotalTx
	set Weight [expr round([.sc.sc get]) - $Tara]
	set Price [expr ($UnitPrice * $Weight + 500) / 1000]
	set PriceTx [format %1.2f [expr $UnitPrice / 100.0]]
	if {$Weight >= 0 && [.sc.sc get] < 5000} {
		set WeightTx [format %1.3f [expr $Weight / 1000.0]]
	} {
		set WeightTx "-.---"
	}
	if {$Price > 999999 || $Price < 0 || [.sc.sc get] == 5000} {
		set TotalTx "----.--"
	} {
		set TotalTx [format %1.2f [expr $Price / 100.0]]
	}
}

# Procedure that will be called whenever the Debug button is pressed. Shows or hides
# console, depending on previous state
proc debug {} {
    global Debug

    if {$Debug} {
        console hide
        set Debug 0
    } {
        console show
        set Debug 1
    }
}

# Procedure to start or stop communication. Must be called after port or mode of
# communication has been changed.
proc startstop {} {
	global SFd Fd Port PortParam Protocol GotEot

	if {$SFd == "" && $Fd == ""} {
		if {[catch {expr $Port + 0}] == 0 && $Port > 0 && $Port < 65535} {
			if [catch {
				set fd [socket -server acceptConnect $Port]
				set SFd $fd
				.dev.st configure -text Stop
			}] {
				puts "Bad port"
			}
		} {
			if [catch {
				set fd [open $Port r+]
				fconfigure $fd -blocking 1 -buffering none -encoding ascii -translation binary -mode [lindex $PortParam $Protocol] -timeout 20
				fileevent $fd readable inputCB
				set SFd [set Fd $fd]
				set GotEot 0
				.dev.st configure -text Stop
			}] {
				puts "Bad port or bad parameter"
			}
		}
	} {
		catch {fileevent $Fd readable {}}
		catch {close $SFd}
		set SFd ""
		catch {close $Fd}
		set Fd ""
		.dev.st configure -text Start
	}
}

# Procedure that will be called whenever an application tries to connect to the
# scale in case of TCP/IP communication.
proc acceptConnect {channel addr port} {
    global Fd GotEot
    if {$Fd != ""} {
        puts "Connect from $addr:$port rejected"
        close $channel
    } {
        puts "Connect from $addr:$port accepted"
        fconfigure $channel -blocking 0 -buffering none -encoding ascii -translation binary
        fileevent $channel readable inputCB
        set Fd $channel
		set GotEot 0
    }
}

# Generate dump data
proc dumpdata {buffer} {
	set len [string length $buffer]
	set res ""
	for {set i 0} {$i < $len} {incr i} {
		scan [string index $buffer $i] %c code
		if {$code == 2} {
			set res "$res STX"
		} elseif {$code == 3} {
			set res "$res ETX"
		} elseif {$code == 4} {
			set res "$res EOT"
		} elseif {$code == 5} {
			set res "$res ENQ"
		} elseif {$code == 6} {
			set res "$res ACK"
		} elseif {$code == 0x15} {
			set res "$res NAK"
		} elseif {$code == 27} {
			set res "$res ESC"
		} elseif {$code < 32 || $code > 127} {
			set res "$res [format %02Xh $code]"
		} {
			set res "$res [format %c $code]"
		}
	}
	return $res
}

# Read and dump data from communication stream
proc readdump {fd count} {
	set req [read $fd $count]
	if {$req != ""} {
		puts ">>[dumpdata $req]"
	} {
		puts ">> (timeout or disconnect)"
	}
	return $req
}

# Write and dump data to the communication stream
proc writedump {fd resp} {
	puts -nonewline $fd $resp
	puts "<<[dumpdata $resp]"
}

# Protocol handler (read callback). Implements Checkout Dialog 02 subset
# (without tara commands)
proc inputCB {} {
	global Fd State Time LastTime UnitPrice Text Weight Price Tara
	
    if {[eof $Fd] != 0} {
		# Disconnect from client (only TCP)
        puts "Disconnect client socket"
        fileevent $Fd readable {}
        close $Fd
        set Fd ""
        return
    }
	set c [readdump $Fd 1]
	set code 0
	if {[scan $c %c code] == 1 && $code == 2} {
		# Retrieve data until ETX and perform action according to the given command
		if {[scan [set data [readdump $Fd 3]] %d%c rec del] == 2 && ($del == 27 || $del == 3)} {
			# Got record type and ESC or ETX
			if {$rec == 8 && $del == 3} {
				# Status information request
				writedump $Fd [format "%c%02d%c%s%c" 2 9 27 $State 3]
				return
			} elseif {$rec == 1 && $del == 27 && [scan [set data [readdump $Fd 8]] %d%c%c up es ex] == 3 && $es == 27 && $ex == 3} {
				# Set unit price
				set tx $Text
				set ta $Tara
			} elseif {$rec == 3 && $del == 27 && [scan [set data [readdump $Fd 12]] %d%c%d%c up es ta ex] == 4 && $es == 27 && $ex == 3} {
				# Set unit price and tara
				set tx $Text
			} elseif {$rec == 4 && $del == 27 && [scan [string map {" " "\1"} [set data [readdump $Fd 21]]] %d%c%13s%c up es tx ex] == 4 && $es == 27 && $ex == 3} {
				# Set unit price and text
				set ta $Tara
			} elseif {$rec == 5 && $del == 27 && [scan [string map {" " "\1"} [set data [readdump $Fd 26]]] %d%c%d%c%13s%c up es ta es1 tx ex] == 6 && $es == 27 && $es1 == 27 && $ex == 3} {
				# Set unit price, tara and text
			} elseif {[waitEtx $data] == 3} {
				# Invalid record: Send NAK
				writedump $Fd "\25"
				set State "10"
				return
			}
			# Check price and tara
			if {[scan "$up\33$ta" "%d\33%d%c" pr tr e] == 2} {
				# Price and tara are valid
				set Text [string map {"\1" " "} $tx]
				puts "up: $up / $pr, ta: $ta / $tr, tx: >$tx< / >$Text<"
				set UnitPrice $pr
				set Tara $tr
				updateWeightPrice
				writedump $Fd "\6"
				set State "00"
			} {
				# Not a valid unit price or not a valid tara value
				writedump $Fd "\25"
				if {[scan "$up" "%d%c" pr e] == 1} {
					# Price is correct, must be invalid tara
					set State "12"
				} {
					# Price invalid
					set State "11"
				}
			}
		} elseif {[waitEtx $data] == 3} {
			# Invalid record: Send NAK
			writedump $Fd "\25"
			set State "10"
		}
	} elseif {$code == 5} {
		# Check whether weight is stable and not zero. If so, send weight and price, else NAK
		set wg $Weight
		set pr $Price
		set up $UnitPrice
		if {[clock milliseconds] - $Time < 500} {
			# Weight not stable
			writedump $Fd "\25"
			set State "20"
		} elseif {$LastTime == $Time} {
			# Weight not changed since last weighing
			writedump $Fd "\25"
			set State "21"
		} elseif {$up == 0} {
			# No unit price set
			writedump $Fd "\25"
			set State "22"
		} elseif {$pr == 0} {
			# Zero weight
			writedump $Fd "\25"
			set State "30"
		} elseif {$pr < 0} {
			# Price negative due to negative weight
			writedump $Fd "\25"
			set State "31"
		} elseif {$pr > 999999 || $wg == 5000 - $Tara} {
			# Weight too high
			writedump $Fd "\25"
			set State "32"
		} {
			set State "00"
			set LastTime $Time
			set resp [format "%c02%c3%c%05d%c%06d%c%06d\3" 2 27 27 $wg 27 $up 27 $pr]
			writedump $Fd $resp
		}
	}
}

# Wait for EXT or EOT. Used whenever reception of incomplete or unknown frame happened.
proc waitEtx {buffer} {
	global Fd
	
	set last 0
	for {set len [expr [string length $buffer] - 1]} {$len >= 0} {set len [expr [string length [set buffer [readdump $Fd 1]]] - 1]} {
		if {[scan [string index $buffer $len] %c last] != 1 || ($last == 3 && $last == 4)} break
	}
	return $last
}

set UnitPrice 0;		# Unit price
set Weight 0;			# Weight
set Price 0;			# Price (unit price * weight) or "------" in case of overflow
set Tara 0;				# Tara weight, will be subtracted from weight before price calculation
set Text "";			# Item text
set Time [clock milliseconds]; # Time of last weight
set LastTime 0;         # Time of last requested weight
set Port 53124;			# COM port or TCP server port
set Protocol "02";      # Scale dialog 02 is the default
set PortParam {"" "" "2400,o,7,1" "" "4800,o,7,1"}
set Debug 0;			# Debug flag
set SFd {};				# Server socket or COM port handle
set Fd {};				# Client socket or copy of COM port handle
set State 00;			# Scale state of last scale data reading operation

# Initialization of the GUI
pack [ttk::frame .dev] -expand 1 -fill both
pack [ttk::labelframe .dev.set -text Port] -fill y -side left
pack [ttk::entry .dev.set.e -textvariable Port -width 10] -expand 1 -fill both
pack [ttk::labelframe .dev.port -text "Scale Dialog"] -fill y -side left
pack [ttk::combobox .dev.port.e -textvariable Protocol -width 2 -values {"02" "04"}] -expand 1 -fill both
if {[catch {console hide}] == 0} {
    pack [ttk::button .dev.dbg -text Debug -width 6 -command debug] -expand 1 -fill both -side left
}
pack [ttk::button .dev.st -text Start -width 6 -command startstop] -expand 1 -fill both -side left
pack [ttk::labelframe .sc -text Scale] -expand 1 -fill both
pack [ttk::scale .sc.sc -from 0 -to 5000 -command setWeightPrice] -expand 1 -fill x
pack [ttk::frame .sc.f] -expand 1 -fill both
pack [ttk::labelframe .sc.f.tx -text "Item"] -expand 1 -fill both -side left
pack [ttk::labelframe .sc.f.up -text "Price"] -expand 1 -fill both -side left
pack [ttk::labelframe .sc.f.wg -text "Weight"] -expand 1 -fill both -side left
pack [ttk::labelframe .sc.f.tot -text "Total"] -expand 1 -fill both -side left
font configure TkFixedFont -size 16 -weight bold
pack [ttk::label .sc.f.tx.l -textvariable Text -width 13 -font TkFixedFont -anchor w] -expand 1 -fill y
pack [ttk::label .sc.f.up.l -textvariable PriceTx -width 7 -font TkFixedFont -anchor e] -expand 1 -fill both
pack [ttk::label .sc.f.wg.l -textvariable WeightTx -width 5 -font TkFixedFont -anchor e] -expand 1 -fill y
pack [ttk::label .sc.f.tot.l -textvariable TotalTx -width 7 -font TkFixedFont -anchor e] -expand 1 -fill y
updateWeightPrice