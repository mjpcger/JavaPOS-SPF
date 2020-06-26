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

# This device provides a remote order device controller that supports up to
# 5 display devices with identical characteristics. All commands, responses
# and messages start with a two-byte address (01 - 05), followed by a message
# identifies, frame data and end with ETX (03h). A description for all
# supported commands is near the end of this script.

# Colors
set colors [list grey0 blue3 green3 cyan3 red3 magenta3 yellow3 grey67 grey33 blue1 green1 cyan1 red1 magenta1 yellow1 grey100]
set colorSelectLabelSize 20
pack [ttk::frame .co] -expand 1 -fill both
pack [ttk::labelframe .co.lors -text "Colors"] -fill y -side left
pack [canvas .co.lors.c -height $colorSelectLabelSize -width [expr $colorSelectLabelSize * [llength $colors]] -bg white] -side left
bind .co.lors.c <ButtonRelease-1> {colorSelect %x}
for {set i 0} {$i < [llength $colors]} {incr i} {
	set curcol [lindex $colors $i]
	.co.lors.c create rectangle [expr $i * $colorSelectLabelSize] 1 [expr ($i + 1)*$colorSelectLabelSize] $colorSelectLabelSize -fill $curcol -outline $curcol -tags c$i
}

proc colorSelect {pos} {
	global colors colorSelectLabelSize CurrentCanvas Canvases
	
	set pos [expr $pos / $colorSelectLabelSize]
	if {$pos >= 0 && $pos < [llength $colors] && [set color [tk_chooseColor -initialcolor [lindex $colors $pos]]] != ""} {
		lset colors $pos $color
		.co.lors.c itemconfigure c$pos -fill $color -outline $color
		set ccv $CurrentCanvas
		for {set cv 1} {$cv < [llength $Canvases]} {incr cv} {
			set CurrentCanvas [lindex $Canvases $cv]
			foreach id [$CurrentCanvas find withtag coltags$pos] {
				switch [$CurrentCanvas type $id] {
					rectangle {$CurrentCanvas itemconfigure $id -fill $color -outline $color}
					default {$CurrentCanvas itemconfigure $id -fill $color}
				}
			}
		}
		set CurrentCanvas $ccv
	}
}

proc sourceText {tx} {
	return [encoding convertfrom utf-8 [encoding convertto [encoding system] $tx]]
}

# Define display font. Font must match the criterium that both, width and height, must be even numbers for the following
set fontsize 8
font create myfont -family Helvetica -size $fontsize -weight bold
while 1 {
	set charwidth [font measure myfont [sourceText "W"]]
	set charheight [font metric myfont -linespace]
	if {$charwidth % 2 || $charheight % 2} {
		incr fontsize
		font configure myfont -size $fontsize
	} {
		break
	}
}

# Set width and height (in characters) for all displays
set DispWidth 25
set DispHeight 20
set DispCount 5
set Canvases {invalid}
set Clocks {}

