import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
def gitURL = "https://github.com/madhephaestus/Fanuc_LR_Mate_200id_7L.git"

def mockCMOD = ScriptingEngine.gitScriptRun(gitURL, "loadHardware.groovy")


MobileBase fromGit = MobileBaseLoader.fromGit(gitURL, "Fanuc_LR_Mate_200id_7L.xml");
fromGit.connect();
simulator = fromGit
				.getAllDHChains().get(0);
simulator.homeAllLinks();