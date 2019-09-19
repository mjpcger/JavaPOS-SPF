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

# Printer simulator. Simulates a cash printer with integrated cash drawer with the following command set:
#
# LF (0Ah): Line feed.
# FF (0Ch): Form feed (cut).
# ESC b n (1Bh 62h n): Bold off [n = "0" (30h)] or on [n = "1" (31h)].
# ESC c n (1Bh 63h n): Color (red) off [n = "0" (30h)] or on [n = "1" (31h)].
# ESC d (1Bh 64h): Open Drawer.
# ESC f n (1Bh 66h n): Select font A [n = "A" (41h)] or B [n = "B" (42h)].
# ESC o n (1Bh 6Fh n): Set orientation to center [n = "c" (63h)], left [n = "l" (6Ch)] or right [n = "r" (72h)].
# ESC p n (1Bh 70h n): Select code page. Valid values for n are:
#		"0" (30h): Code page 1250,
#		"1" (31h): Code page 1251,
#		"2" (32h): Code page 1252,
#		"3" (33h): Code page 1253,
#		"4" (34h): Code page 1254,
#		"5" (35h): Code page 1257,
#		"6" (36h): Unicode (UFT-8).
# ESC s (1Bh 73h): State request. Simulator sends back one of:
#		"S0" (30h): Printer in operational state, drawer closed,
#		"S1" (31h): Printer in operational state, drawer open,
#		"S2" (32h): Paper near end, drawer closed,
#		"S3" (33h): Paper near end, drawer open,
#		"S4" (34h): Paper end, drawer closed,
#		"S5" (35h): Paper end, drawer open,
#		"S6" (36h): Cover open, drawer closed,
#		"S7" (37h): Cover open, drawer open,
#		"S8" (38h): Printer error. drawer closed,
#		"S9" (39h): Printer error, drawer open.
# ESC u n (1Bh 75h n): Underline off [n = "0"(30h)] or on [n = "1"(31h)].
#
# In case of a printer status change, the status bytes (see ESC s) will be sent automatically, but the prefix character
# 'S' will be replaced by 's'. Initial values are:
# Font A, color, underline and bold off, code page 1252, left oriented.
# In addition, the printer status will be sent once after connection start. In case of TCP, this is after a connect,
# in case of serial connection when CTS changes from inactive to active.
#
# Current and last receipt are present in printer window. Paper length and near end offset can be set as long as
# the simulator has not been started. The status can be changed for testing purposes at any time.
#
# Simulator can communicate using COM ports (keep in mind: On Windows, COM ports > COM9 must be specified with
# leading "\\.\", e.g. COM12 must be specified as \\.\COM12) or TCP. For TCP, the given port must be an integer
# between 1 and 65535. If a COM port has been specified, a mode must be specified as well. The other end-point
# of the communication line must use the corresponding line settings.

pack [ttk::labelframe .a -text Settings] -expand 1 -fill both
pack [ttk::labelframe .a.p -text Port] -expand 1 -fill both -side left
pack [ttk::entry .a.p.e -textvariable Port -width 10 -validate key -validatecommand {portCheck %P}] -expand 1 -fill both
pack [ttk::labelframe .a.m -text Mode] -expand 1 -fill both -side left
pack [ttk::combobox .a.m.b -state disabled -values {"9600,n,8,2" "9600,e,8,1" "9600,o,8,1"}] -expand 1 -fill both
pack [ttk::button .a.dbg -text "Debug On" -width 5 -command setDebug] -fill both -expand 1 -side left
pack [ttk::button .a.st -text Start -width 5 -command startstop] -fill both -expand 1 -side left

set Port 65432
set Modes {
	"9600,n,8,2" "9600,e,8,1" "9600,o,8,1"
	"38400,n,8,2" "38400,e,8,1" "38400,o,8,1"
}

font create fontA -family Courier -size 12 -weight normal
font create boldA -family Courier -size 12 -weight bold
font create fontB -family Courier -size 9 -weight normal
font create boldB -family Courier -size 9 -weight bold
pack [ttk::labelframe .t -text "Printer Window"]
pack [text .t.x -width 42 -height 26 -font fontA -state disabled -yscrollcommand [list .t.s set]] -side left -expand 1 -fill y
pack [scrollbar .t.s -command [list .t.x yview]] -side right -expand 1 -fill y
.t.x tag add ABold end
.t.x tag configure ABold -font boldA
.t.x tag add ANormal end
.t.x tag configure ANormal -font fontA
.t.x tag add BBold end
.t.x tag configure BBold -font boldB
.t.x tag add BNormal end
.t.x tag configure BNormal -font fontB
.t.x tag add Underline end
.t.x tag configure Underline -underline 1
.t.x tag add Red end
.t.x tag configure Red -foreground red
.t.x tag add Left end
.t.x tag configure Left -justify left
.t.x tag add Right end
.t.x tag configure Right -justify right
.t.x tag add Center end
.t.x tag configure Center -justify center
.t.x tag add Strike end
.t.x tag configure Strike -overstrike 1

