import com.neuronrobotics.sdk.common.BowlerAbstractDevice

import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.handler.MovedContextHandler

import com.neuronrobotics.bowlerstudio.vitamins.*;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import com.neuronrobotics.sdk.common.IDeviceAddedListener
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener

import java.nio.file.Paths;

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import eu.mihosoft.vrl.v3d.FileUtil
import eu.mihosoft.vrl.v3d.Parabola
import eu.mihosoft.vrl.v3d.RoundedCube
import eu.mihosoft.vrl.v3d.RoundedCylinder
import eu.mihosoft.vrl.v3d.Sphere
import eu.mihosoft.vrl.v3d.Transform
import javafx.scene.paint.Color
import javafx.scene.transform.Affine;
import  eu.mihosoft.vrl.v3d.ext.quickhull3d.*
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter
import eu.mihosoft.vrl.v3d.Vector3d

CSG reverseDHValues(CSG incoming,DHLink dh ){
	//println "Reversing "+dh
	TransformNR step = new TransformNR(dh.DhStep(0))
	Transform move = com.neuronrobotics.bowlerstudio.physics.TransformFactory.nrToCSG(step)
	return incoming.transformed(move)
}

CSG moveDHValues(CSG incoming,DHLink dh ){
	TransformNR step = new TransformNR(dh.DhStep(0)).inverse()
	Transform move = com.neuronrobotics.bowlerstudio.physics.TransformFactory.nrToCSG(step)
	return incoming.transformed(move)
}

return new ICadGenerator(){

			@Override
			public ArrayList<CSG> generateCad(DHParameterKinematics arg0, int arg1) {
				ArrayList<CSG> parts =  new ArrayList<>();
				DHLink dh = arg0.getDhLink(arg1)
				Affine manipulator = arg0.getLinkObjectManipulator(arg1)
				if(arg1<6) {

					def name = "mesh/link_"+(arg1+1)+".stl"
					println "Loading "+name
					CSG link  = moveDHValues(Vitamins.get(ScriptingEngine.fileFromGit(
							"https://github.com/madhephaestus/Fanuc_LR_Mate_200id_7L.git",
							name)).scale(1000),dh)
					if(arg1==0) {
						link=link
					}
					if(arg1==1) {
						link=link.roty(90).movez(440)
					}
					if(arg1==2) {
						link=reverseDHValues(link,dh)
								.movex(-35)
					}
					if(arg1==3) {
						link=moveDHValues(reverseDHValues(link,dh).roty(90),dh).movey(-5)
					}
					if(arg1==4) {
						link=moveDHValues(reverseDHValues(link,dh).roty(90),dh)	.rotx(90)
					}
					if(arg1==5) {
						link=reverseDHValues(link,dh).roty(90).movez(-160)
					}
					link.setManipulator(manipulator)
					link.setColor(Color.web("#f3da0b"))
					parts.add(link)
				}else if(arg1==6) {
					MobileBase handMB = arg0.getSlaveMobileBase(arg1)
					parts.addAll(getHandParts(handMB))
				}
				for(int i=0;i<parts.size();i++) {
					parts.get(i).setName("Fanuc link "+arg1+" part "+i)
				}	
				return parts;
			}
			private ArrayList<CSG> getHandParts(MobileBase handMB){
				return ScriptingEngine.gitScriptRun(
						"https://github.com/madhephaestus/Fanuc_LR_Mate_200id_7L.git",
						"makeModularHand.groovy",[handMB])
			}

			@Override
			public ArrayList<CSG> generateBody(MobileBase arg0) {
				ArrayList<CSG> parts =  new ArrayList<>();
				CSG base  = Vitamins.get(ScriptingEngine.fileFromGit(
						"https://github.com/madhephaestus/Fanuc_LR_Mate_200id_7L.git",
						"mesh/base_link.stl")).scale(1000)
						.toZMax()
						.movez(-160)
				Affine manipulator = arg0.getRootListener()
				base.setManipulator(manipulator)

				parts.add(base)
				for(CSG part :parts) {
					part.setColor(Color.web("#f3da0b"))
				}
				for(int i=0;i<parts.size();i++) {
					parts.get(i).setName("Fanuc link base part "+i)
				}
				return parts;
			}
		};
