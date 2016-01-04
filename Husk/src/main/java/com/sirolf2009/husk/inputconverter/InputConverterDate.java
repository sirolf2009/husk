package com.sirolf2009.husk.inputconverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.sirolf2009.husk.InputConverter;

public class InputConverterDate implements InputConverter {

	private DateFormat format;

	public InputConverterDate() {
		this("dd/MM/yyyy-HH:mm:ss");
	}

	public InputConverterDate(String pattern) {
		format = new SimpleDateFormat(pattern);
	}

	@Override
	public Object convert(Object input, Class<?> requestedClass) {
		try {
			if(input instanceof String) {
				if(requestedClass.equals(Date.class)) {
					return format.parse((String) input);
				}
				if(requestedClass.equals(Calendar.class)) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(format.parse((String) input));
					return calendar;
				}
			} else if(input instanceof Long) {
				if(requestedClass.equals(Date.class)) {
					return new Date((long) input);
				}
				if(requestedClass.equals(Calendar.class)) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(new Date((long) input));
					return calendar;
				}
			}
		} catch(Exception e) {}
		return null;
	}

}
