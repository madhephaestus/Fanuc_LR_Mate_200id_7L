import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import com.neuronrobotics.sdk.common.DeviceManager

//Your code here
MobileBase base=DeviceManager.getSpecificDevice( "Fanuc_LR_Mate_200id_7L",{
			//If the device does not exist, prompt for the connection
			
			MobileBase m = MobileBaseLoader.fromGit(
				"https://github.com/madhephaestus/Fanuc_LR_Mate_200id_7L.git",
				"Fanuc_LR_Mate_200id_7L.xml"
				)
			if(m==null)
				throw new RuntimeException("Arm failed to assemble itself")
			println "Connecting new device robot arm "+m
			return m
		})

def jointRangeLimitsRad=[2.967060, 2.138028, 3.752458, 3.316126, 2.181662,6.283185]

DHParameterKinematics fanuc = base.getAllDHChains().get(0)	

for(int i=0;i<jointRangeLimitsRad.size();i++){
	double deg = jointRangeLimitsRad[i]*180/Math.PI
	println "Link "+i+" val "+deg
	fanuc.getLinkConfiguration(i).setDeviceTheoreticalMax(deg)
	fanuc.getLinkConfiguration(i).setDeviceTheoreticalMin(-deg)
	fanuc.getLinkConfiguration(i).setUpperLimit(deg)
	fanuc.getLinkConfiguration(i).setLowerLimit(-deg)
	
}