# Define the displays here
pack [ttk::frame .d]
set winwidth [expr [set halfwidth [expr $charwidth / 2]] * [expr 2*$DispWidth]]
set winheight [expr [set halfheight [expr $charheight / 2]] * [expr 2 * $DispHeight]]
for {set cv 1} {$cv <= $DispCount} {incr cv} {
	pack [ttk::labelframe .d.l$cv -text "Display $cv"] -side left
	set CanvasActive$cv 0
	pack [ttk::frame .d.l$cv.f] -expand 1 -anchor w
	set CanvasActiveAfter$cv ""
	pack [ttk::checkbutton .d.l$cv.f.cb -text "Present" -variable CanvasActive$cv -onvalue 1 -offvalue 0 -command "clearLater $cv"] -side left -anchor w
	for {set bt 1} {$bt <= 8} {incr bt} {
	    pack [ttk::button .d.l$cv.f.bt$bt -text "F$bt" -command "button $cv $bt" -width 2] -side left -anchor w
	}
	pack [set CurrentCanvas [canvas .d.l$cv.c -height [expr $DispHeight*$charheight] -width [expr $DispWidth*$charwidth] -bg white]]
	bind $CurrentCanvas <ButtonPress-1> "buttonDown $cv %x %y"
	bind $CurrentCanvas <ButtonRelease-1> "buttonUp $cv %x %y"
	for {set i 2} {$i < $winwidth} {incr i $charwidth} {
		for {set j 2} {$j < $winheight} {incr j $charheight} {
			$CurrentCanvas create rectangle $i $j [expr $i + $charwidth - 1] [expr $j + $charheight - 1] -fill white -outline white -tags {bgtags coltags15}
			$CurrentCanvas create text $i $j -anchor n -font myfont -fill black -tags [list txtags coltags0 "c[expr $i / $charwidth]r[expr $j / $charheight]"] -text " "
		}
	}
    $CurrentCanvas move txtags $halfwidth 0
	lappend Canvases $CurrentCanvas
	lappend Clocks "$CurrentCanvas 0 0 0 0"
}

proc setCursor {x y color} {
    global charwidth charheight CurrentCanvas colors halfheight halfwidth

    set colorcode [lindex $colors $color]
    $CurrentCanvas delete cursortag
    if {$x > 0 && $y > 0} {
	    movein
        $CurrentCanvas create line $halfwidth 0 $halfwidth $charheight -width 1 -tag "cursortag coltags$colorcode blinktags" -fill $colorcode
	    $CurrentCanvas move cursortag [expr $charwidth * ($y - 1) + 2] [expr $charheight * ($x - 1) + 2]
        moveout
    }
}

# Bump bar key presses
proc button {d k} {
    upvar #0 CanvasActive$d ca
    global Fd
    if $ca {
        catch {
            puts -nonewline $Fd [format "%02dK%02d\3" $d $k]
        }
        puts "F$k on $d"
    }
}

# Click callback helper: Compute display coordinate from dot position to character position
proc charCoordinate {x y} {
	global charwidth charheight DispWidth DispHeight

	set x [expr int($x / $charwidth)]
	set y [expr int($y / $charheight)]
	if {$x < 0} {
	    set x 1
	} elseif {$x >= $DispWidth} {
	    set x $DispWidth
	} {
	    incr x
	}
	if {$y < 0} {
	    set y 1
	} elseif {$y >= $DispHeight} {
	    set y $DispHeight
	} {
	    incr y
	}
	return [list $x $y]
}

# Press touch handler
proc buttonUp {d x y} {
	# Compute character coordinates
	lassign [charCoordinate $x $y] x y
	global CanvasActive$d Fd
	if [set CanvasActive$d] {
		catch {
			# Send button up notification: Unit, line and column
			puts -nonewline $Fd [format "%02dU%02d%02d\3" $d $x $y] 
		}
		puts [sourceText "▲ $x $y (on $d)"]
	}
}

# Release touch handler
proc buttonDown {d x y} {
	# Compute character coordinates
	lassign [charCoordinate $x $y] x y
	global CanvasActive$d Fd
	if [set CanvasActive$d] {
		catch {
			# Send button down notification: Unit, line and column
			puts -nonewline $Fd [format "%02dD%02d%02d\3" $d $x $y] 
		}
		puts [sourceText "▼ $x $y (on $d)"]
	}
}

# Checkbutton callback
proc clearLater {cv} {
	global Canvases CurrentCanvas  Fd
	upvar #0 CanvasActive$cv ca CanvasActiveAfter$cv caa

	catch {
		# Send online or offline notification.
		puts -nonewline $Fd [format "%02dO%1d\3" $cv $ca]
	}
	if $ca {
	    puts [sourceText "═ (online $cv)"]
		if {[string equal $caa ""] == 0} {
			after cancel $caa
			set caa ""
		}
	} {
	    puts [sourceText "╪ (offline $cv)"]
		set caa [after 5000 "clearVideo $cv"]
	}
}

