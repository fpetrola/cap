package com.fpetrola.cap.config;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class Snippet {
	public static void main(String[] args) {

		Yaml yaml = new Yaml();
		InputStream inputStream = Snippet.class.getClassLoader().getResourceAsStream("cap2.yml");
		Map<String, Object> obj = yaml.load(inputStream);
		System.out.println(obj);
	}
}
