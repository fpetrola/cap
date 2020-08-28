package com.fpetrola.cap.model.binders;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.developer.DeveloperModel;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public class BindersFinder {

	public List<BidirectionalBinder> pullers= new ArrayList<BidirectionalBinder>();
	public List<DeveloperModel> developerModels= new ArrayList<DeveloperModel>();

	public BindersFinder() {
	}

	public void findBinders() {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages("com.fpetrola.cap").scan()) {

			ClassInfoList binderClasses = scanResult.getClassesImplementing(BidirectionalBinder.class.getName()).filter(filter -> !filter.isInterface());

			binderClasses.loadClasses().forEach(puller -> {
				try {
					Constructor<?>[] constructors = puller.getConstructors();
					pullers.add((BidirectionalBinder) constructors[0].newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
			});

			ClassInfoList developerModelsClasses = scanResult.getClassesImplementing(DeveloperModel.class.getName()).filter(filter -> !filter.isInterface());

			developerModelsClasses.loadClasses().forEach(puller -> {
				try {
					Constructor<?>[] constructors = puller.getConstructors();
					developerModels.add((DeveloperModel) constructors[0].newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
			});

		}
	}

}