proc clearVideo {cv} {
	global Canvases CurrentCanvas charwidth charheight
	upvar #0 CanvasActiveAfter$cv caa
	
	set ccs $CurrentCanvas
	set CurrentCanvas [lindex $Canvases $cv]
	clearRegion 1 1 [expr [$CurrentCanvas cget -width] / $charwidth] [expr [$CurrentCanvas cget -height] / $charheight] 15 1
	set $CurrentCanvas $ccs
}

proc setColor {tags notags color} {
	global colors CurrentCanvas
	$CurrentCanvas addtag setColortags withtag $tags
	foreach notag $notags {
		$CurrentCanvas dtag $notag setColortags
	}
	$CurrentCanvas dtag setColortags coltags0
	$CurrentCanvas dtag setColortags coltags1
	$CurrentCanvas dtag setColortags coltags2
	$CurrentCanvas dtag setColortags coltags3
	$CurrentCanvas dtag setColortags coltags4
	$CurrentCanvas dtag setColortags coltags5
	$CurrentCanvas dtag setColortags coltags6
	$CurrentCanvas dtag setColortags coltags7
	$CurrentCanvas dtag setColortags coltags8
	$CurrentCanvas dtag setColortags coltags9
	$CurrentCanvas dtag setColortags coltags10
	$CurrentCanvas dtag setColortags coltags11
	$CurrentCanvas dtag setColortags coltags12
	$CurrentCanvas dtag setColortags coltags13
	$CurrentCanvas dtag setColortags coltags14
	$CurrentCanvas dtag setColortags coltags15
	$CurrentCanvas addtag coltags$color withtag setColortags
}

proc setFgColor {tags color} {
	global colors CurrentCanvas
	setColor $tags bgtags $color
	catch {$CurrentCanvas itemconfigure setColortags -fill [lindex $colors $color]}
	deleteTags setColortags
}

proc setBgColor {tags color} {
	global colors CurrentCanvas
	setColor $tags {txtags boxtags} $color
	catch {$CurrentCanvas itemconfigure setColortags -fill [lindex $colors $color] -outline [lindex $colors $color]}
	deleteTags setColortags
}

proc setTmptag {what x1 x2 x3 x4} {
    global CurrentCanvas

    $CurrentCanvas addtag tmptags $what [expr $x1+1] [expr $x2+1] [expr $x3+1] [expr $x4+1]
}

proc setText {line column text fg bg blink} {
	global charwidth charheight colors CurrentCanvas
	
	movein
	incr line -1
	incr column -1
	set count [string length $text]
    set x2 [expr [set x1 [expr $line * $charheight + 2]] + $charheight - 3]
    set y2 [expr [set y1 [expr $column * $charwidth + 2]] + $count * $charwidth - 3]
	for {set i 0} {$i < $count} {incr i} {
		$CurrentCanvas itemconfigure c[expr $column + $i]r$line -text [string range $text $i $i]
	}
	setTmptag overlapping $y1 $x1 $y2 $x2
	$CurrentCanvas dtag cursortag tmptags
	setFgColor tmptags $fg
	setBgColor tmptags $bg
	$CurrentCanvas dtag bgtags tmptags
	$CurrentCanvas dtag boxtags tmptags
	if $blink {
		$CurrentCanvas addtag blinktags withtag tmptags
	} {
		$CurrentCanvas dtag tmptags blinktags
	}
	deleteTags tmptags
	moveout
}

