import com.neuronrobotics.bowlerstudio.creature.ICadGenerator
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase

import eu.mihosoft.vrl.v3d.CSG

//Your code here

return new ICadGenerator() {

	@Override
	public ArrayList<CSG> generateCad(DHParameterKinematics dh, int linkIndex) {
		ArrayList<CSG> parts = new ArrayList();
		
		return parts;
	}

	@Override
	public ArrayList<CSG> generateBody(MobileBase base) {
		ArrayList<CSG> parts = new ArrayList();
		return parts;
	}
	
}