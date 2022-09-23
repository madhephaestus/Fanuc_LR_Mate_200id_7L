import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import javafx.scene.paint.Color

MobileBase handMB =args[0]
ArrayList<CSG> parts = []
def calTipConeHeight = 22.5
def calSpikeRad=15.8/2
def calSpikeShaftlen = 160-calTipConeHeight
CSG tip = new Cylinder(0, // Radius at the bottom
		calSpikeRad, // Radius at the top
		calTipConeHeight, // Height
		(int)30 //resolution
		).toCSG()//convert to CSG to display                    			         ).toCSG()//convert to CSG to display
CSG calShaft =new Cylinder(calSpikeRad,calSpikeShaftlen).toCSG() // a one line Cylinder
				.movez(calTipConeHeight)
def link = tip.union(calShaft)
link.setColor(Color.web("#C0C0C0"))
double fingerTop = 160-68.5
boolean circle=false;
double handThickness=25
CSG lug = new Cube(handThickness,30,40 ).toCSG()
			.toYMax()
def fingerLugs = []
for(DHParameterKinematics k:handMB.getAppendages()) {
	def mod = k.getRobotToFiducialTransform()
	def csgtf = TransformFactory.nrToCSG(mod);
	CSG lugTransformed = lug.transformed(csgtf)
			.toZMin()
			.movez(fingerTop)
			.setColor(Color.WHITE)
	fingerLugs.add(lugTransformed)
	
}

double seperation = 60
CSG hand = new Cube(handThickness ).toCSG()
			.toZMin()
			.movez(fingerTop)
			.union(fingerLugs)
			.hull()
			.setColor(Color.WHITE)
			
CSG hose =new Cylinder(4, 60).toCSG()
			.rotx(-70)
			.movez(fingerTop+25)
			.setColor(Color.LIGHTBLUE)
parts.addAll([hand,hose])
parts.addAll(fingerLugs)
for(CSG c:parts) {
	c.setManipulator(handMB.getRootListener())
}
return parts;