proc setAttributes {line column width height fg bg blink border} {
	global charwidth charheight CurrentCanvas
	
	movein
    set x2 [expr [set x1 [expr ($line - 1) * $charheight + 2]] + $height * $charheight - 3]
    set y2 [expr [set y1 [expr ($column - 1) * $charwidth + 2]] + $width * $charwidth - 3]
	setTmptag overlapping $y1 $x1 $y2 $x2
	if $border {
    	setTmptag enclosed [expr $y1 - 2] [expr $x1 - 2] [expr $y2 + 2] [expr $x2 + 2]
	}
	$CurrentCanvas dtag cursortag tmptags
	setFgColor tmptags $fg
	setBgColor tmptags $bg
	$CurrentCanvas dtag bgtags tmptags
	if $blink {
		$CurrentCanvas addtag blinktags withtag tmptags
	} {
		$CurrentCanvas dtag tmptags blinktags
	}
	deleteTags tmptags
	moveout
}

proc deleteTags {args} {
	global CurrentCanvas
	
	foreach tag $args {
		$CurrentCanvas dtag $tag
	}
}

proc clearClock {} {
    global CurrentCanvas Clocks

    for {set i 0} {$i < [llength $Clocks]} {incr i} {
        if {[lindex $Clocks $i 0] == $CurrentCanvas} {
            sendClock [expr $i +1]
            lset Clocks $i "$CurrentCanvas 0 0 0 0"
            break
        }
    }
}

proc setClock {type line column seconds} {
    global CurrentCanvas Clocks DispHeight DispWidth

    set h {3 4 7 7}
    if {[lsearch -exact {0 1 2 3} $type] >= 0 && $line > 0 && $column > 0 && $line <= $DispHeight && $column <= $DispWidth - [lindex $h $type]} {
        for {set i 0} {$i < [llength $Clocks]} {incr i} {
            if {[lindex $Clocks $i 0] == $CurrentCanvas} {
                lset Clocks $i "$CurrentCanvas $line $column $type $seconds"
                return 1
            }
        }
    }
    return 0
}

proc sendClock {unit} {
    global Clocks CurrentCanvas Fd

    set clock [lindex $Clocks [expr $unit - 1]]
    if {[lindex $clock 0] == $CurrentCanvas} {
        set data [readClock $clock]
        set type [lindex $clock 3]
        if {[scan $data [lindex {"%n%1d:%02d" "%n%02d:%02d" "%02d:%02d:%02d" "%02d:%02d:%02d"} $type] hour min sec] != 3 || $sec < 0 || $sec >= 60 || $min < 0 || $min >= [lindex {10 60 60 60} $type] || $hour < [lindex {0 0 1 0} $type] || $hour >= [lindex {1 1 13 24} $type]} {
            set data ":"
        }
        set frame [format "%02dT%s\3" $unit $data]
        catch {
            puts -nonewline $Fd $frame
            puts "> $frame"
        }
    }
}

proc readClock {clock} {
    lassign $clock canvas line column type seconds

    if {$type >= 0 && $seconds != 0} {
        incr line -1
        incr column -1
        set time ""
        set last [expr $column + [lindex {3 4 7 7} $type]]
        for {set i $column} {$i <= $last} {incr i} {
            set time "$time[$canvas itemcget c[set i]r[set line] -text]"
        }
    } {
        set time ":"
    }
    return $time
}

