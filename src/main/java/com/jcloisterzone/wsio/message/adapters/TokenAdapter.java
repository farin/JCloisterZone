package com.jcloisterzone.wsio.message.adapters;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jcloisterzone.game.Token;

public class TokenAdapter extends TypeAdapter<Token> {

	@Override
	public void write(JsonWriter out, Token value) throws IOException {
		out.beginArray();
		out.value(value.getClass().getName());
		out.value(value.name());
		out.endArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Token read(JsonReader in) throws IOException {
		in.beginArray();
		String clsName = in.nextString();
		String tokenName = in.nextString();
		in.endArray();
		try {
			Class<? extends Token> cls = (Class<? extends Token>) Class.forName(clsName, true, Token.class.getClassLoader());
			return (Token) cls.getMethod("valueOf", String.class).invoke(null, tokenName);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | ClassNotFoundException e) {
			throw new IOException(e);
		}

	}

}
