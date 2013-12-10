/*
 * Operation.java
 * 
 * Copyright 2013, Compusult Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
   
package net.compusult.owscontext;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class Operation {

	private String operationCode;
	private String operationMethod;
	private String type;
	private String requestURL;		// URI
	private Content payload;
	private Content result;
	private final List<Node> extensions;
	
	public Operation(){
		this.extensions = new ArrayList<Node>();
	}
	
	public String getOperationCode() {
		return operationCode;
	}

	public void setOperationCode(String operationCode) {
		this.operationCode = operationCode;
	}

	public String getOperationMethod() {
		return operationMethod;
	}

	public void setOperationMethod(String operationMethod) {
		this.operationMethod = operationMethod;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public Content getPayload() {
		return payload;
	}

	public void setPayload(Content payload) {
		this.payload = payload;
	}

	public Content getResult() {
		return result;
	}

	public void setResult(Content result) {
		this.result = result;
	}

	public List<Node> getExtensions() {
		return extensions;
	}

	@Override
	public String toString() {
		return "Operation [code=" + operationCode
				+ ", method=" + operationMethod + ", type=" + type
				+ ", requestURL=" + requestURL + ", payload=" + payload
				+ ", result=" + result + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((operationCode == null) ? 0 : operationCode.hashCode());
		result = prime * result
				+ ((operationMethod == null) ? 0 : operationMethod.hashCode());
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		result = prime * result
				+ ((requestURL == null) ? 0 : requestURL.hashCode());
		result = prime * result
				+ ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Operation other = (Operation) obj;
		if (operationCode == null) {
			if (other.operationCode != null)
				return false;
		} else if (!operationCode.equals(other.operationCode))
			return false;
		if (operationMethod == null) {
			if (other.operationMethod != null)
				return false;
		} else if (!operationMethod.equals(other.operationMethod))
			return false;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		if (requestURL == null) {
			if (other.requestURL != null)
				return false;
		} else if (!requestURL.equals(other.requestURL))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
