/**************************************************************************
 *  TW-APPS - Applications used by 3Worlds                                *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-APPS contains ModelMaker and ModelRunner, programs used to         *
 *  construct and run 3Worlds configuration graphs. All code herein is    *
 *  independent of UI implementation.                                     *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-APPS (3Worlds applications).                  *
 *                                                                        *
 *  TW-APPS is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-APPS is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-APPS.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
  **************************************************************************/

package au.edu.anu.twuifx.mm.editors.structure;

import java.util.List;
import org.apache.commons.text.WordUtils;
import au.edu.anu.rscs.aot.graph.AotNode;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.GraphState;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

/**
 * Author Ian Davies
 *
 * Date 13 Jan. 2019
 */
// TODO move to tw-uifx
public class StructureEditorfx extends StructureEditorAdapter {

	private ContextMenu cm;

	public StructureEditorfx(SpecifiableNode n, MouseEvent event) {
		super(n);
		cm = new ContextMenu();
		buildgui();
		cm.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
	}

	@Override
	public void buildgui() {
		if (haveSpecification()) {
			Iterable<AotNode> childSpecs = specifications.getChildSpecificationsOf(targetNode.getLabel(), nodeSpec,
					targetNode.getClassValue());
			List<AotNode> allowedChildSpecs = newChildList(childSpecs);
			List<AotNode> orphanedChildren = orphanedChildList(childSpecs);
			Iterable<AotNode> edgeSpecs = specifications.getEdgeSpecificationsOf(targetNode.getLabel(), nodeSpec,
					targetNode.getClassValue());
			List<Pair<String, AotNode>> allowedEdges = newEdgeList(edgeSpecs);

			if (!allowedChildSpecs.isEmpty()) {
				Menu mu = MenuLabels.addMenu(cm,MenuLabels.ML_NEW);
				for (AotNode child: allowedChildSpecs) {
					addOptionNewChild(mu,child);
				}
				// add new children options
			}

			if (!orphanedChildren.isEmpty()) {
				// list new toNode edge options
				
			}

			if (!allowedEdges.isEmpty()) {
				// addEdgeOptions
			}

			cm.getItems().add(new SeparatorMenuItem());

			if (targetNode.getChildren().isEmpty()) {
				// add exportTreeOptions
			}
			if (!allowedChildSpecs.isEmpty()) {
				// add import tree options
			}

			//--------------------------------------
			if (targetNode.canDelete() || !targetNode.getChildren().isEmpty())
				if (!(allowedChildSpecs.isEmpty() && orphanedChildren.isEmpty()))
					cm.getItems().add(new SeparatorMenuItem());

		}
		if (targetNode.haschildren()|| targetNode.hasOutEdges()) {
			// delete edges to children || xlinks - maybe problem
		}
		
		if(targetNode.canDelete()) {
			// add delete option
		}
		
		if (targetNode.haschildren()) {
			// delete tree options
		}
		
		if (!targetNode.isLeaf()) {
			cm.getItems().add(new SeparatorMenuItem());
			if (targetNode.isCollapsed()) {
				// add expand option
			} else {
				// add collapse option
			}
		}

	}
	private void addOptionNewChild(Menu mu, AotNode childRoot) {
		String label = specifications.getLabel(childRoot);
		MenuItem mi = new MenuItem(label);
		mu.getItems().add(mi);
		mi.setOnAction((e) ->{
			String defName=label+"1";
			boolean doUpper = specifications.nameStartsWithUpperCase(childRoot);
			if (doUpper)
				defName = WordUtils.capitalize(defName); 
			String userName = promptForNewNode(label,defName);
			if (userName!=null)
				userName = userName.trim();
			if (userName.equals(""))
				userName = defName;
			if (doUpper)
				userName = WordUtils.capitalize(userName);
			userName = targetNode.getUniqueName(label, userName);
			// make the node
			newChild = targetNode.newChild(childRoot,label,userName);
			Iterable<AotNode> propertySpecs = specifications.getPropertySpecifications(childRoot);
			
			GraphState.isChanged(true);
		});		
	}
	@Override
	public String promptForNewNode(String label, String promptName) {
		return Dialogs.getText("New "+label+ "node","", "Name:", promptName);
	}
	
	private enum MenuLabels{
		ML_NEW /*-             */("New"), //
		ML_CONNECT_TO/*-       */("Connect to"), //
		ML_CONNECT_TO_CHILD/*- */("Connect to child"), //
		ML_DISCONNECT_FROM/*-  */("Disconnect from"), //
		ML_DELETE/*-           */("Delete"), //
		ML_IMPORT_TREE/*-      */("Import tree"), //
		ML_DELETE_TREE/*-      */("Delete tree"), //
		ML_EXPORT_TREE/*-      */("Export tree"), //
		ML_SELECT_PARAMETERS/* */("Select parameters"), //
		ML_SELECT_DRIVERS/*    */("Select drivers"), //
		ML_SELECT_DECORATORS/* */("Select decorators"),//
		ML_EXPAND/*            */("Expand"),//
		ML_COLLAPSE/*          */("Collapse"),//        
		;

		private final String label;
		
		private MenuLabels(String label) {
			this.label = label;
		}
		public String label() {
			return label;
		}
		public static Menu addMenu(ContextMenu cm, MenuLabels ml) {
			Menu result = new Menu(ml.label());
			cm.getItems().add(result);
			return result;
		}
		
		public static Menu addMenu(Menu mu, MenuLabels ml) {
			Menu result = new Menu(ml.label());
			mu.getItems().add(result);
			return result;
		}
		
		public static MenuItem addMenuItem(ContextMenu cm, MenuLabels ml) {
			MenuItem result = new MenuItem(ml.label());
			cm.getItems().add(result);
			return result;
		}
	}

}
