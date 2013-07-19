/**
 * BSD Licence Copyright (c) 2009, Science & Technology Facilities Council
 * (STFC) All rights reserved. Redistribution and use in source and binary
 * forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - Neither the name of the Science & Technology
 * Facilities Council (STFC) nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package ndg.services.newmoon.velocity.python;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.ToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mnagni
 * 
 */
public class NmVelocity {

	private final VelocityEngine ve = new VelocityEngine();
	private final static NmVelocity instance = new NmVelocity();
	private final ToolManager tm = new ToolManager();

	String packageBase = NmVelocity.class.getClassLoader().getResource(".").getPath();

	private final Logger logger = LoggerFactory.getLogger(NmVelocity.class);

	private NmVelocity() {
		ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");

		ve.setProperty("runtime.log.logsystem.log4j.logger", logger.getName());

		/*
		 * initialize the engine
		 */
		try {
			Properties prop = new Properties();
			InputStream in = NmVelocity.class.getClassLoader().getResourceAsStream("velocity.properties");
			prop.load(in);
			in.close();
			ve.init(prop);
			tm.autoConfigure(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static NmVelocity getInstance() {
		return instance;
	}

	public Context createContext() {
		return tm.createContext();
	}
	
	public String fillTemplate(Context vcontext, String templateName) {
		vcontext.put("tab", "\t"); //four spaces instead of a TAB
		vcontext.put("nl", "\n");
		vcontext.put("vh", NmVelocityHelper.class);
		Template template = loadTemplate(templateName);
		StringWriter sw = new StringWriter();
		try{
		template.merge(vcontext, sw);
		} catch (Exception e) {
			logger.error("Error!!", e);
		}
		sw.flush();
		return sw.getBuffer().toString();
	}

	private Template loadTemplate(String resource) {
		Template template = null;
		try {
			if (!ve.resourceExists(resource)) {
				logger.error("Can't find template");
			} else {
				template = ve.getTemplate(resource);
			}
		} catch (ResourceNotFoundException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Velocity error.", e);
			}
		} catch (ParseErrorException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Velocity error.", e);
			}
		} catch (MethodInvocationException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Velocity error.", e);
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("Velocity error.", e);
			}
		}
		return template;
	}
}
