/**
 * ESUP-Portail Commons - Copyright (c) 2006-2009 ESUP-Portail consortium.
 */
package org.esupportail.commons.web.servlet;

/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;
import javax.servlet.ServletContext;
import org.codehaus.xfire.XFire;

public class XFireServletController 
extends org.codehaus.xfire.transport.http.XFireServletController implements Serializable {
	
	private static final long serialVersionUID = -9085512373620430084L;

    public XFireServletController(
    		final XFire xfire, 
    		final ServletContext servletContext) {
        super(xfire, servletContext);
    }

}
