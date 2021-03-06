package com.test.grelu.mapper.core.mock;




import com.grelu.mapper.core.PureObject;

import java.util.Date;

public class EntityMock implements PureObject {

	public String firstname;

	public String lastname;

	public Date birthday;

	@Override
	public Object clone() {
		EntityMock o = null;
		try {
			o = (EntityMock) super.clone();
			o.firstname = firstname;
			o.lastname = lastname;
			o.birthday = birthday;
			return o;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}
}
