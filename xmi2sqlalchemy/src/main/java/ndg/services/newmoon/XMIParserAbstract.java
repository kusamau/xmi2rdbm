/**
 * BSD Licence
 * Copyright (c) 2009, Science & Technology Facilities Council (STFC) All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   - Neither the name of the Science & Technology Facilities Council
 *     (STFC) nor the names of its contributors may be used to endorse or
 *     promote products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package ndg.services.newmoon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ndg.services.newmoon.NewmoonManager.NM_PARAM;
import ndg.services.newmoon.exception.NM_ParamException;

import org.apache.commons.io.FileUtils;

/**
 * @author mnagni
 *
 */
public class XMIParserAbstract {
	
	protected String getParam(Map<NewmoonManager.NM_PARAM, String> params, NM_PARAM paramName) throws NM_ParamException {
		try {
			return params.get(paramName);
		} catch (NullPointerException ex) {
			throw new NM_ParamException(ex);
		}		 
	}
	
	protected InputStream openInputStream(Map<NewmoonManager.NM_PARAM, String> params, NM_PARAM paramName, File baseDir) throws NM_ParamException {
		try {			
			return FileUtils.openInputStream(new File(baseDir, getParam(params, paramName)));	
		} catch (FileNotFoundException ex) {
			throw new NM_ParamException(paramName, ex);
		} catch (IOException ex) {
			throw new NM_ParamException(paramName, ex);
		} catch (NullPointerException ex) {
			throw new NM_ParamException(paramName, ex);
		}
		
	}
}
