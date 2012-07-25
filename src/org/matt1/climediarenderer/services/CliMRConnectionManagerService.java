/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.matt1.climediarenderer.services;

import java.util.logging.Logger;

import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.ProtocolInfo;

public class CliMRConnectionManagerService extends ConnectionManagerService {

    final private static Logger log = Logger.getLogger(CliMRConnectionManagerService.class.getName());

    /**
     * Basic MP3 support only for now...
     */
    public CliMRConnectionManagerService() {
      
                sinkProtocolInfo.add(new ProtocolInfo("audio/mpeg3"));
          
    }

}