proc incClockText {clock} {
    lassign $clock canvas line column type seconds

    if {[set time [readClock $clock]] != ""} {
        incr line -1
        incr column -1
        set last [expr $column + [lindex {3 4 7 7} $type]]
        set mask [lindex {"%1d:%02d" "%02d:%02d" "%02d:%02d:%02d" "%02d:%02d:%02d"} $type]
        set newtime $time
        set upper {10 60 12 24}
        if {$type < 2} {
            if {[scan $time $mask min sec] == 2 && $sec >= 0 && $sec < 60 && $min >= 0 && $min < [lindex $upper $type]} {
                set sec [expr $sec + $seconds]
                if {$sec >= 60|| $sec < 0} {
					set min [expr ($min + ($sec / 60)) % [lindex $upper $type]]
                    set sec [expr $sec % 60] 
                }
                set newtime [format $mask $min $sec]
				lset clock 4 1
            } {
				lset clock 4 0
			}
        } elseif {[scan $time $mask hour min sec] == 3 && $sec >= 0 && $sec < 60 && $min >= 0 && $min < 60 && $hour + $type >= 3 && $hour + $type < [lindex $upper $type] + 3} {
            set sec [expr $sec + $seconds]
            if {$sec >= 60 || $sec < 0} {
				set min [expr ($min + ($sec / 60))]
				set sec [expr $sec % 60] 
                if {$min >= 60 || $min < 0} {
					set hour [expr ($hour + ($min / 60)) % [lindex $upper $type]]
					if {$hour == 0 && $type == 2} {
						set hour 12
					}
                    set min [expr $min % 60]
                }
            }
            set newtime [format $mask $hour $min $sec]
			lset clock 4 1
		} {
			lset clock 4 0
        }
        if {[string equal $time $newtime] != 1} {
            for {set i $column} {$i <= $last} {incr i} {
                set j [expr $i - $column]
                $canvas itemconfigure c[set i]r[set line] -text [string range $newtime $j $j]
            }
        }
    }
	return $clock
}

set BlinkOff 0

proc blink {} {
    global Clocks
	after [expr 1000 - [clock milliseconds] % 1000] {
		doBlink
		for {set i [expr [llength $Clocks] - 1]} {$i >= 0} {incr i -1} {
			lset Clocks $i [incClockText [lindex $Clocks $i]]
		}
		blink
	}
}

proc doBlink {} {
	global BlinkOff Canvases CurrentCanvas

	set ccv $CurrentCanvas
	if $BlinkOff {
		for {set cv 1} {$cv < [llength $Canvases]} {incr cv} {
			set CurrentCanvas [lindex $Canvases $cv]
			movein
			}
		set BlinkOff [expr 1 - $BlinkOff]
	} {
		set BlinkOff [expr 1 - $BlinkOff]
		for {set cv 1} {$cv < [llength $Canvases]} {incr cv} {
			set CurrentCanvas [lindex $Canvases $cv]
			moveout
			}
	}
	set CurrentCanvas $ccv
}

blink

proc moveout {args} {
	global BlinkOff CurrentCanvas halfwidth
	if {$BlinkOff != 0} {
		$CurrentCanvas move blinktags [$CurrentCanvas cget -width] 0
	}
	if [llength $args] {
	    $CurrentCanvas move txtags $halfwidth 0
	}
}

proc movein {args} {
	global BlinkOff CurrentCanvas halfwidth
	if {$BlinkOff != 0} {
		$CurrentCanvas move blinktags -[$CurrentCanvas cget -width] 0
	}
	if [llength $args] {
	    $CurrentCanvas move txtags -$halfwidth 0
	}
}

proc clearRegion {line column width height bg border} {
	global charwidth charheight CurrentCanvas
	
	movein
    set x2 [expr [set x1 [expr ($line - 1) * $charheight + 2]] + $height * $charheight - 3]
    set y2 [expr [set y1 [expr ($column - 1) * $charwidth + 2]] + $width * $charwidth - 3]
    setTmptag overlapping $y1 $x1 $y2 $x2
	if $border {
    	setTmptag enclosed [expr $y1 - 2] [expr $x1 - 2] [expr $y2 + 2] [expr $x2 + 2]
	}
	$CurrentCanvas dtag cursortag tmptags
	setFgColor tmptags $bg
	setBgColor tmptags $bg
	$CurrentCanvas dtag tmptags blinktags
	$CurrentCanvas dtag bgtags tmptags
	catch {$CurrentCanvas itemconfigure tmptags -text " "}
	$CurrentCanvas dtag txtags tmptags
	$CurrentCanvas delete tmptags
	moveout
}

