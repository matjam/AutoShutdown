/*
 * Copyright (c) 2011, chrome@stupendous.net
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 * 
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright 
 * notice, this list of conditions and the following disclaimer in 
 * the documentation and/or other materials provided with the 
 * distribution.
 * 
 * The names of its contributors may not be used to endorse or 
 * promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
 * SUCH DAMAGE.
 *  */

package net.stupendous.autoshutdown.misc;

import java.util.logging.Logger;

/* Convenience class to make it easier to log stuff with an optional format. */

public class Log {
	private final Logger log;
    private final String pluginName;
    
    public Log(String pluginName) {
    	this.pluginName = pluginName;
    	log = Logger.getLogger("Minecraft." + pluginName);
    }
    
    public void info(String msg) {
   		log.info(String.format("[%s] %s", pluginName, msg));
    }
    
    public void info(String format, Object... args) {
   		this.info(String.format(format, args));
     }

    public void warning(String msg) {
   		log.warning(String.format("[%s] %s", pluginName, msg));
    }
    
    public void warning(String format, Object... args) {
   		this.warning(String.format(format, args));
     }

    public void severe(String msg) {
   		log.severe(String.format("[%s] %s", pluginName, msg));
    }
    
    public void severe(String format, Object... args) {
   		this.severe(String.format(format, args));
     }
}
