package com.fpetrola.cap.model.developer;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;

public class ORMMappingYamlReader {

	static String lastMd5 = "";

	public ORMEntityMapping read() {

		try {
			String fileName = "/com/fpetrola/cap/mapping.yml";
			YamlMapping team = Yaml.createYamlInput(ORMMappingYamlReader.class.getResourceAsStream(fileName)).readYamlMapping();

			String md5 = createMd5(team.toString());

			if (!lastMd5.equals(md5)) {

				YamlMapping yamlMapping = team.yamlMapping("entity-mapping");
				YamlSequence propertiesMapping = yamlMapping.yamlSequence("properties");

				String tableName = yamlMapping.string("table-name");

				Class<?> mappedClass = Class.forName(yamlMapping.string("entity-class"));
				Collection<PropertyMapping> propertyMappings = new ArrayList<PropertyMapping>();

				propertiesMapping.forEach(action -> {
					YamlMapping asMapping = action.asMapping();
					YamlNode object = (YamlNode) asMapping.keys().toArray()[0];
					String propertyName = object.asScalar().value();
					YamlMapping property = asMapping.yamlMapping(propertyName);

					String columnName = property.string("column-name");
					String mappingType = property.string("mapping-type");
					PropertyMappingType propertyMappingType = mappingType != null ? PropertyMappingType.valueOf(mappingType) : null;
					propertyMappings.add(new PropertyMapping(propertyName, columnName, "String", propertyMappingType));

				});

				lastMd5 = md5;
				//TODO: fix entitymodel creation
				return new ORMEntityMapping(new EntityModel(tableName, new ArrayList<>()), mappedClass.getName(), tableName, propertyMappings);
			} else
				return null;

		} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private String createMd5(String string) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(string.getBytes());

		byte[] digest = md.digest();
//		String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
		return digest.toString();
	}
}