# Draw line from line / column length len characters horizontal or vertical, depending on orientation. Line/column start at 0/0
# orientation is 0 (left), 1 (top), 2 (right) or 3 (bottom).
proc drawLine {line column orientation len fg blink} {
    global charwidth charheight CurrentCanvas

    movein
    set x1 [expr $line * $charheight + [lindex "1 1 1 $charheight" $orientation] + 1]
    set y1 [expr $column * $charwidth + [lindex "1 1 $charwidth 1" $orientation] + 1]
    for {set i 0} {$i < $len} {incr i} {
        if {[lindex "1 0 1 0" $orientation] == 1} {
            # vertical line
            set tg [format "O%dL%dC%d" $orientation [expr $line + $i] $column]
            catch {$CurrentCanvas delete $tg}
            set id [$CurrentCanvas create line $y1 $x1 $y1 [expr $x1 + $charheight] -tag "boxtags $tg" -width 1]
            incr x1 $charheight
        } {
            # horizontal line
            set tg [format "O%dL%dC%d" $orientation $line [expr $column + $i]]
            catch {$CurrentCanvas delete $tg}
            set id [$CurrentCanvas create line $y1 $x1 [expr $y1 + $charwidth] $x1 -tag "boxtags $tg" -width 1]
            incr y1 $charwidth
        }
        setFgColor $tg $fg
        if $blink {
            $CurrentCanvas addtag blinktags withtag $id
        }
    }
    moveout
}

proc drawBox {line column width height fg blink} {
    global CurrentCanvas

    incr line -1
    incr column -1
    drawLine $line $column 0 $height $fg $blink
    drawLine $line $column 1 $width  $fg $blink
    drawLine $line [expr $column + $width - 1] 2 $height $fg $blink
    drawLine [expr $line + $height - 1] $column 3 $width $fg $blink
}

# Communication
pack [ttk::labelframe .co.port -text "Port"] -fill y -side left
set Port 43434
pack [ttk::entry .co.port.e -textvariable Port -width 5] -fill both -side left
if {[catch {console hide}] == 0} {
    pack [ttk::button .co.debug -text "Debug On" -command debug] -fill y -side left
}
pack [ttk::button .co.startstop -text "Start" -command startstop] -expand 1 -side left -fill both

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

set SFd ""
set Fd ""

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
	global SFd Fd Canvases
	
	if {$Fd == ""} {
		puts "Connect from $addr:$port accepted, fd = $fd"
		set Fd $fd
		fconfigure $fd  -blocking 0 -buffering none -translation binary -encoding binary
		fileevent $Fd readable {processInput}
		for {set cv 1} {$cv < [llength $Canvases]} {incr cv} {
			global CanvasActive$cv
			puts -nonewline $Fd [format "%02dO%1d\3" $cv [set CanvasActive$cv]]
			if {[set CanvasActive$cv]} {
			    puts [sourceText "═ (online $cv)"]
			} {
			    puts [sourceText "╪ (offline $cv)"]
			}
		}
	} {
		puts "Connect from $addr:$port rejected"
		close $fd
	}
}

