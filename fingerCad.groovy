import com.neuronrobotics.bowlerstudio.creature.ICadGenerator
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Transform
import javafx.scene.paint.Color

//Your code here
CSG moveDHValues(CSG incoming,DHLink dh ){
	TransformNR step = new TransformNR(dh.DhStep(0)).inverse()
	Transform move = com.neuronrobotics.bowlerstudio.physics.TransformFactory.nrToCSG(step)
	return incoming.transformed(move)
}
return new ICadGenerator() {

	@Override
	public ArrayList<CSG> generateCad(DHParameterKinematics dh, int linkIndex) {
		ArrayList<CSG> parts = new ArrayList();
		CSG finger
		DHLink dhl = dh.getDhLink(linkIndex)
		if(linkIndex==0) {
			def base=Vitamins.get(ScriptingEngine.fileFromGit(
				"https://github.com/madhephaestus/Fanuc_LR_Mate_200id_7L.git",
				"mesh/fingerBase.stl"))
			.roty(90)
			.toYMax()
			.toXMax()
			.movey(2)
			base.setColor(Color.BLUE)
			base.setManipulator(dh.getRootListener())
			parts.add(base)
		}
		if(linkIndex==dh.getNumberOfLinks()-1) {
			
			 finger=Vitamins.get(ScriptingEngine.fileFromGit(
				"https://github.com/madhephaestus/Fanuc_LR_Mate_200id_7L.git",
				"mesh/fingertip.stl"))
			 

		}else {
			finger=Vitamins.get(ScriptingEngine.fileFromGit(
				"https://github.com/madhephaestus/Fanuc_LR_Mate_200id_7L.git",
				"mesh/fingerRib.stl"))

		}
		finger=finger
				.roty(90)
				.toYMax()
				.toXMax()
				.movey(2)
		if(linkIndex!=0) {
			finger.getStorage().set("no-collide", true);
		}		
		finger.setColor(Color.BLUE)
		finger.setManipulator(dh.getLinkObjectManipulator(linkIndex))
		parts.add(finger)
		for(int i=0;i<parts.size();i++) {
			parts.get(i).setName(dh.getScriptingName()+" bellow "+linkIndex+" part "+i)
		}
		return parts;
	}

	@Override
	public ArrayList<CSG> generateBody(MobileBase base) {
		ArrayList<CSG> parts = new ArrayList();
		parts.add(new Cube(0.01).toCSG())
		return parts;
	}
	
}