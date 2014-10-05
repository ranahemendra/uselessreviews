package com.totspot.uselessreviews;

import com.parse.ParseException;

public interface LoginListener {
	public void success();
	public void failed(ParseException e);
}