# Command handler. Each command will be terminated with ETX (03h). Commands must be formatted as follows:
#	The following format specifications follow the format of Tcl/Tk format command (which is compatible to
#	the printf format of C):
#		%02dVC								Clear video. Parameter is the unit number (1 - 5)
#		%02dRC%02d%02d%02d%02d%02d%1d		Clear region. Parameter are unit number, line (1-19), column (1-24), width,
#											height, background color (0-15) and clear border flag (0|1, 1 = clear border).
#		%02dDB%02d%02d%02d%02d%02d%1d	    Draw box. Box will be drawn around the specified region. The parameters
#											are: unit number, line, column, width, height, color (0 - 15) and
#											blinking flag (0 or 1, 1 means blinking border)
#       %02dBL%02d%02d%1d%02d%02d%1d        Draw border line. border line will be drawn as specified. The parameters are:
#                                           unit number, line, column, orientation (0 - 3), length, color (0 - 15) and
#                                           blinking flag. Orientation: 0 (left), 1 (top), 2 (right) or 3 (bottom).
#		%02dDT%02d%02d%02d%02d%1d%s			Display text. Displays text starting at given position. Parameters are:
#											unit number, line, column, foreground color, background color, blinking
#											flag and text to be displayed, encoded using UTF-8.
#		%02dSA%02d%02d%02d%02d%02d%02d%1d%1dSet attributes for specified region. The parameters are: unit number, line,
#                                           column, width, height, foreground color (0-15), background color (0-15),
#                                           blinking flag and clear border flag.
#       %02dSC%02d%02d%02d                  Set cursor. Blinking line will be drawn at the specified position. The
#                                           parameters are: unit number, line, column, color. To delete the cursor, set
#                                           line and column to 0.
#		%02dST%02d%02d%1d%d					Specify clock. The text at the given position must match the specified clock
#											format. The parameters are: unit number, line, column, type, seconds.
#											type must be 0 (M:SS), 1 (MM:SS), 2 (HH:MM:SS, 12 hours) or 3 (HH:MM:SS, 24 hours).
#											Seconds must be any number and will be added to the currently displayed time,
#											0 stops the clock.
#       %02dRT                              Read time from clock. Parameter is the unit number (1 - 5)

# Frames sent by the simulator will be terminated with ETX (03h). They have one of the following formats:
#       %02dO%1d                            Status message. Parameters are unit and online flag (0 or 1), 0 means offline
#                                           and 1 means online.
#       %02dD%02d%02d                       Click down message. Parameters are unit, column (1 - 25) and line (1 - 20).
#       %02dU%02d%02d                       Click release message. Parameters are unit, column (1 - 25) and line (1 - 20).
#                                           If the focus has left the corresponding display area before releasing, the
#                                           closest valid line and column will be returned instead.
#       %02dT%s                             Current clock time. Parameters are unit and time, where
#                                           unit is the unit number (1 - 5) and time the clock time of the corresponding
#                                           clock or ":" when the clock is not running.

