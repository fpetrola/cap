package com.fpetrola.cap;

import org.junit.jupiter.api.Test;

import com.fpetrola.cap.model.JPAEntityMappingWriter;
import com.fpetrola.cap.model.ORMEntityMapping;
import com.fpetrola.cap.model.ORMMappingYamlReader;

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
