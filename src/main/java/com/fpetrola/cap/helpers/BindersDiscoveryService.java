package com.fpetrola.cap.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.developer.DeveloperModel;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public class BindersDiscoveryService {

	private static final String basePackage = "com.fpetrola.cap";
	private List<Class<?>> loadClasses;

	public BindersDiscoveryService() {
	}

	@SuppressWarnings("rawtypes")
	public List<Binder> findBinders() {
		List<Binder> instances = new ArrayList<Binder>();

		if (loadClasses == null) {
			ClassInfoList binderClasses;
			try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(basePackage).scan()) {
				binderClasses = scanResult.getClassesImplementing(BidirectionalBinder.class.getName()).filter(filter -> !filter.isInterface());
				loadClasses = binderClasses.loadClasses();
			}
		}

		loadClasses.forEach(puller -> {
			try {
				Constructor<?>[] constructors = puller.getConstructors();
				instances.add((Binder) constructors[0].newInstance());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		});

		return instances;
	}

	public List<DeveloperModel> findModels() {
		List<DeveloperModel> developerModels = new ArrayList<DeveloperModel>();
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(basePackage).scan()) {

			ClassInfoList developerModelsClasses = scanResult.getClassesImplementing(DeveloperModel.class.getName()).filter(filter -> !filter.isInterface());

			developerModelsClasses.loadClasses().forEach(puller -> {
				try {
					Constructor<?>[] constructors = puller.getConstructors();
					developerModels.add((DeveloperModel) constructors[0].newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
			});
		}

		return developerModels;
	}

}