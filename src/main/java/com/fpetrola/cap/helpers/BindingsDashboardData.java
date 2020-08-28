package com.fpetrola.cap.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.developer.DeveloperModel;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public class BindingsDashboardData {
	public List<BidirectionalBinder> pullers;
	public List<DeveloperModel> developerModels;

	public BindingsDashboardData() {
		pullers = new ArrayList<>();
		developerModels = new ArrayList<>();

		findBinders();
	}

	public void findBinders() {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages("com.fpetrola.cap.model").scan()) {

			ClassInfoList binderClasses = scanResult.getClassesImplementing(BidirectionalBinder.class.getName()).filter(filter -> !filter.isInterface());

			binderClasses.loadClasses().forEach(puller -> {
				try {
					Constructor<?>[] constructors = puller.getConstructors();
					pullers.add((BidirectionalBinder) constructors[0].newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			});

			ClassInfoList developerModelsClasses = scanResult.getClassesImplementing(DeveloperModel.class.getName()).filter(filter -> !filter.isInterface());

			developerModelsClasses.loadClasses().forEach(puller -> {
				try {
					Constructor<?>[] constructors = puller.getConstructors();
					developerModels.add((DeveloperModel) constructors[0].newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			});

		}
	}
}