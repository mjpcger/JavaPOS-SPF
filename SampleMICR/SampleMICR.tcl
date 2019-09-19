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

# Structure of simplified MICR track as described in Epson TM-H6000 manual (E13B):
# < ttttttttt < aaaaaaaaaaaaaaa ; ssss
# where	< is the Transit character (T),
#	; is the On-Us character (O),
#	ttttttttt is a left-justified maximum 9-digit transit number,
#	aaaaaaaaaaaaaaa is a right-justified maximum 15-digit account information
#					(account no), filled with space,
#	ssss is 4-digit bank specific special information field (e.g. check number)
# CMC7 tracks are not simulated (specification unclear)
#
# Commands:
#	I	Insertion, command to open virtual cheque slot for MICR reading.
#	R	Release, command to close virtual cheque slot for MICR reading.
# Reading MICR data will be simulated by pressing "Send Data" button after
# filling out the data fields.
#
# Response:
#	<data> LF
#	where <data> are the simulated MICR data, followed by
#		  LF	 is the line feed character "\n" (0Ah)
#

set Port 45654
set Fd ""
set SFd ""

set Transit ""
set Account ""
set SpecInfo ""

proc validatelen {len value} {
	if {$value == ""} {
		return 1
	} {
		if {[string length $value] <= $len} {
			switch -glob "0$value" {*[-+e.E]*} {
				return 0
			}
			if {[catch {
				set retval [expr "1$value" % 1000000000 >= 0] 
			}] == 0} {
				return $retval
			}
		}
	}
	return 0
}

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

proc startstop {} {
	global Port SFd Fd
	
	if [string equal [.co.startstop cget -text] "Start"] {
		if [catch {expr 1 / ($Port > 0 && $Port < 0xffff)}] {
			tk_messageBox -title "Startup error" -message "Invalid port $Port: Must be a numeric value between 1 and 65534" -icon error -type ok
		} {
			if [catch {socket -server service $Port} SFd] {
				tk_messageBox -title "Startup error" -message "Server could not be created, reason: $SFd" -icon error -type ok
				set SFd ""
			} {
				.co.startstop configure -text "Stop"
				puts "Waiting for connect on port $Port..."
			}
		}
	} {
		.co.startstop configure -text "Start"
		catch {close $SFd}
		catch {close $Fd}
		set SFd [set Fd ""]
	}
}

proc service {fd addr port} {
	global SFd Fd
	
	if {$Fd == ""} {
		puts "Connect from $addr:$port accepted, fd = $fd"
		set Fd $fd
		fconfigure $fd -blocking 0 -buffering none -translation binary -encoding binary
		fileevent $Fd readable {processInput}
	} {
		puts "Connect from $addr:$port rejected"
		close $fd
	}
}

proc processInput {} {
	global Fd SndButton
	
	if [eof $Fd] {
		fileevent $Fd readable ""
		close $Fd
		set Fd ""
		puts "Connection disconnected. Waiting for connect"
	} {
		set cmd [read $Fd 1]
		if {$cmd == "I"} {
			puts "< I: Insert: Reader now ready to transmit data"
			$SndButton configure -state normal
		} elseif {$cmd == "R"} {
			puts "< R: Release: Reader now disabled"
			$SndButton configure -state disabled
		} elseif {[scan $cmd {%c} val] == 1 && $val < 32} {
			puts "< <$val>: Unknown command"
		} {
			puts "< $cmd: Unknown command"
		}
	}
}

proc sendData {} {
	global Transit Account SpecInfo Fd SndButton
	
	if {$Fd != ""} {
		set data [format "<%-9s<%15s;%04s" $Transit $Account $SpecInfo]
		puts $Fd $data
		puts "> $data LF"
		$SndButton configure -state disabled
	} {
		puts "Could not transmit data: Reader disconnected"
	}
}

# Communication
pack [ttk::labelframe .co -text Setup] -fill both
pack [ttk::labelframe .co.port -text "Port"] -fill y -side left
pack [ttk::entry .co.port.e -textvariable Port -width 5] -fill both -side left
pack [ttk::button .co.debug -text "Debug On" -command debug] -fill y -side left
pack [ttk::button .co.startstop -text "Start" -command startstop] -expand 1 -side left -fill both
pack [set SndButton [ttk::button .co.senddata -text "Send Data" -command sendData -state disabled]] -expand 1 -side left -fill both
pack [ttk::labelframe .dt -text "Data"] -fill both
font create cardfont -family Courier -size 10 -weight bold
pack [ttk::labelframe .dt.tr -text "Transit"] -side left
pack [ttk::labelframe .dt.ac -text "OnUs"] -side left
pack [ttk::labelframe .dt.si -text "Spec"] -side left
pack [ttk::entry .dt.tr.e -textvariable Transit -font cardfont -width 9 -validate key -validatecommand {validatelen 9 %P}] -fill both
pack [ttk::entry .dt.ac.e -textvariable Account -font cardfont -width 15 -validate key -validatecommand {validatelen 15 %P}] -fill both
pack [ttk::entry .dt.si.e -textvariable SpecInfo -font cardfont -width 4 -validate key -validatecommand {validatelen 4 %P}] -fill both

