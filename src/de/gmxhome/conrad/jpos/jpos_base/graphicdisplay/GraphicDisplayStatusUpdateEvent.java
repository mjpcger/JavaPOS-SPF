/*
 * Copyright 2023 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.graphicdisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Status update event implementation for GraphicDisplay devices.
 */
public class GraphicDisplayStatusUpdateEvent extends JposStatusUpdateEvent {
    private String URL;

    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (graphicdisplay.)GraphicDisplayService object.
     * @param state  Status, see UPOS specification.
     */
    public GraphicDisplayStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
        URL = null;
    }

    /**
     * Constructor, Parameters passed to base class unchanged. For use in combination with URL load status change.
     *
     * @param source Source, for services implemented with this framework, the (graphicdisplay.)GraphicDisplayService object.
     * @param state  Status, see UPOS specification.
     * @param url    URL to be set in property URL in case of a URL load status change.
     */
    public GraphicDisplayStatusUpdateEvent(JposBase source, int state, String url) {
        super(source, state);
        URL = url;
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        GraphicDisplayService srv = (GraphicDisplayService) getSource();
        switch (getStatus()) {
            case GraphicDisplayConst.GDSP_SUE_START_LOAD_WEBPAGE:
                srv.UrlLoading = true;
                break;
            case GraphicDisplayConst.GDSP_SUE_FINISH_LOAD_WEBPAGE:
            case GraphicDisplayConst.GDSP_SUE_CANCEL_LOAD_WEBPAGE:
                srv.UrlLoading = false;
                break;
            case GraphicDisplayConst.GDSP_SUE_START_PLAY_VIDEO:
                srv.VideoPlaying = true;
                break;
            case GraphicDisplayConst.GDSP_SUE_STOP_PLAY_VIDEO:
                srv.VideoPlaying = false;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void setLateProperties() {
        super.setStatusProperties();
        GraphicDisplayProperties props = (GraphicDisplayProperties) getPropertySet();
        Integer loadstate = props.LoadStatus;
        switch (getStatus()) {
            case GraphicDisplayConst.GDSP_SUE_START_LOAD_WEBPAGE:
                loadstate = GraphicDisplayConst.GDSP_LSTATUS_START;
                break;
            case GraphicDisplayConst.GDSP_SUE_FINISH_LOAD_WEBPAGE:
                loadstate = GraphicDisplayConst.GDSP_LSTATUS_FINISH;
                break;
            case GraphicDisplayConst.GDSP_SUE_CANCEL_LOAD_WEBPAGE:
                loadstate = GraphicDisplayConst.GDSP_LSTATUS_CANCEL;
                break;
            default:
                return;
        }
        if (loadstate != null && !loadstate.equals(props.LoadStatus)) {
            props.LoadStatus = loadstate;
            ((GraphicDisplayService) getSource()).logSet("LoadStatus");
        }
        if (URL != null && !URL.equals(props.URL)) {
            props.URL = URL;
            ((GraphicDisplayService) getSource()).logSet("URL");
        }
    }
    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case GraphicDisplayConst.GDSP_SUE_START_IMAGE_LOAD:
                return "GraphicDisplay Start Load Image";
            case GraphicDisplayConst.GDSP_SUE_END_IMAGE_LOAD:
                return "GraphicDisplay End Load Image";
            case GraphicDisplayConst.GDSP_SUE_START_LOAD_WEBPAGE:
                return "GraphicDisplay Start Load Web Page" + (URL == null ? "" : " " + URL);
            case GraphicDisplayConst.GDSP_SUE_FINISH_LOAD_WEBPAGE:
                return "GraphicDisplay Finish Load Web Page " + (URL == null ? "" : " " + URL);
            case GraphicDisplayConst.GDSP_SUE_CANCEL_LOAD_WEBPAGE:
                return "GraphicDisplay Cancel Load Web Page " + (URL == null ? "" : " " + URL);
            case GraphicDisplayConst.GDSP_SUE_START_PLAY_VIDEO:
                return "GraphicDisplay Start Play Video";
            case GraphicDisplayConst.GDSP_SUE_STOP_PLAY_VIDEO:
                return "GraphicDisplay Stop Play Video";
        }
        return "Unknown GraphicDisplay Status Change: " + getStatus();
    }
}
