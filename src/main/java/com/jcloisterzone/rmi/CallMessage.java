package com.jcloisterzone.rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CallMessage implements Serializable {

	private static final long serialVersionUID = 15L;

	protected String method;
	protected Object[] args;

	public CallMessage(Method method, Object[] args) {
		this(method.getName(), args);
	}

	public CallMessage(String method, Object[] args) {
		this.method = method;
		this.args = args;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}


	private void writeObject(ObjectOutputStream s) throws IOException {
		s.writeObject(method);
		s.writeObject(args);
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
		method = (String) s.readObject();
		args = (Object[]) s.readObject();
	}

	@Override
	public String toString() {
		return "CallMessage(" + method + ")";
	}

	public void call(Object target, Class<?> targetIF) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
		Method[] methods = targetIF.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(method)) {
				methods[i].invoke(target, args);
				return;
			}
		}
	}
}