set Taglist {ANormal Left}
set CodePages {cp1250 cp1251 cp1252 cp1253 cp1254 cp1257 utf-8}
set CurrentCodepage cp1252
set FirstPos [.t.x index "end - 1 chars"]
set StartPos $FirstPos

# Add the given tag to Taglist, if it is not yet present in the list.
#				tagname must be one of Underline, Red
proc addTag {tagname} {
	global Taglist
	
	if {[lsearch $Taglist $tagname] == -1} {
		lappend Taglist $tagname
	}
}

# Delete the given tag from Taglist, if it is püresent in the list.
#				tagname must be one of Underline, Red
proc delTag {tagname} {
	global Taglist
	
	if {[set index [lsearch $Taglist $tagname]] > 0} {
		if {[llength $Taglist] - $index == 1} {
			set Taglist [lrange $Taglist 0 [expr $index - 1]]
		} {
			set Taglist [concat [lrange $Taglist 0 [expr $index - 1]] [lrange $Taglist [expr $index + 1] end]]
		}
	}
}

# Set font tag. fontname must be A or B. Taglist will be modified in its first element to start with the
#				given font name.
proc setFont {fontname} {
	global Taglist

	lset Taglist 0 "$fontname[string range [lindex $Taglist 0] 1 end]"
}

# Set font tag. weightname must be Normal or Bold. Tablist will be modified in its first element to end with
#				the given weight name.
proc setWeight {weightname} {
	global Taglist
	
	lset Taglist 0 "[string range [lindex $Taglist 0] 0 0]$weightname"
}

# Set orientation tag. tagname must be Jeft, Center or Right. The second element of tallist will be set to
#				the given tag name.
proc setOrientation {tagname} {
	global Taglist
	
	lset Taglist 1 $tagname
}

set LineCount 0
set PaperLimit 1000
set NearEndOffset 100
set StatusList {Operational {Paper Low} {Paper End} {Cover Open} {Printer Error}}
set DrawerStatusList {Closed Open}

pack [ttk::labelframe .s -text Status] -expand 1 -fill both
pack [ttk::labelframe .s.ml -text "Max.Lines"] -expand 1 -fill both -side left
pack [ttk::entry .s.ml.e -textvariable PaperLimit -width 5] -expand 1 -fill both
pack [ttk::labelframe .s.ne -text "Near End"]  -expand 1 -fill both -side left
pack [ttk::entry .s.ne.e -textvariable NearEndOffset -width 3] -expand 1 -fill both
pack [ttk::labelframe .s.pl -text "Prt.Lines"] -expand 1 -fill both -side left
pack [ttk::entry .s.pl.e -textvariable LineCount -width 5 -state readonly] -expand 1 -fill both
pack [ttk::labelframe .s.st -text "Status"] -expand 1 -fill both -side left
pack [ttk::combobox .s.st.b -state readonly -values $StatusList -width 11] -expand 1 -fill both
.s.st.b current 0
pack [ttk::labelframe .s.ds -text "Drawer"] -expand 1 -fill both -side left
pack [ttk::combobox .s.ds.b -state readonly -values $DrawerStatusList -width 7] -expand 1 -fill both
.s.ds.b current 0
pack [ttk::button .s.rp -text "Change Paper" -width 13 -command changePaper] -expand 1 -fill both

set PreviousState 0
set MaxLinesShown 100

proc changePaper {} {
    global LineCount
    set LineCount 0
	.t.x configure -state normal
    .t.x delete 1.0 end
	.t.x configure -state disabled
}

proc print {text} {
	global Taglist CurrentCodepage LineCount PaperLimit NearEndOffset PreviousState MaxLinesShown Fd
	
	set len [string length $text]
	set toBeInserted ""
	.t.x configure -state normal
	for {set i 0} {$i < $len} {incr i} {
		if {$PreviousState != [set state [.s.st.b current]]} {
			set PreviousState $state
			sendState
		}
		set c [string range $text $i $i]
		if {$c == "\14" || $c == "\33"} {
			if {$toBeInserted != "" && [.s.st.b current] < 2} {
				.t.x insert "end-1c" [encoding convertfrom $CurrentCodepage $toBeInserted] $Taglist
				set toBeInserted ""
			}
			if {$c == "\14"} {
				global StartPos FirstPos

                set cutline [expr [.t.x index end] - 3]
                if {$cutline >= 1} {
                    .t.x delete $cutline "$cutline + 1l"
                    .t.x insert $cutline "                                          \n" {{ANormal} {Strike}}
           	    }
			} {
				incr i
				set c [string range $text $i $i]
				if {$c == "d"} {
					.s.ds.b current 1
				} elseif {$c == "s"} {
					sendState
				} elseif {[lsearch {b c f o p u} $c] >= 0} {
					incr i
					set v [string range $text $i $i]
					if {$c == "b"} {
						if {$v == "0"} {
							setWeight Normal
						} elseif {$v == "1"} {
							setWeight Bold
						}
					} elseif {$c == "c"} {
						if {$v == "0"} {
							delTag Red
						} elseif {$v == "1"} {
							addTag Red
						}
					} elseif {$c == "f"} {
						if {$v == "A" || $v == "B"} {
							setFont $v
						}
					} elseif {$c == "o"} {
						if {$v == "l"} {
							setOrientation Left
						} elseif {$v == "c"} {
							setOrientation Center
						} elseif {$v == "r"} {
							setOrientation Right
						}				
					} elseif {$c == "p"} {
						global CodePages
						
						if {[scan $v "%d" j] == 1 && $j >= 0 && $j < [llength $CodePages]} {
							set CurrentCodepage [lindex $CodePages $j]
						}
					} {
						if {$v == "0"} {
							delTag Underline
						} elseif {$v == "1"} {
							addTag Underline
						}
					}
				}
			}
		} {
			if {$c == "\12"} {
				incr LineCount
				setStatus
			}
			set toBeInserted "$toBeInserted$c"
		}
	}
	if {$toBeInserted != "" && [.s.st.b current] < 2} {
		.t.x insert "end-1c" [encoding convertfrom $CurrentCodepage $toBeInserted] $Taglist
	}
	if {[set deletecount [expr int([.t.x index end] - $MaxLinesShown)]] > 0} {
	    .t.x delete 1.0 "1.0+[set deletecount]l"
	}
	.t.x see end
	.t.x configure -state disabled
}

