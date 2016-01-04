package com.sirolf2009.husk.outputconverter;

import java.util.Arrays;

import com.sirolf2009.husk.OutputConverter;

public class OutputConverterArray implements OutputConverter {

	@Override
	public String convert(Object object) {
		if(object.getClass().isArray()) {
			return Arrays.deepToString((Object[]) object);
		}
		return null;
	}

}
