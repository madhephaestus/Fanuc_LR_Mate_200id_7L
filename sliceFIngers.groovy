import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube

//Your code here
CSG finger=Vitamins.get(ScriptingEngine.fileFromGit(
	"https://github.com/madhephaestus/Fanuc_LR_Mate_200id_7L.git",
	"mesh/4acc-5k-kin-centered.stl"))
double heightOfBase = -23
double heightOfRib = 16
double lengthOfTip  =32
def fingerGetBoundingBoxToZMinMovez = finger.getBoundingBox().toZMin().movez(heightOfBase)
CSG Base = finger.intersect(fingerGetBoundingBoxToZMinMovez).toZMax()
CSG rib = finger.intersect(
			new Cube(Base.getTotalX(),Base.getTotalY(),heightOfRib).toCSG()
			.toZMax()
			.movez(heightOfBase)		
	).toZMax()
def TipCut = finger.getBoundingBox()
	.toZMax()
	.movez(finger.getMinZ()+lengthOfTip)
CSG tip = finger.intersect(TipCut)
.toZMax()
Base.setName("fingerBase")
rib.setName("fingerRib")
tip.setName("fingertip")
return [Base,rib,tip]