proc setStatus {} {
	global LineCount NearEndOffset PaperLimit

	if {[.s.st.b current] == 0} {
		if {$LineCount >= $PaperLimit - $NearEndOffset} {
			.s.st.b current 1
		}
	}
	if {[.s.st.b current] == 1} {
		if {$LineCount >= $PaperLimit} {
			.s.st.b current 2
		}
	}
}

set LastState {}

proc sendState {} {
	global Fd SFd LastState
	
	set data [.s.st.b current]
	incr data [expr $data + [.s.ds.b current]]
	set readytosend 0
	catch {
		if {$Fd != "" && ($Fd != $SFd || [lindex [fconfigure $Fd -ttystatus] 1] == 1)} {
			puts -nonewline $Fd "$data"
			puts "<< $data"
			set LastState $data
		}
	}
}

proc checkStatusChange {} {
	global Fd SFd LastState
	
	set data [.s.st.b current]
	incr data [expr $data + [.s.ds.b current]]
	if {$Fd == "" || ($Fd == $SFd && [lindex [fconfigure $Fd -ttystatus] 1] != 1)} {
		set LastState ""
	} elseif {$data != $LastState} {
		sendState
	}
	after 1000 checkStatusChange
}

proc setDebug {} {
	if {[.a.dbg cget -text] == "Debug On"} {
		.a.dbg configure -text "Debug Off"
		console show
	} {
		.a.dbg configure -text "Debug On"
		console hide
	}
}

set SFd ""
set Fd ""

checkStatusChange

proc waitConnect {fd ip port} {
	global Fd
	
	if {$Fd != ""} {
		close $fd
		puts "Reject connect from $ip:$port"
	} {
		fconfigure $fd -blocking 0 -buffering none -translation binary -encoding binary
		fileevent $fd readable processData
		set Fd $fd
		puts "Connect from $ip:$port, waiting for data"
	}
}

proc startstop {} {
	global Port SFd Fd Modes
	
	if {[.a.st cget -text] == "Start"} {
		if {[.a.m.b cget -state] == "disabled"} {
			if [catch {socket -server waitConnect $Port} SFd] {
				set SFd ""
				return 0
			}
			puts "$Port opened, waiting for connect"
		} {
			if {[set mode [lindex $Modes [.a.m.b current]]] == "" || [catch {
				set SFd [open $Port r+]
				fconfigure $SFd -blocking 1 -buffering none -translation binary -encoding binary -mode $mode -timeout 10
				fileevent $SFd readable processData	
				set Fd $SFd
			}]} {
				set SFd ""
				return 0
			}
			puts "$Port opened, waiting for data"
		}
		.a.st configure -text "Stop"
		.a.p.e configure -state readonly
		.a.m.b configure -state disabled
		.s.ml.e configure -state readonly
		.s.ne.e configure -state readonly
	} {
		catch {
			close $SFd
			close $Fd
		}
		set Fd [set SFd ""]
		puts "$Port closed"
		.a.st configure -text "Start"
		.a.p.e configure -state normal
		portCheck $Port
		.s.ml.e configure -state normal
		.s.ne.e configure -state normal
	}
}

proc portCheck {value} {
	set currentstate [.a.m.b cget -state]
	if {[catch {expr int($value)} val] == 0 && $val == $value && $val > 0 && $val < 0xffff} {
		if {$currentstate != "disabled"} {
			.a.m.b configure -state disabled
		}
	} {
		if {$currentstate != "readonly"} {
			.a.m.b configure -state readonly
		}
	}
	return 1
}

proc processData {} {
	global Fd SFd Port
	
	if {$Fd != $SFd && [eof $Fd]} {
		fileevent $Fd readable ""
		close $Fd
		set Fd ""
		puts "Got disconnect. Waiting for connect on port $Port"
		return
	}
	if {[catch {read $Fd} c] == 1} {
		puts "Read error : $c"
	} {
		puts ">> $c"
		print $c
	}
}