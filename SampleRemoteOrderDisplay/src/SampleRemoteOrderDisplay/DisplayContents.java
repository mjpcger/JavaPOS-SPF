/*
 * Copyright 2019 Martin Conrad
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package SampleRemoteOrderDisplay;

import jpos.RemoteOrderDisplayConst;

/**
 * Class that holds display contents for all remote order display units supported by sample device
 */
public class DisplayContents {
    /**
     * Pseudo color value for non-present border lines.
     */
    final static int NOT_PRESENT = -1;

    /**
     * Value to be aded to any color value if that color is blinking
     */
    final static int BLINKING = 16;

    /**
     * Number of supported display units
     */
    final static int UNITCOUNT = 5;

    /**
     * Number of supported lines, equal for all display units
     */
    final static int LINECOUNT = 20;

    /**
     * Number of supported columns, equal for all display units
     */
    final static int COLUMNCOUNT = 25;

    final static int BUFFERIDLIMIT = 1;

    /**
     * Unit contains all relevant information about the contents of up to 5 display units.
     */
    DisplayUnit[] Unit = new DisplayUnit[] {
            new DisplayUnit(),
            new DisplayUnit(),
            new DisplayUnit(),
            new DisplayUnit(),
            new DisplayUnit()
    };

    /**
     * Holds value and colors of a display position.
     */
    static class CharAttributes {
        /**
         * Character stored at this display position.
         */
        char Value = ' ';

        /**
         * Foreground color of the character at this display position.
         */
        int ForegroundColor = RemoteOrderDisplayConst.ROD_ATTR_FG_BLACK;

        /**
         * Background color of the character at this display position.
         */
        int BackgroundColor = RemoteOrderDisplayConst.ROD_ATTR_FG_GRAY|RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;

        /**
         * Color of the left border at this display position, if present. NOT_PRESENT otherwise.
         */
        int LeftBorderColor = NOT_PRESENT;

        /**
         * Color of the top border at this display position, if present. NOT_PRESENT otherwise.
         */
        int TopBorderColor = NOT_PRESENT;

        /**
         * Color of the right border at this display position, if present. NOT_PRESENT otherwise.
         */
        int RightBorderColor = NOT_PRESENT;

        /**
         * Color of the bottom border at this display position, if present. NOT_PRESENT otherwise.
         */
        int BottomBorderColor = NOT_PRESENT;
    }

    /**
     * Display unit specific data containing relevant attributes
     */
    static class DisplayUnit {
        /**
         * Cursor row.
         */
        int CursorRow = 0;

        /**
         * Cursor column.
         */
        int CursorColumn = 0;

        /**
         * Cursor active flag. True if cursor is visible, false otherwise.
         */
        boolean CursorActive = false;

        /**
         * Clock type. Valid only between clock start and clock stop.
         */
        int ClockType = -1;

        /**
         * Row of clock.
         */
        int ClockRow = 0;

        /**
         * Start column of clock.
         */
        int ClockColumn = 0;

        /**
         * Tick when the clock has been suspended.
         */
        long ClockSuspendTick = 0;

        /**
         * Allowed lower limits for clock value strings
         */
        static char[][] ClockValueLowerBoarder = new char[][]{
                new char[]{'0', ':', '0', '0'},
                new char[]{0, ':', '0', '0'},
                new char[]{1, ':', '0', '0', ':', '0', '0'},
                new char[]{0, ':', '0', '0', ':', '0', '0'}
        };

        /**
         * Allowed upper limits for clock value strings
         */
        static char[][] ClockValueUpperBoarder = new char[][]{
                new char[]{'9', ':', '5', '9'},
                new char[]{59, ':', '5', '9'},
                new char[]{12, ':', '5', '9', ':', '5', '9'},
                new char[]{23, ':', '5', '9', ':', '5', '9'}
        };

        /**
         * Between clock start and clock stop, ClockActive remains true as long as it won't be overwritten by the
         * application.
         */
        boolean ClockActive = false;

        /**
         * Video save buffer, one per ID and one reserved (e.g. for clock handling)
         */
        CharAttributes[][][] SaveBuffer = new CharAttributes[BUFFERIDLIMIT + 1][][];

        /**
         * Character attributes for 20 lines with 25 columns, each.
         */
        CharAttributes[][] Attribute = new CharAttributes[][]{
                // Line 1
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 2
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 3
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 4
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 5
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 6
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 7
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 8
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 9
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 10
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 11
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 12
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 13
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 14
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 15
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 16
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 17
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 18
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 19
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                },
                // Line 20
                new CharAttributes[]{
                        new CharAttributes(),   // 1
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 5
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 10
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 15
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),   // 20
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes(),
                        new CharAttributes()   // 25
                }
        };
    }
}
