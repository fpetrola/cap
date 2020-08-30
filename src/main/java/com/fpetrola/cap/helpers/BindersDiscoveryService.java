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

	public BindersDiscoveryService() {
	}

	public List<Binder> findBinders() {
		List<Binder> pullers = new ArrayList<Binder>();
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(basePackage).scan()) {

			ClassInfoList binderClasses = scanResult.getClassesImplementing(BidirectionalBinder.class.getName()).filter(filter -> !filter.isInterface());

			binderClasses.loadClasses().forEach(puller -> {
				try {
					Constructor<?>[] constructors = puller.getConstructors();
					pullers.add((Binder) constructors[0].newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
			});
		}

		return pullers;
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