set Buffer {}
proc processInput {} {
	global Fd Buffer Canvases CurrentCanvas DispWidth DispHeight DispCount
	
	if [eof $Fd] {
		fileevent $Fd readable ""
		close $Fd
		set Fd ""
		puts "Connection disconnected. Waiting for connect"
	} {
		set Buffer [lindex [set commands [split "$Buffer[read $Fd]" "\3"]] end]
		set commands [lrange $commands 0 end-1]
		foreach cmd $commands {
			puts -nonewline [format [sourceText "◄ %s: "] [encoding convertfrom utf-8 $cmd]]
			if {[set cnt [scan $cmd "%02d%2s%s" unit what data]] < 1 || $unit < 1 || $unit > $DispCount} {
				puts "Invalid command format"
			} {
				upvar #0 CanvasActive$unit unitactive
				set CurrentCanvas [lindex $Canvases $unit]
				set data [string range $cmd 4 end]
				if {$unitactive == 0 } {
					puts "Unit inactive"
				} elseif {$cnt == 2 && $what == "VC"} {
					# Clear video
					clearVideo $unit
					puts "OK"
				} elseif {$cnt == 2 && $what == "RT"} {
				    # Read clock time
				    sendClock $unit
				    puts "OK"
				} elseif {$cnt == 3} {
					switch $what {
						"RC" {
							if {[set cnt [scan $data "%02d%02d%02d%02d%2d%1d" line column width height color border]] == 6} {
								# Clear region
								if {$line > 0 && $column > 0 && $width > 0 && $height > 0 && $line + $height - 1 <= $DispHeight && $column + $width - 1 <= $DispWidth && $color >= 0 && $color <= 15 && $border >= 0 && $border <= 1} {
									clearRegion $line $column $width $height $color $border
									puts "OK"
								} {
									puts "Invalid parameter"
								}
							}
						}
						"BL" {
							if {[set cnt [scan $data "%02d%02d%1d%02d%02d%1d" line column orientation length fgcol blink]] == 6} {
								# Draw border line
		                        catch {lindex "1 $length 1 $length" $orientation} width
		                        catch {lindex "$length 1 $length 1" $orientation} height
								if {$line > 0 && $column > 0 && $length > 0 && $orientation >= 0 && $orientation <= 3 && $line + $height - 1 <= $DispHeight && $column + $width - 1 <= $DispWidth && $fgcol >= 0 && $fgcol <= 15 && $blink >= 0 && $blink <= 1} {
									drawLine [expr $line - 1] [expr $column - 1] $orientation $length $fgcol $blink
									puts "OK"
								} {
									puts "Invalid parameter"
								}
							}
						}
						"DB" {
							if {[set cnt [scan $data "%02d%02d%02d%02d%02d%1d" line column width height fgcol blink]] == 6} {
								# Draw box
								if {$line > 0 && $column > 0 && $width > 0 && $height > 0 && $line + $height - 1 <= $DispHeight && $column + $width - 1 <= $DispWidth && $fgcol >= 0 && $fgcol <= 15 && $blink >= 0 && $blink <= 1} {
									drawBox $line $column $width $height $fgcol $blink
									puts "OK"
								} {
									puts "Invalid parameter"
								}
							}
						}
						"DT" {
							if {[string length $data] > 9 && [set cnt [scan [string range $data 0 8] "%02d%02d%02d%2d%1d" line column fgcol bgcol blink]] == 5} {
								# Display text
								set text [string range $data 9 end]
								if {$line > 0 && $column > 0 && $line <= $DispHeight && $column + [string length $text] - 1 <= $DispWidth && $fgcol >= 0 && $fgcol <= 15 && $bgcol >= 0 && $bgcol <= 15 && $blink >= 0 && $blink <= 1} {
									setText $line $column $text $fgcol $bgcol $blink
									puts "OK"
								} {
									puts "Invalid parameter"
								}
							}
						}
						"SC" {
						    if {[set cnt [scan $data "%02d%02d%02d" line column fgcol]] == 3} {
                                # Set cursor
						        if {$line > 0 && $line <= $DispHeight && $column > 0 && $column <= $DispWidth && $fgcol >= 0 && $fgcol <= 15} {
						            setCursor $line $column $fgcol
                                    puts "OK"
						        } elseif {$line == 0 && $column == 0} {
						            setCursor 0 0 0
                                    puts "OK"
						        } {
									puts "Invalid parameter"
						        }
						    }
						}
						"SA" {
							if {[set cnt [scan $data "%02d%02d%02d%02d%02d%2d%1d%1d" line column width height fgcol bgcol blink border]] == 8} {
								# Set attribute
								if {$line > 0 && $column > 0 && $width > 0 && $height > 0 && $line + $height - 1 <= $DispHeight && $column + $width - 1 <= $DispWidth && $fgcol >= 0 && $fgcol <= 15 && $bgcol >= 0 && $bgcol <= 15 && $blink >= 0 && $blink <= 1 && $border >= 0 && $border <= 1} {
									setAttributes $line $column $width $height $fgcol $bgcol $blink $border
									puts "OK"
								} {
									puts "Invalid parameter"
								}
							}
						}
						"ST" {
							if {[set cnt [scan $data "%02d%02d%1d%d" line column type seconds]] == 4} {
								# Set clock at specified position
								if {$line > 0 && $column > 0 && [setClock $type $line $column $seconds]} {
									puts "OK"
								} elseif {$line == 0 && $column == 0} {
								    clearClock
								    puts "OK"
								} {
									puts "Invalid parameter"
								}
							}
						}
						default {
							puts "Command $what unknown"
						}
					}
				} {
					puts "Command $what unknown"
				}
			}
		}
	}
}
