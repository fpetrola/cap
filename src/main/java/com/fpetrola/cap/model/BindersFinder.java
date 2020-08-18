package com.fpetrola.cap.model;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JFrame;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public class BindersFinder extends JFrame {

	protected List<BidirectionalBinder> pullers;
	protected List<DeveloperModel> developerModels;

	public BindersFinder() throws HeadlessException {
		super();
	}

	public BindersFinder(GraphicsConfiguration gc) {
		super(gc);
	}

	public BindersFinder(String title) throws HeadlessException {
		super(title);
	}

	public BindersFinder(String title, GraphicsConfiguration gc) {
		super(title, gc);
	}

	protected void findBinders() {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages("com.fpetrola.cap").scan()) {
	
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