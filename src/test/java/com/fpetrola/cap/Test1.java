package com.fpetrola.cap;

import org.junit.jupiter.api.Test;

import com.fpetrola.cap.model.binders.JPAEntityMappingWriter;
import com.fpetrola.cap.model.binders.ORMMappingYamlReader;
import com.fpetrola.cap.model.developer.ORMEntityMapping;

class Test1 {

	@Test
	void test() {

		while (true) {

			try {
				ORMEntityMapping ormEntityMapping = new ORMMappingYamlReader().read();
				if (ormEntityMapping != null)
					new JPAEntityMappingWriter(ormEntityMapping).write();
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
	}

}
