package com.fpetrola.cap;

import java.util.List;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import com.fpetrola.cap.model.BidirectionalBinder;
import com.fpetrola.cap.model.BindingsDashboardData;
import com.fpetrola.cap.model.DeveloperModel;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodInfoList;
import io.github.classgraph.MethodTypeSignature;
import io.github.classgraph.ReferenceTypeSignature;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeArgument;

public class BindingsDashboard extends JFrame {

	private static final int WIDTH = 220;
	private mxGraphComponent graphComponent;
	private mxGraph graph;
	private BindingsDashboardData data = new BindingsDashboardData();

	public BindingsDashboard() {
		super("Hello, World!");

		graph = new mxGraph() {
			private int y = 20;

			@Override
			public boolean isValidConnection(Object source, Object target) {
				try {
					if (target != null) {
						Object value = ((mxCell) source).getValue();

						Object value2 = ((mxCell) target).getValue();
						if (value2 instanceof BidirectionalBinder) {
							final BidirectionalBinder bidirectionalBinder = (BidirectionalBinder) value2;

							final Supplier valueSupplier = () -> value instanceof Supplier ? ((Supplier) value).get() : value;
							List pull = bidirectionalBinder.pull(valueSupplier.get());

							this.getModel().beginUpdate();

							List<Supplier> suppliers = bidirectionalBinder.getSuppliers(valueSupplier);

							for (int i = 0; i < pull.size(); i++) {
								Object developerModel = pull.get(i);

								Object v1 = this.insertVertex(getDefaultParent(), null, developerModel.toString(), 800, y += 50, WIDTH, 30);
								mxCell mxCell = (mxCell) v1;
								graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "#ffff00", new Object[] { mxCell });
								mxCell.setValue(suppliers.get(i));

								Object insertEdge = insertEdge(getDefaultParent(), null, "creates", target, v1);
								graph.setCellStyle("strokeColor=#CCCCCC;dashed=true", new Object[] { insertEdge });
							}

							insertEdge(getDefaultParent(), null, "input", source, target);

							this.getModel().endUpdate();
							return true;
						}
					}
				} catch (Exception e) {
					return false;
				}
				return super.isValidConnection(target, source);
			}

			public boolean isValidDropTarget(Object cell, Object[] cells) {
				return super.isValidDropTarget(cell, cells);
			}
		};

		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();
		try {

			for (BidirectionalBinder puller2 : data.pullers) {

				mxCell insertVertex = (mxCell) graph.insertVertex(parent, null, "BiBinder:\n" + puller2.getClass().getSimpleName(), 240, 150, WIDTH, 30);
				insertVertex.setValue(puller2);
			}
			for (DeveloperModel puller2 : data.developerModels) {

				mxCell insertVertex = (mxCell) graph.insertVertex(parent, null, puller2.getClass().getSimpleName(), 240, 150, WIDTH, 30);
				insertVertex.setValue(puller2);
				System.out.println(insertVertex);
			}
		} finally {
			graph.getModel().endUpdate();
		}

		graphComponent = new mxGraphComponent(graph);

		getContentPane().add(graphComponent);

		morphGraph();

		SwingWorker<Boolean, Integer> swingWorker = new SwingWorker<Boolean, Integer>() {

			@Override
			protected Boolean doInBackground() throws Exception {

				while (true) {
					Thread.sleep(1000);

					try {
						Object[] cells = graph.getChildVertices(graph.getDefaultParent());
						for (int i = 0; i < cells.length; i++) {

							mxCell mxCell = (mxCell) cells[i];
							Object value = mxCell.getValue();

							if (value instanceof Supplier) {
								Supplier supplier = (Supplier) value;
								Object object = supplier.get();
							}
						}
						graph.refresh();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		};

		swingWorker.execute();
	}

	private void t1(ScanResult scanResult) {
		ClassInfoList filtered = scanResult.getAllClasses().filter(classInfo -> {
			MethodInfoList methodInfoList = classInfo.getMethodInfo();
			for (MethodInfo methodInfo : methodInfoList) {
				MethodTypeSignature typeDescriptor = methodInfo.getTypeSignatureOrTypeDescriptor();

				if (typeDescriptor.getResultType() instanceof ClassRefTypeSignature) {
					ClassRefTypeSignature classRefTypeSignature = (ClassRefTypeSignature) typeDescriptor.getResultType();
					List<TypeArgument> stringWithSimpleNames = classRefTypeSignature.getTypeArguments();

					ReferenceTypeSignature typeSignature = stringWithSimpleNames.get(0).getTypeSignature();
					System.out.println(typeDescriptor);

				}
			}

			return true;
		});
	}

	private void morphGraph() {
		// define layout
		mxIGraphLayout layout = new mxCircleLayout(graph);

		// layout using morphing
		graph.getModel().beginUpdate();
		try {
			layout.execute(graph.getDefaultParent());
		} finally {
			mxMorphing morph = new mxMorphing(graphComponent, 20, 1.5, 20);

			morph.addListener(mxEvent.DONE, new mxIEventListener() {

				@Override
				public void invoke(Object arg0, mxEventObject arg1) {
					graph.getModel().endUpdate();
					// fitViewport();
				}

			});

			morph.startAnimation();
		}

	}

	public static void main(String[] args) {
		BindingsDashboard frame = new BindingsDashboard();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 700);
		frame.setVisible(true);
	}

}